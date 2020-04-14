package android.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.log.Log
import android.log.LogActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable

abstract class CActivity : LogActivity() {
    val mContext by lazy { this }
    val mActivity by lazy { this }
    //val mDecor: View by lazy { window.decorView }

    val progress by lazy { createProgress() }

    @SuppressLint("SourceLockedOrientationActivity")
    protected open fun setRequestedOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
    }

    override fun setRequestedOrientation(requestedOrientation: Int) {
        kotlin.runCatching { super.setRequestedOrientation(requestedOrientation) }
    }

    protected open fun setSoftInputMode() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    /** onParseExtra() -> onLoadOnce() -> onReload() -> onClear() -> onLoad() -> onUpdateUI() */
    override fun onCreate(savedInstanceState: Bundle?) {
        setRequestedOrientation()
        setSoftInputMode()
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun autoDisposable() {
                disposables.forEach { it.dispose() }
            }
        })
        parseExtra()
    }

    var disposables = mutableSetOf<Disposable>()
    fun Disposable.autoDispose() = disposables.add(this)

    /** onParseExtra() -> onReload() -> onClear() -> onLoad() -> onUpdateUI() */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseExtra()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        loadOnce()
        reload()
        updateUI()
    }

    fun parseExtra() = kotlin.runCatching { onParseExtra() }.onFailure { Log.printStackTrace(it) }.let { Unit }
    fun loadOnce() = onLoadOnce()
    fun updateUI() = kotlin.runCatching { onUpdateUI() }.onFailure { Log.printStackTrace(it) }.let { Unit }
    fun clear() = kotlin.runCatching { onClear() }.onFailure { Log.printStackTrace(it) }.let { Unit }
    fun reload() {
        clear()
        load()
    }

    protected var mIsLoading = false
    protected fun load() {
        if (mIsLoading) return
        if (lifecycle.currentState === Lifecycle.State.DESTROYED) return
        if (isFinishing) return
        kotlin.runCatching { onLoad() }.onFailure { Log.printStackTrace(it) }
    }

    protected open fun onParseExtra() = Log.d()
    protected open fun onLoadOnce() = Log.d()
    protected open fun onClear() = Log.d()
    protected open fun onLoad() = Log.d()
    protected open fun onUpdateUI() = Log.d()

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    protected open fun createProgress(): AppCompatDialog {
        val builder = AlertDialog.Builder(mContext)
        builder.setView(ProgressBar(mContext, null, android.R.attr.progressBarStyleLarge))
        val dlg = builder.create()
        dlg.window!!.setBackgroundDrawable(ColorDrawable(0x00ff0000))
        dlg.setCanceledOnTouchOutside(false)
        dlg.setCancelable(true)
        return dlg
    }

    open fun showProgress() {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) return
        if (isFinishing) return
        if (progress.isShowing) return
        progress.show()
    }

    open fun dismissProgress() {
        if (progress.isShowing) progress.dismiss()
    }

    open fun dismissProgressForce() = dismissProgress()

    open fun exit() {
        Log.e(javaClass, "public void exit()")
        finish()
    }

    open fun main() {
        Log.e(javaClass, "public void main()")
    }

    /**OnClick*/
    protected open fun onBackPressedEx(): Boolean {
        for (fragment in supportFragmentManager.fragments) {
            if (fragment is CFragment) {
                if (fragment.onBackPressedEx())
                    return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        if (onBackPressedEx())
            return
        super.onBackPressed()
    }

    open fun onHeaderTitle(v: View) {
        Log.d(javaClass, "onHeaderTitle")
    }

    open fun onHeaderLogin(v: View) {
        Log.d(javaClass, "onHeaderLogin")
    }

    open fun onHeaderBack(v: View) {
        Log.d(javaClass, "onHeaderBack")
        onBackPressed()
    }

    open fun onHeaderClose(v: View) {
        Log.d(javaClass, "onHeaderClose")
        onBackPressed()
    }

    open fun onHeaderMain(v: View) {
        Log.d(javaClass, "onHeaderMain")
        main()
    }

    open fun onHeaderMenu(v: View) {
        Log.d(javaClass, "onHeaderMenu")
    }

    open fun onHeaderLeft(v: View) {
        Log.d(javaClass, "onHeaderLeft")
    }

    open fun onHeaderRight(v: View) {
        Log.d(javaClass, "onHeaderRight")
    }

    inline fun <reified T : Fragment> FragmentActivity.instantiate() = supportFragmentManager.fragmentFactory.instantiate(classLoader, T::class.java.name)

    fun Context.chooseActivities(prefix: String = javaClass.`package`!!.name) {
        val list = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).activities
        //list.forEach { Log.e(it.name) }

        val items = list
                .filter { it.name.startsWith(prefix) }
                .filterNot { it.name == javaClass.name }
                .map { it.name }
                .toTypedArray()
        //Log.e(items.contentToString())

        AlertDialog.Builder(this)
                .setItems(items) { dialog, which ->
                    kotlin.runCatching {
                        val item = (dialog as AlertDialog).listView.getItemAtPosition(which) as String
                        startActivity(Intent().setClassName(this, item))
                    }.onFailure { Log.printStackTrace(it) }
                }
                .show()
    }
}