@file:Suppress("SpellCheckingInspection")

package smart.auth

import android.content.Context
import android.log.Log
import android.webkit.CookieManager
import com.crashlytics.android.Crashlytics
import dev.eastar.operaxinterceptor.event.OperaXEventObservable
import dev.eastar.operaxinterceptor.event.OperaXEvents

object AA {

    fun login(context: Context, json: String) {
        Log.i("login", json)
        Crashlytics.setUserIdentifier("custNo")
        Crashlytics.setString("current_level", "userType.name")
        Crashlytics.setString("last_UI_action", "logged_in")
        Session.startSessionTimeout()
        OperaXEventObservable.notify(OperaXEvents.Logined)
    }

    fun logout() {
        Crashlytics.setString("last_UI_action", "logged_out")
        Log.w("logout")
        removeSessionCookie()
        Session.stopSessionTimeout()
        OperaXEventObservable.notify(OperaXEvents.Logouted)
    }

    private fun removeSessionCookie() {
//        Log.e("<<SS:removeSessionCookie")
        CookieManager.getInstance().removeSessionCookies { Unit }
        CookieManager.getInstance().removeAllCookies { Unit }
        CookieManager.getInstance().flush()
    }

    fun isLogin(): Boolean = info != null

    var info: Info? = null
    data class Info(val userId: String?)
}
