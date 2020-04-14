package smart.net

import android.log.Log
import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl


class NetCookie : CookieJar {
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach {
            Log.e(it)
            CookieManager.getInstance().setCookie(url.toString(), it.toString())
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return CookieManager.getInstance().getCookie(url.toString())?.let { it ->
            it.split("[,;]".toRegex())
                .dropLastWhile { it.isEmpty() }
                .map { Cookie.parse(url, it.trim())!! }
                .toList()
//                .also { it.forEach { cookie -> Log.w(cookie) } }
        } ?: emptyList()
    }
}