package smart.base

import android.base.CActivity
import android.graphics.drawable.ColorDrawable
import android.log.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import dev.eastar.operaxinterceptor.event.OperaXEventObservable
import dev.eastar.operaxinterceptor.event.OperaXEventObserver
import dev.eastar.operaxinterceptor.event.OperaXEvents
import smart.auth.AA
import java.util.*

abstract class BActivity : CActivity(), OperaXEventObserver {

    open fun login() {}

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
    override fun onHeaderMain(v: View) = main()
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