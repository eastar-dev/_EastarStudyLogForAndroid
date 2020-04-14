package android.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.base.CActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.log.Log;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.provider.Browser;
import android.text.InputType;
import android.util.AttributeSet;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import dev.eastar.ktx.KtxTextKt;

@SuppressWarnings("unused")
public class BWebView extends android.webkit.WebView {
    protected static final String APPLICATION_ID = "webapp";

    public BWebView(Context context) {
        super(context);
    }

    public BWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface WebActivityInterface {
        void loadUrl(String url);

        void sendJavascript(String script);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        setWebSettings();
        setWebViewClient();
        setWebChromeClient();
        addJavascriptInterface();
    }

    @SuppressLint("SetJavaScriptEnabled")
    protected void setWebSettings() {
        setVerticalScrollBarEnabled(true);
        setHorizontalScrollBarEnabled(false);

        final WebSettings webSettings = getSettings();
        onWebSettings(webSettings);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void onWebSettings(WebSettings webSettings) {
        webSettings.setJavaScriptEnabled(true);

        webSettings.setSupportMultipleWindows(true);

        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        if (mOnWebSettingsListener != null)
            mOnWebSettingsListener.onWebSettings(webSettings);
    }

    protected void setWebViewClient() {
        setWebViewClient(new BWebViewClient());
        if (mOnWebViewClientListener != null)
            mOnWebViewClientListener.onWebViewClient(this);
    }

    protected void setWebChromeClient() {
        setWebChromeClient(new BChromeClient());
        if (mOnWebChromeClientListener != null)
            mOnWebChromeClientListener.onWebChromeClient(this);
    }

    protected void addJavascriptInterface() {
        addViewSourceJavascriptInterface();
        addJsBackJavascriptInterface();
        if (mOnAddJavascriptListener != null)
            mOnAddJavascriptListener.onAddJavascript(this);
    }

    @Override
    public void loadUrl(String url) {
        Log.e(">WEB:L>", url);
        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("Referer", getUrl());
        super.loadUrl(url, extraHeaders);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //source----------------------------------------------------------------------------
    private static final String SOURCE = "SOURCE";
    private static final String SOURCE_VIEW = "SOURCE_VIEW";
    private static final String SCROLL_SOURCE_VIEW = "SCROLL_SOURCE_VIEW";

    public void toggleSource() {
        if (findViewWithTag(SCROLL_SOURCE_VIEW) == null) {
            final Context context = getContext();
            TextView tv = new TextView(context);
            tv.setTag(SOURCE_VIEW);
            tv.setTextColor(Color.RED);
            final ScrollView sv = new ScrollView(context);
            sv.setTag(SCROLL_SOURCE_VIEW);
            sv.addView(tv);
            addView(sv);
        } else {
            final View sv = findViewWithTag(SCROLL_SOURCE_VIEW);
            sv.setVisibility(View.GONE - sv.getVisibility());
        }

        if (findViewWithTag(SCROLL_SOURCE_VIEW).getVisibility() == View.VISIBLE)
            sendJavascript(SOURCE + ".viewSource(document.documentElement.outerHTML);");
    }

    @SuppressLint("AddJavascriptInterface")
    private void addViewSourceJavascriptInterface() {
        addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void viewSource(final String source) {
                //NOT MAIN THREAD
                post(() -> ((TextView) findViewWithTag(SOURCE_VIEW)).setText(source));
            }
        }, SOURCE);
    }

    //colsoleLog-----------------------------------------------------------------------
    private static final String LOG_VIEW = "LOG_VIEW";
    private static final String SCROLL_LOG_VIEW = "SCROLL_LOG_VIEW";
    private StringBuilder builder = new StringBuilder();

