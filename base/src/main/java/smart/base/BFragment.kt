package smart.base

import android.base.CFragment
import android.log.Log
import dev.eastar.operaxinterceptor.event.OperaXEventObserver
import java.util.*

abstract class BFragment : CFragment(), OperaXEventObserver {
    override fun update(observable: Observable?, data: Any?) {
        Log.e(observable, data)
    }
}