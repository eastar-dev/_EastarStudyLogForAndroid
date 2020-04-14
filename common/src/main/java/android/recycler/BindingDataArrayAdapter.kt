@file:Suppress("unused")

package android.recycler

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class BindingDataArrayAdapter(@LayoutRes layoutResId: Int, var brId: Int, objects: MutableList<Any> = mutableListOf()) :
    BindingViewArrayAdapter<ViewDataBinding, Any>(layoutResId, objects) {
    override fun onBindViewHolder(bb: ViewDataBinding, d: Any, holder: RecyclerView.ViewHolder, position: Int) {
        bb.setVariable(brId, d)
    }
}