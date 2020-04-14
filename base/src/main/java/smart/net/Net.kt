package smart.net

//import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.converter.scalars.ScalarsConverterFactory
import smart.base.NN
import java.util.concurrent.TimeUnit

class NNN

object Net {
    private const val connectTimeout: Long = 10                // ConnectTimeout Default 180
    private const val writeTimeout: Long = 300                 // WriteTimeout Default 180
    private const val readTimeout: Long = 300                  // ReadTimeout Default 180
    val okHttpClient by lazy {
        okHttpClientBuilder().apply {
            connectTimeout(connectTimeout, TimeUnit.SECONDS)
            writeTimeout(writeTimeout, TimeUnit.SECONDS)
            readTimeout(readTimeout, TimeUnit.SECONDS)
            cookieJar(NetCookie())
        }.build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder().apply {
            baseUrl(NN.HOST) // BaseUrl 설정
            addConverterFactory(GsonConverterFactory.create())         // Respone Gson
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())  // RxJava Adapter
            client(okHttpClient)
        }.build()
//            addConverterFactory(ScalarsConverterFactory.create())      // Respone String
    }

    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}


//            when (it) {
//                is NetException -> when (it.header.errorCode) {
//                    서버세션오류코드2,
//                    서버세션오류코드 -> Log.e(it.header.errorMessage)
//                    else -> Log.w("통신중 장애가 발생하였습니다.(${it.javaClass.simpleName}${it.message})")
//                }
//                is SocketTimeoutException -> Log.w("시스템 오류가 발생하였습니다.\n지속적으로 해당 오류가 발생하시면, 내용 확인을 위해 콜센터로 문의하여 주십시오.")
//                is UnknownHostException -> Log.w("모바일 또는 Wi-Fi 네트워크를 확인하세요.")
//                is SSLException -> Log.w("서비스 요청 중 오류가 발생했습니다.\n다시 시도해 주세요.")
//                else -> Log.w("통신중 장애가 발생하였습니다.(${it.javaClass.simpleName}${it.message})")


//    private var mLastActivityWeakReference: WeakReference<BActivity>? = null
//
//    fun error(message: String) {
//        Log.i("error")
//        if (mLastActivityWeakReference == null)
//            return
//        val sm = mLastActivityWeakReference?.get()
//        if (sm == null || sm.lifecycle.currentState == Lifecycle.State.DESTROYED)
//            return
//        sm.runOnUiThread {
//            CustomDialog.showOkDialog(sm, null, message)
//        }
//    }
//
//    fun sessionError(message: String) {
//        Log.i("error")
//        if (mLastActivityWeakReference == null)
//            return
//        val sm = mLastActivityWeakReference?.get()
//        if (sm == null || sm.lifecycle.currentState == Lifecycle.State.DESTROYED)
//            return
//        sm.runOnUiThread { sm.sessionError(message) }
//    }
//
//    fun timeOutError() {
//        Log.i("timeOutError")
//        if (mLastActivityWeakReference == null)
//            return
//        val sm = mLastActivityWeakReference?.get()
//        if (sm == null || sm.lifecycle.currentState == Lifecycle.State.DESTROYED)
//            return
//        sm.runOnUiThread { sm.timeoutError() }
//    }
//
//    fun networkError() {
//        Log.i("networkError")
//        if (mLastActivityWeakReference == null)
//            return
//        val sm = mLastActivityWeakReference?.get()
//        if (sm == null || sm.lifecycle.currentState == Lifecycle.State.DESTROYED)
//            return
//        sm.runOnUiThread { sm.networkError() }
//    }
//
//    fun sslError() {
//        Log.i("sslError")
//        if (mLastActivityWeakReference == null)
//            return
//        val sm = mLastActivityWeakReference?.get()
//        if (sm == null || sm.lifecycle.currentState == Lifecycle.State.DESTROYED)
//            return
//        sm.runOnUiThread { sm.sslError() }
//    }
//
//    fun setLastActivity(lastActivityWeakReference: WeakReference<BActivity>) {
//        mLastActivityWeakReference = lastActivityWeakReference
//    }
//
//    fun checkServerSession(code: String): Boolean =
//        code == 서버세션오류코드 || code == 서버세션오류코드2
//
//    fun isPublicKeyError(code: String): Boolean = code == NFILTER오류

//    fun getPublicKey(
//        _isProgress: MutableLiveData<Boolean>,
//        success: () -> Unit,
//        error: (e: Throwable) -> Unit
//    ) {
//        net.retrofit.create(CommonDataSource::class.java)
//            .getPublicKey()
//            .checkStatus()
//            .progress()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : SingleObserver<NFilterData> {
//                override fun onSuccess(nFilterData: NFilterData) {
//                    // TODO - public key
////                        VV.PATTERN_PUBLIC_KEY = nFilterData.publicKey
////                        VV.NFILTER_PUBLIC_KEY = nFilterData.publicKey
//                    success.invoke()
//                }
//
//                override fun onSubscribe(d: Disposable) {
//                    Log.i("subscribe")
//                }
//
//                override fun onError(e: Throwable) {
//                    Log.e("onError : $e")
//                    error.invoke(e)
//                }
//            })
//    }
//
//    companion object {
//
//        private var INSTANCE: ServerError? = null
//
//        const val 서버세션오류코드 = "FRU0001"
//        const val 서버세션오류코드2 = "FRU0002"
//
//        const val NFILTER오류 = "PSNBNFILTER_1"
//
//        @JvmStatic
//        fun getInstance(): ServerError {
//            if (INSTANCE == null) {
//                INSTANCE = ServerError()
//            }
//            return INSTANCE!!
//        }
//    }