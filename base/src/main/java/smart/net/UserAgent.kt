package smart.net

import android.content.Context
import android.log.Log
import android.os.Build.BRAND
import android.os.Build.MODEL
import android.os.Build.VERSION.RELEASE
import android.util.BV
import android.webkit.WebSettings
import com.google.gson.Gson
import dev.eastar.ktx.appName
import dev.eastar.ktx.networkOperatorName
import dev.eastar.ktx.versionName
import smart.base.PP

object UserAgent {
    fun create(context: Context) {
        USER_AGENT = Gson().toJson(
            mapOf(
                "platform" to "Android",
                "brand" to BRAND,
                "model" to MODEL,
                "version" to RELEASE,
                "deviceId" to PP.androidId,
                "phoneNumber" to "",
                "countryIso" to "",
                "telecom" to context.networkOperatorName,
                "simSerialNumber" to "",
                "subscriberId" to "",
                "appVersion" to context.versionName,
                "phoneName" to "",
                "appName" to context.appName,
                "deviceWidth" to "context.WIDTH_PIXELS",
                "deviceHeight" to "context.HEIGHT_PIXELS",
                "uid" to PP.androidId,
                "hUid" to PP.appUuid,
                "etcStr" to "",
                "User-Agent" to kotlin.runCatching { WebSettings.getDefaultUserAgent(context) }.getOrDefault("")
            )
        )
        Log.w(USER_AGENT)
    }

    const val userAgent: String = "User-Agent"
    lateinit var USER_AGENT: String
}