    public void toggleConsoleLog() {
        if (findViewWithTag(SCROLL_LOG_VIEW) == null) {
            final Context context = getContext();
            TextView tv = new TextView(context);
            tv.setTag(LOG_VIEW);
            tv.setTextColor(Color.BLUE);
            final ScrollView sv = new ScrollView(context);
            sv.setFillViewport(true);
            sv.setTag(SCROLL_LOG_VIEW);
            sv.addView(tv);
            sv.setBackgroundColor(0x55ff0000);
            addView(sv, -1, -1);
        } else {
            final View sv = findViewWithTag(SCROLL_LOG_VIEW);
            sv.setVisibility(View.GONE - sv.getVisibility());
        }

        if (findViewWithTag(SCROLL_LOG_VIEW).getVisibility() == View.VISIBLE) {
            long millis = System.currentTimeMillis();
            consoleLog(">>SHOW{" + millis + "}");
        }
    }

    private static final int MAX_LOG_LENGTH = 300000;

    public void consoleLog(CharSequence text) {
        builder.insert(0, '\n');
        builder.insert(0, text);
        builder.setLength(MAX_LOG_LENGTH);
        final ScrollView sv = findViewWithTag(SCROLL_LOG_VIEW);
        if (sv != null)
            sv.post(() -> ((TextView) findViewWithTag(LOG_VIEW)).setText(builder.toString()));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    private Semaphore mSemaphore;
    private boolean mConsumeJsBack;
    private static final String HWBACK = "HWBACK";
    private String mJavascriptFunctionNameForHWBackkey;

    public void setJavascriptFunctionNameForHWBackkey(String javascriptFunctionNameForHWBackkey) {
        mJavascriptFunctionNameForHWBackkey = javascriptFunctionNameForHWBackkey;
    }

    public boolean onBackPressedJavascriptFunction() {
        Log.e("onBackPressedEx", mJavascriptFunctionNameForHWBackkey);
        if (mJavascriptFunctionNameForHWBackkey == null || mJavascriptFunctionNameForHWBackkey.length() <= 0) {
            return false;
        }
        mSemaphore = new Semaphore(0);
        final String javascriptBack = String.format(""//
                        + "var b = false;" //
                        + "try { b = (typeof %1$s == 'function'); } catch (e) { b = false; }" //
                        + HWBACK + ".setJsBackResult(b);"//
                        + "if(b){"//
                        + "   %1$s();"//
                        + "}"//
                , mJavascriptFunctionNameForHWBackkey);
//        Log.e(javascriptBack);
        sendJavascript(javascriptBack);
        try {
            mSemaphore.tryAcquire(1, TimeUnit.SECONDS);
            Log.i("consumeJsBack", mConsumeJsBack);
            return mConsumeJsBack;
        } catch (InterruptedException e) {
            Log.printStackTrace(e);
        }
        return false;
    }

    @SuppressLint("AddJavascriptInterface")
    private void addJsBackJavascriptInterface() {
        Log.e("addJsBackJavascriptInterface", HWBACK);
        addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void setJsBackResult(final boolean consumeJsBack) {
//                Log.i("consumeJsBack", consumeJsBack);
                mConsumeJsBack = consumeJsBack;
                mSemaphore.release();
            }
        }, HWBACK);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void sendJavascript(String script) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            post(() -> sendJavascript(script));
            return;
        }

