package android.base

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.log.Log
import android.os.Process
import android.util.BV
import androidx.core.content.getSystemService

abstract class CApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        Log.e("=============================================================================")
        Log.e("=== 시작됨 ==================================================================")
        Log.e("=============================================================================")
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        if (isMainProcess) {
            BV.create(this)
        }
    }

    val Application.isMainProcess: Boolean get() = Process.myPid().let { pid -> getSystemService<ActivityManager>()?.runningAppProcesses?.firstOrNull { pid == it.pid }?.processName == packageName }
}

