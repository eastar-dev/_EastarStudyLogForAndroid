@file:Suppress("unused", "UNUSED_PARAMETER")

package android.view

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.base.CActivity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.log.Log
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Looper
import android.os.Message
import android.provider.Browser
import android.text.InputType
import android.util.AttributeSet
import android.webkit.*
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

open class CWebView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.webViewStyle) : WebView(context, attrs, defStyleAttr) {

    companion object {
        protected const val APPLICATION_ID = "webapp"

        /////////////////////////////////////////////////////////////////////////////////////////////////
        //source----------------------------------------------------------------------------
        private const val SOURCE = "SOURCE"
        private const val SOURCE_VIEW = "SOURCE_VIEW"
        private const val SCROLL_SOURCE_VIEW = "SCROLL_SOURCE_VIEW"

        //colsoleLog-----------------------------------------------------------------------
        private const val LOG_VIEW = "LOG_VIEW"
        private const val SCROLL_LOG_VIEW = "SCROLL_LOG_VIEW"

        private const val MAX_LOG_LENGTH = 300000
        private const val HWBACK = "HWBACK"
        val EXTRA_URL = "url"
    }

    private val builder = StringBuilder()

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    private var mConsumeJsBack: Boolean = false
    private var javascriptFunctionNameForHWBackkey: String? = null

    private var onWebSettingsListener: OnWebSettingsListener? = null
    private var onWebViewClientListener: OnWebViewClientListener? = null
    private var onWebChromeClientListener: OnWebChromeClientListener? = null
    private var onAddJavascriptListener: OnAddJavascriptListener? = null
    private var onShouldOverrideUrlLoading: ShouldOverrideUrlLoading? = null
    private var onLoadResource: OnLoadResource? = null
    private var onPageStarted: OnPageStarted? = null
    private var onPageFinished: OnPageFinished? = null
    private var onReceivedSslError: OnReceivedSslError? = null
    private var onReceivedError: OnReceivedError? = null
    private var onConsoleMessage: OnConsoleMessage? = null
    private var onProgressChanged: OnProgressChanged? = null
    private var onReceivedTitle: OnReceivedTitle? = null
    private var onJsAlert: OnJsAlert? = null
    private var onJsConfirm: OnJsConfirm? = null
    private var onJsPrompt: OnJsPrompt? = null

    interface WebActivityInterface {
        fun loadUrl(url: String)

        fun sendJavascript(script: String)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setWebSettings()
        setWebViewClient()
        setWebChromeClient()
        addJavascriptInterface()
    }