//            evaluateJavascript(script, Log::i);
        evaluateJavascript(script, null);
    }

    public boolean onBackPressedEx() {
        if (findViewWithTag(SCROLL_SOURCE_VIEW) != null && findViewWithTag(SCROLL_SOURCE_VIEW).getVisibility() == View.VISIBLE) {
            toggleSource();
            return true;
        }

        if (findViewWithTag(SCROLL_LOG_VIEW) != null && findViewWithTag(SCROLL_LOG_VIEW).getVisibility() == View.VISIBLE) {
            toggleConsoleLog();
            return true;
        }

//        if (onBackPressedJavascriptFunction()) {
//            return true;
//        }
//
//        if (historyBack()) {
//            return true;
//        }

        return false;
    }

    public boolean historyBack() {
        getBackForwardList();
        final boolean canGoBack = canGoBack();
        if (canGoBack)
            goBack();

        return canGoBack;
    }

    protected void getBackForwardList() {
        WebBackForwardList currentList = copyBackForwardList();
        int currentSize = currentList.getSize();
        for (int i = 0; i < currentSize; ++i) {
            WebHistoryItem item = currentList.getItemAtIndex(i);
            String url = item.getUrl();
            Log.d("" + i + " is " + url);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    protected boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() <= 0;
    }

    protected boolean isInternal(WebView view, String url) {
        return url.matches("^(https?)://.*");
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        consoleLog("shouldOverrideUrlLoading" + url);
        Log.e(view.getTitle(), url);
        if (isEmpty(url))
            return false;

        if (url.startsWith("android-app:") || url.startsWith("intent:") || url.startsWith("#Intent;")) {
            Intent intent = null;
            try {
                intent = Intent.parseUri(url, 0);
                final String Browser_ID = intent.getStringExtra(Browser.EXTRA_APPLICATION_ID);
                final String action = intent.getAction();
                if (action != null && action.equals(Intent.ACTION_VIEW) && (Browser_ID == null || Browser_ID.length() <= 0))
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, APPLICATION_ID);
                view.getContext().startActivity(intent);
                return true;
            } catch (URISyntaxException e) {
                Log.printStackTrace(e);
            } catch (ActivityNotFoundException e) {
                Log.w(e.getMessage(), intent);
                if (intent != null) {
                    final String packageName = intent.getPackage();
                    if (packageName == null || packageName.length() <= 0)
                        return false;
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    Log.i("!this package intent is auto move to market ");
                }
                return true;
            }
        }

        if (isInternal(view, url)) {
            view.loadUrl(url);
            return true;
        }


        //!ERROR_UNSUPPORTED_SCHEME
        try {
            view.getContext().startActivity(Intent.parseUri(url, 0));
            return true;
        } catch (URISyntaxException e) {
            Log.printStackTrace(e);
        } catch (ActivityNotFoundException e) {
            Log.w(e.getMessage());
            return true;
        }
        return false;
    }

    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        switch (errorCode) {
            //@formatter:off
            case WebViewClient.ERROR_AUTHENTICATION:
                Log.w("!ERROR_AUTHENTICATION");
                break;// 서버에서 사용자 인증 실패
            case WebViewClient.ERROR_BAD_URL:
                Log.w("!ERROR_BAD_URL");
                break;// 잘못된 URL
            case WebViewClient.ERROR_CONNECT:
                Log.w("!ERROR_CONNECT");
                break;// 서버로 연결 실패
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
                Log.w("!ERROR_FAILED_SSL_HANDSHAKE");
                break;// SSL handshake 수행 실패
            case WebViewClient.ERROR_FILE:
                Log.w("!ERROR_FILE");
                break;// 일반 파일 오류
            case WebViewClient.ERROR_FILE_NOT_FOUND:
                Log.w("!ERROR_FILE_NOT_FOUND");
                break;// 파일을 찾을 수 없습니다
            case WebViewClient.ERROR_HOST_LOOKUP:
                Log.w("!ERROR_HOST_LOOKUP");
                break;// 서버 또는 프록시 호스트 이름 조회 실패
            case WebViewClient.ERROR_IO:
                Log.w("!ERROR_IO");
                break;// 서버에서 읽거나 서버로 쓰기 실패
            case WebViewClient.ERROR_PROXY_AUTHENTICATION:
                Log.w("!ERROR_PROXY_AUTHENTICATION");
                break;// 프록시에서 사용자 인증 실패
            case WebViewClient.ERROR_REDIRECT_LOOP:
                Log.w("!ERROR_REDIRECT_LOOP");
                break;// 너무 많은 리디렉션
            case WebViewClient.ERROR_TOO_MANY_REQUESTS:
                Log.w("!ERROR_TOO_MANY_REQUESTS");
                break;// 페이지 로드중 너무 많은 요청 발생
            case WebViewClient.ERROR_UNKNOWN:
                Log.w("!ERROR_UNKNOWN");
                break;// 일반 오류
            case WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME:
                Log.w("!ERROR_UNSUPPORTED_AUTH_SCHEME");
                break;// 지원되지 않는 인증 체계
            case WebViewClient.ERROR_UNSUPPORTED_SCHEME:
                Log.w("!ERROR_UNSUPPORTED_SCHEME");
                break;// ?
            case WebViewClient.ERROR_TIMEOUT:
                Log.w("!ERROR_TIMEOUT");
                break;// 연결 시간 초과
            default:
                Log.w("OK");
                break;// ?
            //@formatter:on
        }
    }

    public void onPageStarted(WebView view, String url, Bitmap favicon) {
//        Log.pm(Log.ERROR, "onPageStarted", ">WEB:S>", view.getTitle(), url);
        if (!(getContext() instanceof CActivity))
            return;
        CActivity ba = (CActivity) getContext();
        ba.showProgress();
    }

    public void onPageFinished(WebView view, String url) {
//        Log.pm(Log.WARN, "onPageFinished", ">WEB:E>", view.getTitle(), url);
        if (!(getContext() instanceof CActivity))
            return;
        CActivity ba = (CActivity) getContext();
        ba.dismissProgress();
    }

    public void onLoadResource(WebView view, String url) {
//		Log.v(url);
    }

    /////////////////////////////////////////////////////////////////////////
    public void onProgressChanged(WebView view, int newProgress) {
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
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        Log.e(view.getTitle(), url);
        Log.e(message);

        AlertDialog dlg = new AlertDialog.Builder(view.getContext())
                .setMessage(message)
                .setPositiveButton("확인", (dialog, which) -> result.confirm())
                .create();
        dlg.setOnCancelListener(dialog -> result.cancel());

        return true;
    }

    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        Log.e(view.getTitle(), url);
        Log.e(message);

        if (!(view.getContext() instanceof CActivity))
            return false;
        CActivity ba = (CActivity) view.getContext();
        AlertDialog dlg = new AlertDialog.Builder(view.getContext())
                .setMessage(message)
                .setPositiveButton("확인", (dialog, which) -> result.confirm())
                .setNegativeButton("취소", (dialog, which) -> result.cancel())
                .create();
        dlg.setOnCancelListener(dialog -> result.cancel());
        return true;
    }

    private boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(defaultValue);
        AlertDialog dlg = new AlertDialog.Builder(view.getContext())
                .setView(input)
                .setMessage(message)
                .setPositiveButton("확인", (dialog, which) -> result.confirm(input.getText().toString()))
                .setNegativeButton("취소", (dialog, which) -> result.cancel())
                .create();
        dlg.setOnCancelListener(dialog -> result.cancel());
        dlg.show();
        return true;
    }

    public void onReceivedTitle(WebView view, String title) {
//        if (!(view.getContext() instanceof Activity))
//            ((Activity)view.getContext()).setTitle(title);
    }

    private void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
    }

    private boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        //			Log.e(consoleMessage.message());
        //^(.*)    at (\S+) (\[.+\] |)\([\w:/.-]*?([\w-]+\.\w+:\d+|<anonymous>:\d+):\d+\)
