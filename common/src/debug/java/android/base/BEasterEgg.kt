@file:Suppress("NonAsciiCharacters", "NonAsciiCharacters")

package android.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.log.Log
import android.os.Bundle
import android.util.RU
import android.view.BWebView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import com.google.firebase.iid.FirebaseInstanceId
import dev.eastar.ktx.NoMore
import dev.eastar.ktx.getResId
import dev.eastar.ktx.startSetting

var systemWindowInsetTop: Int = 0

fun Application.easterEgg() = registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    //@formatter:off
    override fun onActivityStarted(activity: Activity?) { activity?.easterEgg() }
    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
    override fun onActivityDestroyed(activity: Activity?) {}
    override fun onActivityStopped(activity: Activity?) {}
    override fun onActivityPaused(activity: Activity?) {}
    override fun onActivityResumed(activity: Activity?) {}
    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
    //@formatter:on
})

@SuppressLint("SetTextI18n")
private fun Activity.easterEgg() {
    val versionTag = "show_me_the_money"
    val appEasterEgg = "android.etc.AppEasterEgg"
    val smartEasterEgg = "smart.base.SmartEasterEgg"
    val bEasterEgg = BEasterEgg::class.java.name

    val parent = findViewById<ViewGroup>(android.R.id.content)
    if (parent.findViewWithTag<View>(versionTag) != null)
        return
    val ver = TextView(this)
    ver.setOnApplyWindowInsetsListener { _, insets ->
        systemWindowInsetTop = insets.systemWindowInsetTop
        insets
    }
    ver.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    ver.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val locationInWindow = IntArray(2)
                    ver.getLocationInWindow(locationInWindow)

                    if (locationInWindow[1] == 0) {
                        Log.e(" ver.y", locationInWindow[1], "->", systemWindowInsetTop)
                        ver.y = systemWindowInsetTop.toFloat()
                    }
                }
            }
    )

    val server = BN.getLastServer(this)
    val versionCode = PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(packageName, 0))
    val versionName = packageManager.getPackageInfo(packageName, 0).versionName

    ver.text = "[$versionCode][$server][v$versionName]"
    ver.tag = versionTag
    ver.setTextColor(0x55ff0000)
    ver.setBackgroundColor(0x5500ff00)
    ver.textSize = 14f//sp
    val height = -2// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35f, resources.displayMetrics).toInt()
    parent.addView(ver, ViewGroup.LayoutParams.WRAP_CONTENT, height)

    val appFunc = getFunc(appEasterEgg)
    val smartFunc = getFunc(smartEasterEgg)
    val baseFunc = getFunc(bEasterEgg)
    val func = (appFunc + smartFunc + baseFunc).toTypedArray()

    ver.setOnClickListener {
        AlertDialog.Builder(this)
                .setItems(func) { dialog, which ->
                    val funcName = (dialog as AlertDialog).listView.getItemAtPosition(which) as String
                    if (runFunc(appEasterEgg, funcName, this)) return@setItems
                    if (runFunc(smartEasterEgg, funcName, this)) return@setItems
                    if (runFunc(bEasterEgg, funcName, this)) return@setItems
                }
                .show()
    }
}

private fun getFunc(clz: String): List<String> {
    return try {
        Class.forName(clz).run {
            methods.filter { it.declaringClass == this }
                    .filter { it.returnType == Void.TYPE }
                    .filterNot { it.name.contains("$") }
                    .map { it.name }
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun runFunc(clz: String, funcName: String, activity: Activity) = runCatching {
    val clazz = Class.forName(clz)
    val method = clazz.getMethod(funcName)
    val constructor = clazz.getConstructor(Activity::class.java)
    val receiver = constructor.newInstance(activity)
    method.invoke(receiver)
    true
}.getOrDefault(false)

@Suppress("SpellCheckingInspection", "FunctionName", "unused")
class BEasterEgg(val activity: Activity) {

    fun APPLICATION_DETAILS_SETTINGS() {
        setting()
    }

    fun APP_TEST() {
        RU.invokeDeclared(activity, "test")
    }

    fun PUSH_TOKEN() {
        activity.runCatching {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener(this) { instanceIdResult ->
                val token = instanceIdResult.token
                val clipboardManager = getSystemService<ClipboardManager>()
                val clip = ClipData.newPlainText("label", token)
                clipboardManager?.setPrimaryClip(clip)
                toast(clip.getItemAt(0).text.toString() + " 복사되었습니다.")
            }
        }
    }

    fun 다시_보지않음_초기화() {
        NoMore.clear(activity)
    }

    fun _MAIN() {
        (activity as CActivity).main()
    }

    fun 소스보기() {
        val resId = activity.getResId("webview", "id", activity.packageName)
        val webview = activity.findViewById<BWebView>(resId)
        webview.toggleSource()
    }

    fun 콘솔보기() {
        val resId = activity.getResId("webview", "id", activity.packageName)
        val webview = activity.findViewById<BWebView>(resId)
        webview.toggleConsoleLog()
    }

    fun AllActivity() {
        val list = activity.packageManager.getPackageInfo(activity.packageName, PackageManager.GET_ACTIVITIES).activities
        val items = list
                .filter { it.name.startsWith("com.hana.") || it.name.startsWith("com.opera.") }
                .filterNot { it.name == javaClass.name }
                .map { it.name }
                .toTypedArray()

        AlertDialog.Builder(activity)
                .setItems(items) { dialog, which ->
                    try {
                        val item = (dialog as AlertDialog).listView.getItemAtPosition(which) as String
                        activity.startActivity(Intent().setClassName(activity, item))
                    } catch (e: Exception) {
                        Log.printStackTrace(e)
                    }
                }
                .show()
    }

    private fun toast(text: CharSequence) = Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
    private fun restart() = activity.finish().run { activity.startActivity(activity.packageManager.getLaunchIntentForPackage(activity.packageName)?.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)) }
    private fun setting() = activity.startSetting()
}
