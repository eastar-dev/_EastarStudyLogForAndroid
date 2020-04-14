@file:Suppress("NonAsciiCharacters", "ObjectPropertyName", "SpellCheckingInspection")

package smart.base

import android.base.BN
import android.content.Context
import android.log.Log

/** 기본설정은 운영기준*/
object NN {

    @JvmField var HOST             = "https://smart.kebhana.com"
    @JvmField var Authorization    = "key=AAAA-X6hxEw:APA91bF4HfB1HH-xEZm92DEvGfptstiykL9SSYEZOnP4-1iCLTDLttpZQVPJH5rm5uednhRhAQIYSTeQON0joiEwYWTsfjqYO0-XsvXDvX4vRp-p1W7JvBrLymscG3mEBZUdDuycxxoT"


    @JvmStatic
    fun getUrl(path: String?): String {
        when {
            path.isNullOrBlank() -> Log.w("!앗 주소가 없다", path)
            path.startsWith("http", true) && path.startsWith(HOST) -> Log.w("!앗 주소가 다른서버다", path)
            path.startsWith("file://", true) -> Log.w("!앗 주소가 파일이다", path)
            else -> Unit
        }

        return when {
            path.isNullOrBlank() -> HOST
            path.startsWith("http", true) -> path
            path.startsWith("file://", true) -> path
            else -> HOST + path
        }
    }

    fun apply(context: Context, lastServer: BN) {
        when (lastServer) {
            BN.REAL -> {
                HOST             = "https://smart.kebhana.com"
                Authorization    = "key=AAAA-X6hxEw:APA91bF4HfB1HH-xEZm92DEvGfptstiykL9SSYEZOnP4-1iCLTDLttpZQVPJH5rm5uednhRhAQIYSTeQON0joiEwYWTsfjqYO0-XsvXDvX4vRp-p1W7JvBrLymscG3mEBZUdDuycxxoT"
            }
            BN.STG -> {
                HOST             = "https://stg11-smart.kebhana.com:18080"
                Authorization    = "key=AAAA-X6hxEw:APA91bF4HfB1HH-xEZm92DEvGfptstiykL9SSYEZOnP4-1iCLTDLttpZQVPJH5rm5uednhRhAQIYSTeQON0joiEwYWTsfjqYO0-XsvXDvX4vRp-p1W7JvBrLymscG3mEBZUdDuycxxoT"
            }
            BN.DEV -> {
                HOST             = "https://dev12-mbp.hanabank.com:18080"
                Authorization    = "key=AAAA-X6hxEw:APA91bF4HfB1HH-xEZm92DEvGfptstiykL9SSYEZOnP4-1iCLTDLttpZQVPJH5rm5uednhRhAQIYSTeQON0joiEwYWTsfjqYO0-XsvXDvX4vRp-p1W7JvBrLymscG3mEBZUdDuycxxoT"
            }
            BN.DEV2 -> {
                HOST             = "https://211.32.31.146:18080"
                Authorization    = "key=AAAA-X6hxEw:APA91bF4HfB1HH-xEZm92DEvGfptstiykL9SSYEZOnP4-1iCLTDLttpZQVPJH5rm5uednhRhAQIYSTeQON0joiEwYWTsfjqYO0-XsvXDvX4vRp-p1W7JvBrLymscG3mEBZUdDuycxxoT"
            }
        }
        BN.setLastServer(context, lastServer)
    }
}