//        String regularExpression = "^(.*)    at (\\S+) (?:\\[.+\\] |)\\([\\w:/.-]*?([\\w-]+\\.\\w+:\\d+|<anonymous>:\\d+):\\d+\\)";
        final String log = consoleMessage.message();
        final String tag = consoleMessage.sourceId() + "#" + consoleMessage.lineNumber();
        final ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();

//        int priority;
//        switch (level) {
//            case ERROR   : priority = Log.ERROR; break;
//            case WARNING : priority = Log.WARN ; break;
//            case DEBUG   : priority = Log.DEBUG; break;
//            default      : priority = Log.INFO ; break;
//        }
//        Log.p(priority, this, log, tag);
        consoleLog(log + "::" + tag);
        return true;
    }

    //@formatter:off
    public interface OnWebSettingsListener {
        void onWebSettings(WebSettings settings);
    }

    public interface OnWebViewClientListener {
        void onWebViewClient(WebView webview);
    }

    public interface OnWebChromeClientListener {
        void onWebChromeClient(WebView webview);
    }

    public interface OnAddJavascriptListener {
        void onAddJavascript(WebView webview);
    }

    public void setOnWebSettingsListener(OnWebSettingsListener onWebSettingsListener) {
        mOnWebSettingsListener = onWebSettingsListener;
    }

    public void setOnWebViewClientListener(OnWebViewClientListener onWebViewClientListener) {
        mOnWebViewClientListener = onWebViewClientListener;
    }

    public void setOnWebChromeClientListener(OnWebChromeClientListener onWebChromeClientListener) {
        mOnWebChromeClientListener = onWebChromeClientListener;
    }

    public void setOnAddJavascriptListener(OnAddJavascriptListener onAddJavascriptListener) {
        mOnAddJavascriptListener = onAddJavascriptListener;
    }

    private OnWebSettingsListener mOnWebSettingsListener;
    private OnWebViewClientListener mOnWebViewClientListener;
    private OnWebChromeClientListener mOnWebChromeClientListener;
    private OnAddJavascriptListener mOnAddJavascriptListener;

    //@formatter:off
    public void setShouldOverrideUrlLoading(ShouldOverrideUrlLoading shouldOverrideUrlLoading) {
        mShouldOverrideUrlLoading = shouldOverrideUrlLoading;
    }

    public void setOnLoadResource(OnLoadResource onLoadResource) {
        mOnLoadResource = onLoadResource;
    }

    public void setOnPageStarted(OnPageStarted onPageStarted) {
        mOnPageStarted = onPageStarted;
    }

    public void setOnPageFinished(OnPageFinished onPageFinished) {
        mOnPageFinished = onPageFinished;
    }

    public void setOnReceivedSslError(OnReceivedSslError onReceivedSslError) {
        mOnReceivedSslError = onReceivedSslError;
    }