    @SuppressLint("SetJavaScriptEnabled")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected fun setWebSettings() {
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = false

        val webSettings = settings
        onWebSettings(webSettings)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun onWebSettings(webSettings: WebSettings) {
        webSettings.javaScriptEnabled = true

        webSettings.setSupportMultipleWindows(true)

        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true

        onWebSettingsListener?.onWebSettings(webSettings)
    }

    protected open fun setWebViewClient() {
        webViewClient = CWebViewClient()
        onWebViewClientListener?.onWebViewClient(this)
    }

    protected open fun setWebChromeClient() {
        webChromeClient = BChromeClient()
        onWebChromeClientListener?.onWebChromeClient(this)
    }

    protected open fun addJavascriptInterface() {
        addViewSourceJavascriptInterface()
        addJsBackJavascriptInterface()
        onAddJavascriptListener?.onAddJavascript(this)
    }

    override fun loadUrl(url: String) {
        Log.e(url)
        val extraHeaders = HashMap<String, String>()
        extraHeaders["Referer"] = getUrl()
        super.loadUrl(url, extraHeaders)
    }

    fun toggleSource() {
        if (findViewWithTag<View>(SCROLL_SOURCE_VIEW) == null) {
            val context = context
            val tv = TextView(context)
            tv.tag = SOURCE_VIEW
            tv.setTextColor(Color.RED)
            val sv = ScrollView(context)
            sv.tag = SCROLL_SOURCE_VIEW
            sv.addView(tv)
            addView(sv)
        } else {
            val sv = findViewWithTag<View>(SCROLL_SOURCE_VIEW)
            sv.visibility = View.GONE - sv.visibility
        }

        if (findViewWithTag<View>(SCROLL_SOURCE_VIEW).visibility == View.VISIBLE)
            sendJavascript("$SOURCE.viewSource(document.documentElement.outerHTML);")
    }

    @SuppressLint("AddJavascriptInterface")
    private fun addViewSourceJavascriptInterface() {
        addJavascriptInterface(object : Any() {
            @JavascriptInterface
            fun viewSource(source: String) {
                //NOT MAIN THREAD
                post { (findViewWithTag<View>(SOURCE_VIEW) as TextView).text = source }
            }
        }, SOURCE)
    }

    fun toggleConsoleLog() {
        if (findViewWithTag<View>(SCROLL_LOG_VIEW) == null) {
            val context = context
            val tv = TextView(context)
            tv.tag = LOG_VIEW
            tv.setTextColor(Color.BLUE)
            val sv = ScrollView(context)
            sv.isFillViewport = true
            sv.tag = SCROLL_LOG_VIEW
            sv.addView(tv)
            sv.setBackgroundColor(0x55ff0000)
            addView(sv, -1, -1)
        } else {
            val sv = findViewWithTag<View>(SCROLL_LOG_VIEW)
            sv.visibility = View.GONE - sv.visibility
        }

        if (findViewWithTag<View>(SCROLL_LOG_VIEW).visibility == View.VISIBLE) {
            val millis = System.currentTimeMillis()
            consoleLog(">>SHOW{$millis}")
        }
    }

    private fun consoleLog(text: CharSequence) {
        builder.insert(0, '\n')
        builder.insert(0, text)
        builder.setLength(MAX_LOG_LENGTH)
        val sv = findViewWithTag<ScrollView>(SCROLL_LOG_VIEW)
        sv?.post { (findViewWithTag<View>(LOG_VIEW) as TextView).text = builder.toString() }
    }

    //backkey
    private var mSemaphore: Semaphore? = null

    fun setJavascriptFunctionNameForHWBackkey(javascriptFunctionNameForHWBackkey: String) {
        this.javascriptFunctionNameForHWBackkey = javascriptFunctionNameForHWBackkey
    }

    fun onBackPressedJavascriptFunction(): Boolean {
        Log.e("onBackPressedEx", javascriptFunctionNameForHWBackkey)
        if (javascriptFunctionNameForHWBackkey.isNullOrEmpty())
            return false

        mSemaphore = Semaphore(0)
        val javascriptBack = """
                 var b = false;
                 try { b = (typeof $javascriptFunctionNameForHWBackkey == 'function'); } catch (e) { b = false; }
                 $HWBACK.setJsBackResult(b);
                 if(b){
                    $javascriptFunctionNameForHWBackkey();
                 }"""

        Log.e(javascriptBack)
        sendJavascript(javascriptBack)
        try {
            mSemaphore?.tryAcquire(1, TimeUnit.SECONDS)
            Log.i("consumeJsBack", mConsumeJsBack)
            return mConsumeJsBack
        } catch (e: InterruptedException) {
            Log.printStackTrace(e)
        }

        return false
    }

    @SuppressLint("AddJavascriptInterface")
    private fun addJsBackJavascriptInterface() {
        Log.e("addJsBackJavascriptInterface", HWBACK)
        addJavascriptInterface(object : Any() {
            @JavascriptInterface
            fun setJsBackResult(consumeJsBack: Boolean) {
                //                Log.i("consumeJsBack", consumeJsBack);
                mConsumeJsBack = consumeJsBack
                mSemaphore?.release()
            }
        }, HWBACK)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun sendJavascript(script: String) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            post { sendJavascript(script) }
            return
        }
//        evaluateJavascript(script, Log::i);
        evaluateJavascript(script, null)
    }

    fun onBackPressedEx(): Boolean {
        if (findViewWithTag<View>(SCROLL_SOURCE_VIEW) != null && findViewWithTag<View>(SCROLL_SOURCE_VIEW).visibility == View.VISIBLE) {
            toggleSource()
            return true
        }

        if (findViewWithTag<View>(SCROLL_LOG_VIEW) != null && findViewWithTag<View>(SCROLL_LOG_VIEW).visibility == View.VISIBLE) {
            toggleConsoleLog()
            return true
        }

        //        if (onBackPressedJavascriptFunction()) {
        //            return true;
        //        }
        //
        //        if (historyBack()) {
        //            return true;
        //        }

        return false
    }

    fun historyBack(): Boolean {
        getBackForwardList()
        val canGoBack = canGoBack()
        if (canGoBack)
            goBack()

        return canGoBack
    }

