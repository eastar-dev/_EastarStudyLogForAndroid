@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package smart.base

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.base.BD
import android.base.BN
import android.content.Context
import android.content.pm.PackageManager
import android.log.Log
import android.util.SDF
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import dev.eastar.operaxwebextansion.OperaXLog
import java.io.File
import java.security.MessageDigest

object DD {
    fun attachBaseContext() {
        Log.LOG = true
        BD.DEVELOP = false
        BD.PASS = false
        OperaXLog.LOG = true
        OperaXLog._IN_1 = true
        OperaXLog._OUT_1 = true

    }

    fun onCreate(context: Context) {
        BD.CONTEXT = context
        NN.apply(context, BN.getLastServer(context))
        Log.FILE_LOG = File(context.getExternalFilesDir(null), SDF.yyyymmdd.now() + "_log.log")
        OperaXLog.FILE_LOG = File(context.getExternalFilesDir(null), SDF.yyyymmdd.now() + "_log.log")
        logInfo(context)
        displayInfo(context)
        setWebContentsDebuggingEnabled()
        uncaughtExceptionHandler()
        //Session.addRef()
//        hashkeyForDaumMap(context)
//        sacnFolder(context.filesDir.parentFile, "")
//        sacnFolder(File(context.getDatabasePath("0000").parent), "")
//        uncaughtExceptionHandler(context)
//        setNotification(context)
//        Pref.CREATE(context)
//        Log.e(Pref.get(Pref.__PUSH_TOKEN))
    }

    //@SuppressLint("WebViewApiAvailability")
    private fun logInfo(context: Context) {
        Log.e("Log.LOG       ", Log.LOG)
        Log.e("Log.FILE_LOG  ", Log.FILE_LOG)
        Log.e("OperaXLog.LOG ", OperaXLog.LOG)
        Log.e("OperaXLog.FILE", OperaXLog.FILE_LOG)

        Log.e("BD.DEVELOP    ", BD.DEVELOP)
        Log.e("BD.PASS       ", BD.PASS)

        Log.e("SERVER        ", BN.getLastServer(context))
//        Log.w("WebView version: " + WebViewCompat.getCurrentWebViewPackage(context.applicationContext)?.versionName)
    }

    private fun displayInfo(context: Context) {
        with(context.resources.displayMetrics) {
            Log.e("DENSITYDPI    ", densityDpi)
            Log.e("DENSITY       ", density)
            Log.e("WIDTHPIXELS   ", widthPixels)
            Log.e("HEIGHTPIXELS  ", heightPixels)
            Log.e("SCALEDDENSITY ", scaledDensity)
        }
    }

    private fun uncaughtExceptionHandler() {
        val def = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.flog(android.util.Log.getStackTraceString(throwable))
            def?.uncaughtException(thread, throwable)
        }
    }

    fun scanFolder(dir: File?, prefix: String) {
        dir ?: return
        if (!dir.isDirectory) {
            Log.i(prefix, dir.absolutePath, dir.length(), dir.exists())
            return
        }

        Log.e(prefix, dir.absolutePath)

        dir.listFiles()?.forEach {
            if (!it.isDirectory) {
                Log.i(prefix, it.absolutePath, it.length())
            }
        }

        dir.listFiles()?.forEach {
            if (it.isDirectory) {
                scanFolder(it, "$prefix��")
            }
        }
    }

    fun setNotification(context: Context) {
        try {
            val pm = context.packageManager
            val applicationInfo = pm.getApplicationInfo(context.packageName, 0)
            val name = pm.getApplicationLabel(applicationInfo).toString()

            val notificationBuilder = NotificationCompat.Builder(context, "COMMON")
                    .setSmallIcon(android.R.drawable.ic_notification_overlay)
                    .setAutoCancel(false)
                    .setContentTitle(name)
                    .setContentText("$name ������")
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(0 /* ID of notification */, notificationBuilder.build())
        } catch (e: Exception) {
            Log.printStackTrace(e)
        }

    }

    private fun setWebContentsDebuggingEnabled() {
        runCatching { WebView.setWebContentsDebuggingEnabled(true) }.onFailure { it.printStackTrace() }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("PackageManagerGetSignatures")
    fun hashkeyForDaumMap(context: Context) {
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        for (signature in info.signatures) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            Log.d(context.packageName, android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP))
        }
    }
}