//    public void setOnReceivedError(OnReceivedError onReceivedError) {
//        mOnReceivedError = onReceivedError;
//    }

    public void setOnConsoleMessage(OnConsoleMessage onConsoleMessage) {
        mOnConsoleMessage = onConsoleMessage;
    }

    public void setOnProgressChanged(OnProgressChanged onProgressChanged) {
        mOnProgressChanged = onProgressChanged;
    }

    public void setOnReceivedTitle(OnReceivedTitle onReceivedTitle) {
        mOnReceivedTitle = onReceivedTitle;
    }

    public void setOnJsAlert(OnJsAlert onJsAlert) {
        mOnJsAlert = onJsAlert;
    }

    public void setOnJsConfirm(OnJsConfirm onJsConfirm) {
        mOnJsConfirm = onJsConfirm;
    }

    public void setOnJsPrompt(OnJsPrompt onJsPrompt) {
        mOnJsPrompt = onJsPrompt;
    }

    public interface ShouldOverrideUrlLoading {
        boolean shouldOverrideUrlLoading(WebView view, String url);
    }

    public interface OnLoadResource {
        void onLoadResource(WebView view, String url);
    }

    public interface OnPageStarted {
        void onPageStarted(WebView view, String url, Bitmap favicon);
    }

    public interface OnPageFinished {
        void onPageFinished(WebView view, String url);
    }

    public interface OnReceivedSslError {
        void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error);
    }

