package android.base

import android.log.Log
import android.log.LogFragment
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable

/**
 * @author r
 */
abstract class CFragment : LogFragment() {
    val mContext by lazy { requireContext() }
    val mActivity by lazy { requireActivity() as CActivity }
    val intent by lazy { requireActivity().intent }

    var disposables = mutableSetOf<Disposable>()
    fun Disposable.autoDispose() = disposables.add(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun autoDisposable() {
                disposables.forEach { if (!it.isDisposed) it.dispose() }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parseExtra()
        loadOnce()
        reload()
        updateUI()
    }

    fun parseExtra() =
        kotlin.runCatching { onParseExtra() }.onFailure { Log.printStackTrace(it) }.let { Unit }

    fun loadOnce() = onLoadOnce()
    fun updateUI() =
        kotlin.runCatching { onUpdateUI() }.onFailure { Log.printStackTrace(it) }.let { Unit }

    fun clear() =
        kotlin.runCatching { onClear() }.onFailure { Log.printStackTrace(it) }.let { Unit }

    fun reload() {
        clear()
        load()
    }

    var mIsLoading = false
    protected fun load() {
        if (mIsLoading) return
        if (lifecycle.currentState === Lifecycle.State.DESTROYED) return
        kotlin.runCatching { onLoad() }.onFailure { Log.printStackTrace(it) }
    }

    protected open fun onParseExtra() {}
    protected open fun onLoadOnce() {}
    protected open fun onClear() {}
    protected open fun onLoad() {}
    protected open fun onUpdateUI() {}

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    fun showProgress() {
        kotlin.runCatching { (activity as? CActivity)?.showProgress() }
    }

    fun dismissProgress() {
        runCatching { (activity as? CActivity)?.dismissProgress() }
    }

    fun main() {
        (requireActivity() as CActivity).main()
    }

    /** OnClick */
    open fun onBackPressedEx(): Boolean = false

    fun finishFr() {
        parentFragmentManager.commit(true) {
            remove(this@CFragment)
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Suppress("UNCHECKED_CAST")
    fun <T : View> findViewById(@IdRes resId: Int): T? {
        if (resId == NO_ID) {
            Log.w("id is NO_ID" + " in the " + javaClass.simpleName)
            return null
        }
        if (resId == 0) {
            Log.w("id is 0" + " in the " + javaClass.simpleName)
            return null
        }

        if (requireView().id == resId)
            return requireView() as T

        val v = requireView().findViewById<T>(resId)
        if (v == null) {
            Log.printStackTrace(Exception("!has not " + resources.getResourceName(resId) + " in the " + javaClass.simpleName))
        }
        return v
    }

    companion object {
        private const val NO_ID = -1
    }
}