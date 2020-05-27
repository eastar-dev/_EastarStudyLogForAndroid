package smart.net

//import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
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
            addConverterFactory(ScalarsConverterFactory.create())      // Respone String
            client(okHttpClient)
        }.build()
            //addCallAdapterFactory(RxJava2CallAdapterFactory.create())  // RxJava Adapter
    }

    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}