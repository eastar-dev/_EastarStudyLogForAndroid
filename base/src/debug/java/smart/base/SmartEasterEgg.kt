@file:Suppress("NonAsciiCharacters", "FunctionName", "unused")

package smart.base

import android.app.Activity
import android.base.BN
import android.content.Intent
import android.widget.Toast

class SmartEasterEgg(val activity: Activity) {
    fun A_DEV_SERVER() {
        val prev = BN.getLastServer(activity).name
        NN.apply(activity, BN.DEV)
        toast("$prev->${BN.DEV.name}")
        restart()
    }

    fun A_STG_SERVER() {
        val prev = BN.getLastServer(activity).name
        NN.apply(activity, BN.STG)
        toast("$prev->${BN.STG.name}")
        restart()
    }

    fun A_REAL_SERVER() {
        val prev = BN.getLastServer(activity).name
        NN.apply(activity, BN.REAL)
        toast("$prev->${BN.REAL.name}")
        restart()
    }

    private fun toast(text: CharSequence) = Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
    private fun restart() = activity.finish().run { activity.startActivity(activity.packageManager.getLaunchIntentForPackage(activity.packageName)?.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)) }

}
