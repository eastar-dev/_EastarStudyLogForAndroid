package smart.base

import android.annotation.TargetApi
import android.base.CActivity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.log.Log
import android.os.Build
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import dev.eastar.base.R
import dev.eastar.operaxinterceptor.event.OperaXEventObservable
import dev.eastar.operaxinterceptor.event.OperaXEventObserver
import dev.eastar.operaxinterceptor.event.OperaXEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import smart.auth.AA
import java.util.*

abstract class BActivity : CActivity(), OperaXEventObserver {

    fun login() {}

    override fun update(observable: Observable?, data: Any?) {
        Log.e(observable, data)
        if (OperaXEventObservable == observable) {
            when (data) {
                OperaXEvents.Exited -> {
                    AA.logout()
                    finish()
                }
            }
        }
    }

    override fun onTitleChanged(title: CharSequence?, color: Int) {
        super.onTitleChanged(title, color)
        setHeaderTitle(title.toString())
    }

    open fun setHeaderTitle(headerTitle: String?) = reload()
    override fun onHeaderBack(v: View) = onBackPressed()
    override fun onHeaderClose(v: View) = onBackPressed()
    override fun onHeaderMain(v: View)  = main()
    override fun exit() {
        super.exit()
        OperaXEventObservable.notify(OperaXEvents.Exited)
    }

    override fun createProgress(): AppCompatDialog {
        val context = mContext

        return AppCompatDialog(context, android.R.style.Theme_DeviceDefault_Dialog).apply {
            window?.setBackgroundDrawable(ColorDrawable(0x00ff0000))
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(window?.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            lp.gravity = Gravity.CENTER

            window?.attributes = lp

//            setContentView(R.layout.loading_dialog)
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }
}