//    public interface OnReceivedError {
//        void onReceivedError(WebView view, int errorCode, String description, String failingUrl);
//    }

    public interface OnConsoleMessage {
        boolean onConsoleMessage(ConsoleMessage consoleMessage);
    }

    public interface OnProgressChanged {
        void onProgressChanged(WebView view, int newProgress);
    }

    public interface OnReceivedTitle {
        void onReceivedTitle(WebView view, String title);
    }

    public interface OnJsAlert {
        boolean onJsAlert(WebView view, String url, String message, JsResult result);
    }

    public interface OnJsConfirm {
        boolean onJsConfirm(WebView view, String url, String message, JsResult result);
    }

    public interface OnJsPrompt {
        boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result);
    }

    private ShouldOverrideUrlLoading mShouldOverrideUrlLoading;
    private OnLoadResource mOnLoadResource;
    private OnPageStarted mOnPageStarted;
    private OnPageFinished mOnPageFinished;
    private OnReceivedSslError mOnReceivedSslError;
    //    private OnReceivedError mOnReceivedError;
    private OnConsoleMessage mOnConsoleMessage;
    private OnProgressChanged mOnProgressChanged;
    private OnReceivedTitle mOnReceivedTitle;
    private OnJsAlert mOnJsAlert;
    private OnJsConfirm mOnJsConfirm;
    private OnJsPrompt mOnJsPrompt;

    public static final String EXTRA_URL = "url";
    //@formatter:on

    public class BWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (mShouldOverrideUrlLoading != null)
                return mShouldOverrideUrlLoading.shouldOverrideUrlLoading(view, url);
            else
                return BWebView.this.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            if (mOnLoadResource != null)
                mOnLoadResource.onLoadResource(view, url);
            else
                BWebView.this.onLoadResource(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (mOnPageStarted != null)
                mOnPageStarted.onPageStarted(view, url, favicon);
            else
                BWebView.this.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mOnPageFinished != null)
                mOnPageFinished.onPageFinished(view, url);
            else
                BWebView.this.onPageFinished(view, url);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (mOnReceivedSslError != null)
                mOnReceivedSslError.onReceivedSslError(view, handler, error);
            else
                BWebView.this.onReceivedSslError(view, handler, error);
        }

//        @Override
//        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//            if (mOnReceivedError != null)
//                mOnReceivedError.onReceivedError(view, errorCode, description, failingUrl);
//            else
//                BWebView.this.onReceivedError(view, errorCode, description, failingUrl);
//        }
    }

    public class BChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            if (mOnConsoleMessage != null)
                return mOnConsoleMessage.onConsoleMessage(consoleMessage);
            else
                return BWebView.this.onConsoleMessage(consoleMessage);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (mOnProgressChanged != null)
                mOnProgressChanged.onProgressChanged(view, newProgress);
            else
                BWebView.this.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (mOnReceivedTitle != null)
                mOnReceivedTitle.onReceivedTitle(view, title);
            else
                BWebView.this.onReceivedTitle(view, title);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            if (mOnJsAlert != null)
                return mOnJsAlert.onJsAlert(view, url, message, result);
            else
                return BWebView.this.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            if (mOnJsConfirm != null)
                return mOnJsConfirm.onJsConfirm(view, url, message, result);
            else
                return BWebView.this.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            if (mOnJsPrompt != null)
                return mOnJsPrompt.onJsPrompt(view, url, message, defaultValue, result);
            else
                return BWebView.this.onJsPrompt(view, url, message, defaultValue, result);
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //etc////////////////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            return BWebView.this.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            BWebView.this.onGeolocationPermissionsShowPrompt(origin, callback);
        }
    }

    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        //https://play.google.com/apps/publish/?account=8841149513553108353#AndroidMetricsErrorsPlace:p=com.kebhana.hanapush&appid=4976086679178587985&appVersion=PRODUCTION&clusterName=apps/com.kebhana.hanapush/clusters/bffa9a37&detailsAppVersion=PRODUCTION&detailsSpan=7
        callback.invoke(origin, false, false);
//        if (getContext() instanceof BActivity) {
//            BActivity ba = (BActivity) getContext();
//            ba.showDialog(origin + " 에서 위치정보를 사용하려 합니다." //
//                    , "승인", (dialog, which) -> callback.invoke(origin, true, true)//
//                    , "이번만", (dialog, which) -> callback.invoke(origin, true, false)//
//                    , "불가", (dialog, which) -> callback.invoke(origin, false, false)//
//            );
//        }
    }

    private boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        Log.e(view, isDialog, isUserGesture, resultMsg);
        WebView newWebView = new WebView(view.getContext());
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(newWebView);
        resultMsg.sendToTarget();
        return true;
    }

    static Rect rect = new Rect();
    static Paint paint = new Paint();

    static {
        paint.setColor(0x55ff0000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(KtxTextKt.getDp(8));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.getDrawingRect(rect);
        canvas.drawRect(rect, paint);
    }
}

