package android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import java.util.*

abstract class PagerArrayAdapter<_DATA> @JvmOverloads constructor(@LayoutRes var layoutID: Int, objects: List<_DATA>? = null) : androidx.viewpager.widget.PagerAdapter() {

    private val objects: MutableList<_DATA> = ArrayList(10)
    private val lock = Any()

    init {
        set(objects)
    }

    fun set(items: List<_DATA>?) {
        synchronized(lock) {
            objects.clear()
            if (items != null)
                objects.addAll(items)
        }
        notifyDataSetChanged()
    }

    fun clear() {
        synchronized(lock) {
            objects.clear()
        }
        notifyDataSetChanged()
    }

    //------------------------------------------------------------------------
    protected open fun inflate(@LayoutRes layer: Int, parent: ViewGroup, position: Int): View =
            LayoutInflater.from(parent.context).inflate(layer, parent, false)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = inflate(layoutID, container, position)
        val d = getItem(position)
        instantiateItem(container, position, v, d)
        container.addView(v)
        return v
    }

    abstract fun instantiateItem(container: ViewGroup, position: Int, v: View, d: _DATA)
    open fun getItem(position: Int): _DATA = objects[position]

    override fun getCount() = objects.size
    override fun isViewFromObject(view: View, obj: Any) = (view === obj)
    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) = container.removeView(obj as View)
    override fun getItemPosition(obj: Any) = POSITION_NONE
}