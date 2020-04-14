package smart.base

import android.annotation.SuppressLint
import android.base.CApplication
import android.base.easterEgg
import android.content.Context
import android.log.logActivity


@SuppressLint("Registered")
abstract class BDApplication : CApplication() {
    override fun attachBaseContext(base: Context?) {
        DD.attachBaseContext()
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        if (isMainProcess) {
            easterEgg()
            logActivity()
            DD.onCreate(applicationContext)
        }
    }
}