    private fun getBackForwardList() {
        val currentList = copyBackForwardList()
        val currentSize = currentList.size
        for (i in 0 until currentSize) {
            val item = currentList.getItemAtIndex(i)
            val url = item.url
            Log.d("$i is $url")
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    protected fun isEmpty(cs: CharSequence?): Boolean = cs.isNullOrEmpty()

    private fun isInternal(view: WebView, url: String): Boolean =
        url.matches("^(https?)://.*".toRegex())

    fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        consoleLog("shouldOverrideUrlLoading::$url")
        Log.e(view.title, url)
        if (isEmpty(url))
            return false

        if (url.startsWith("android-app:") || url.startsWith("intent:") || url.startsWith("#Intent;")) {
            runCatching {
                val intent: Intent = Intent.parseUri(url, 0)
                if (intent.action == Intent.ACTION_VIEW && intent.getStringExtra(Browser.EXTRA_APPLICATION_ID)
                        .isNullOrEmpty()
                )
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, APPLICATION_ID)
                view.context.startActivity(intent)
            }.recoverCatching {
                Intent.parseUri(url, 0).`package`?.takeUnless { it.isNotEmpty() }?.let {
                    view.context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$it")
                        )
                    )
                    return true
                } ?: return false
            }.recover {
                if (it is URISyntaxException)
                    return false
            }
        }

        if (isInternal(view, url)) {
            view.loadUrl(url)
            return true
        }

        //!ERROR_UNSUPPORTED_SCHEME
        return runCatching {
            view.context.startActivity(Intent.parseUri(url, 0))
            true
        }.getOrElse {
            Log.w(it)
            false
        }

    }

    fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        when (errorCode) {
            WebViewClient.ERROR_AUTHENTICATION -> Log.w("!ERROR_AUTHENTICATION")// 서버에서 사용자 인증 실패
            WebViewClient.ERROR_BAD_URL -> Log.w("!ERROR_BAD_URL")// 잘못된 URL
            WebViewClient.ERROR_CONNECT -> Log.w("!ERROR_CONNECT")// 서버로 연결 실패
            WebViewClient.ERROR_FAILED_SSL_HANDSHAKE -> Log.w("!ERROR_FAILED_SSL_HANDSHAKE")// SSL handshake 수행 실패
            WebViewClient.ERROR_FILE -> Log.w("!ERROR_FILE")// 일반 파일 오류
            WebViewClient.ERROR_FILE_NOT_FOUND -> Log.w("!ERROR_FILE_NOT_FOUND")// 파일을 찾을 수 없습니다
            WebViewClient.ERROR_HOST_LOOKUP -> Log.w("!ERROR_HOST_LOOKUP")// 서버 또는 프록시 호스트 이름 조회 실패
            WebViewClient.ERROR_IO -> Log.w("!ERROR_IO")// 서버에서 읽거나 서버로 쓰기 실패
            WebViewClient.ERROR_PROXY_AUTHENTICATION -> Log.w("!ERROR_PROXY_AUTHENTICATION")// 프록시에서 사용자 인증 실패
            WebViewClient.ERROR_REDIRECT_LOOP -> Log.w("!ERROR_REDIRECT_LOOP")// 너무 많은 리디렉션
            WebViewClient.ERROR_TOO_MANY_REQUESTS -> Log.w("!ERROR_TOO_MANY_REQUESTS")// 페이지 로드중 너무 많은 요청 발생
            WebViewClient.ERROR_UNKNOWN -> Log.w("!ERROR_UNKNOWN")// 일반 오류
            WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME -> Log.w("!ERROR_UNSUPPORTED_AUTH_SCHEME")// 지원되지 않는 인증 체계
            WebViewClient.ERROR_UNSUPPORTED_SCHEME -> Log.w("!ERROR_UNSUPPORTED_SCHEME")// ?
            WebViewClient.ERROR_TIMEOUT -> Log.w("!ERROR_TIMEOUT")// 연결 시간 초과
            else -> Log.w("OK")// ?
        }

    }

    fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
        Log.pm(Log.ERROR, "onPageStarted", ">WEB:S>", view.title, url)
        if (context !is CActivity)
            return
        val ba = context as CActivity
        ba.showProgress()
    }

    fun onPageFinished(view: WebView, url: String) {
        Log.pm(Log.WARN, "onPageFinished", ">WEB:E>", view.title, url)
        if (context !is CActivity)
            return
        val ba = context as CActivity
        ba.dismissProgress()
    }

    fun onLoadResource(view: WebView, url: String) {
        //		Log.v(url);
    }

    /////////////////////////////////////////////////////////////////////////
    fun onProgressChanged(view: WebView, newProgress: Int) {
        //        Log.d(newProgress, view.getTitle(), view.getUrl());
        //        if (!(getContext() instanceof BActivity))
        //            return;
        //        BActivity ba = (BActivity) getContext();
        //
        //        if (newProgress < 100) {
        //            ba.showProgress();
        //        } else {
        //            ba.dismissProgress();
        //        }
    }

    /////////////////////////////////////////////////////////////////////////
    open fun onJsAlert(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
        Log.e(view.title, url)
        Log.e(message)
        val dlg = AlertDialog.Builder(view.context)
            .setMessage(message)
            .setPositiveButton("확인") { _: DialogInterface?, _: Int -> result.confirm() }
            .create()
        dlg.setOnCancelListener { result.cancel() }
        return true
    }

    open fun onJsConfirm(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
        Log.e(view.title, url)
        Log.e(message)
        val dlg = AlertDialog.Builder(view.context)
            .setMessage(message)
            .setPositiveButton("확인") { _: DialogInterface?, _: Int -> result.confirm() }
            .setNegativeButton("취소") { _: DialogInterface?, _: Int -> result.cancel() }
            .create()
        dlg.setOnCancelListener { result.cancel() }
        return true
    }

    open fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
        val input = EditText(view.context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(defaultValue)
        val dlg = AlertDialog.Builder(view.context)
            .setView(input)
            .setMessage(message)
            .setPositiveButton("확인") { _: DialogInterface?, _: Int -> result.confirm(input.text.toString()) }
            .setNegativeButton("취소") { _: DialogInterface?, _: Int -> result.cancel() }
            .create()
        dlg.setOnCancelListener { result.cancel() }
        dlg.show()
        return true
    }

    fun onReceivedTitle(view: WebView, title: String) {
        //        if (!(view.getContext() instanceof Activity))
        //            ((Activity)view.getContext()).setTitle(title);
    }

    private fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {}

    private fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        val log = consoleMessage.message()
        val tag = consoleMessage.sourceId() + "#" + consoleMessage.lineNumber()
        consoleLog("$log::$tag")
        return true
    }

    interface OnWebSettingsListener {
        fun onWebSettings(settings: WebSettings)
    }

    interface OnWebViewClientListener {
        fun onWebViewClient(webview: WebView)
    }

    interface OnWebChromeClientListener {
        fun onWebChromeClient(webview: WebView)
    }

    interface OnAddJavascriptListener {
        fun onAddJavascript(webview: WebView)
    }

//    open fun setOnWebSettingsListener(onWebSettingsListener: OnWebSettingsListener) {
//        mOnWebSettingsListener = onWebSettingsListener
//    }
//
//    fun setOnWebViewClientListener(onWebViewClientListener: OnWebViewClientListener) {
//        mOnWebViewClientListener = onWebViewClientListener
//    }
//
//    fun setOnWebChromeClientListener(onWebChromeClientListener: OnWebChromeClientListener) {
//        mOnWebChromeClientListener = onWebChromeClientListener
//    }
//
//    fun setOnAddJavascriptListener(onAddJavascriptListener: OnAddJavascriptListener) {
//        mOnAddJavascriptListener = onAddJavascriptListener
//    }
//
//    fun setShouldOverrideUrlLoading(shouldOverrideUrlLoading: ShouldOverrideUrlLoading) {
//        mShouldOverrideUrlLoading = shouldOverrideUrlLoading
//    }
//
//    fun setOnLoadResource(onLoadResource: OnLoadResource) {
//        mOnLoadResource = onLoadResource
//    }
//
//    fun setOnPageStarted(onPageStarted: OnPageStarted) {
//        mOnPageStarted = onPageStarted
//    }
//
//    fun setOnPageFinished(onPageFinished: OnPageFinished) {
//        mOnPageFinished = onPageFinished
//    }
//
//    fun setOnReceivedSslError(onReceivedSslError: OnReceivedSslError) {
//        mOnReceivedSslError = onReceivedSslError
//    }
//
//    fun setOnReceivedError(onReceivedError: OnReceivedError) {
//        mOnReceivedError = onReceivedError
//    }
//
//    fun setOnConsoleMessage(onConsoleMessage: OnConsoleMessage) {
//        mOnConsoleMessage = onConsoleMessage
//    }
//
//    fun setOnProgressChanged(onProgressChanged: OnProgressChanged) {
//        mOnProgressChanged = onProgressChanged
//    }
//
//    fun setOnReceivedTitle(onReceivedTitle: OnReceivedTitle) {
//        mOnReceivedTitle = onReceivedTitle
//    }
//
//    fun setOnJsAlert(onJsAlert: OnJsAlert) {
//        mOnJsAlert = onJsAlert
//    }
//
//    fun setOnJsConfirm(onJsConfirm: OnJsConfirm) {
//        mOnJsConfirm = onJsConfirm
//    }
//
//    fun setOnJsPrompt(onJsPrompt: OnJsPrompt) {
//        mOnJsPrompt = onJsPrompt
//    }

    interface ShouldOverrideUrlLoading {
        fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean
    }

    interface OnLoadResource {
        fun onLoadResource(view: WebView, url: String)
    }

    interface OnPageStarted {
        fun onPageStarted(view: WebView, url: String, favicon: Bitmap)
    }

    interface OnPageFinished {
        fun onPageFinished(view: WebView, url: String)
    }

    interface OnReceivedSslError {
        fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError)
    }

    interface OnReceivedError {
        fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String)
    }

    interface OnConsoleMessage {
        fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean
    }

    interface OnProgressChanged {
        fun onProgressChanged(view: WebView, newProgress: Int)
    }

    interface OnReceivedTitle {
        fun onReceivedTitle(view: WebView, title: String)
    }

    interface OnJsAlert {
        fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean
    }

    interface OnJsConfirm {
        fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean
    }

    interface OnJsPrompt {
        fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean
    }

    inner class CWebViewClient : WebViewClient() {
        //@formatter:on
        override fun shouldOverrideUrlLoading(view: WebView, url: String) = onShouldOverrideUrlLoading?.shouldOverrideUrlLoading(view, url) ?: this@CWebView.shouldOverrideUrlLoading(view, url)
        override fun onLoadResource(view: WebView, url: String) = onLoadResource?.onLoadResource(view, url) ?: this@CWebView.onLoadResource(view, url)
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) = onPageStarted?.onPageStarted(view, url, favicon) ?: this@CWebView.onPageStarted(view, url, favicon)
        override fun onPageFinished(view: WebView, url: String) = onPageFinished?.onPageFinished(view, url) ?: this@CWebView.onPageFinished(view, url)
        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) = onReceivedSslError?.onReceivedSslError(view, handler, error) ?: this@CWebView.onReceivedSslError(view, handler, error)
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) = onReceivedError?.onReceivedError(view, errorCode, description, failingUrl) ?: this@CWebView.onReceivedError(view, errorCode, description, failingUrl)
        //@formatter:on
    }

    inner class BChromeClient : WebChromeClient() {
        //@formatter:on
        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean = onConsoleMessage?.onConsoleMessage(consoleMessage) ?: this@CWebView.onConsoleMessage(consoleMessage)
        override fun onProgressChanged(view: WebView, newProgress: Int) = onProgressChanged?.onProgressChanged(view, newProgress) ?: this@CWebView.onProgressChanged(view, newProgress)
        override fun onReceivedTitle(view: WebView, title: String) = onReceivedTitle?.onReceivedTitle(view, title) ?: this@CWebView.onReceivedTitle(view, title)
        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean = onJsAlert?.onJsAlert(view, url, message, result) ?: this@CWebView.onJsAlert(view, url, message, result)
        override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean = onJsConfirm?.onJsConfirm(view, url, message, result) ?: this@CWebView.onJsConfirm(view, url, message, result)
        override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean = onJsPrompt?.onJsPrompt(view, url, message, defaultValue, result) ?: this@CWebView.onJsPrompt(view, url, message, defaultValue, result)
        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean = this@CWebView.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
        override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) = this@CWebView.onGeolocationPermissionsShowPrompt(origin, callback)
        //@formatter:on
    }

    fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        //https://play.google.com/apps/publish/?account=8841149513553108353#AndroidMetricsErrorsPlace:p=com.kebhana.hanapush&appid=4976086679178587985&appVersion=PRODUCTION&clusterName=apps/com.kebhana.hanapush/clusters/bffa9a37&detailsAppVersion=PRODUCTION&detailsSpan=7
        callback.invoke(origin, false, false)
        //        if (getContext() instanceof BActivity) {
        //            BActivity ba = (BActivity) getContext();
        //            ba.showDialog(origin + " 에서 위치정보를 사용하려 합니다." //
        //                    , "승인", (dialog, which) -> callback.invoke(origin, true, true)//
        //                    , "이번만", (dialog, which) -> callback.invoke(origin, true, false)//
        //                    , "불가", (dialog, which) -> callback.invoke(origin, false, false)//
        //            );
        //        }
    }

    private fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
        Log.e(view, isDialog, isUserGesture, resultMsg)
        val newWebView = WebView(view.context)
        val transport = resultMsg.obj as WebViewTransport
        transport.webView = newWebView
        resultMsg.sendToTarget()
        return true
    }


}

