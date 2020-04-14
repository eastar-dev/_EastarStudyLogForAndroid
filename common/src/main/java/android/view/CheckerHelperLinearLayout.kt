package android.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class CheckerHelperLinearLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    val checkerHelper = CheckerHelper()
    val checkedIndex: Int = checkerHelper.getCheckedIndex()
    val checkedView: View? = checkerHelper.getCheckedView()
    val count: Int = checkerHelper.getCount()

    override fun onFinishInflate() {
        super.onFinishInflate()
        set()
    }

    fun set() = checkerHelper.set(this, tag as? String)
    fun setOnItemClickListener(listener: ((CheckerHelper, ViewGroup?, View, Int, Int) -> Unit)? = null) = checkerHelper.setOnItemClickListener(listener)
    fun setOnItemSelectedListener(listener: CheckerHelper.OnItemSelectedListener) = checkerHelper.setOnItemSelectedListener(listener)
    fun getView(index: Int): View = checkerHelper.getView(index)
    fun setItemSelected(index: Int) = checkerHelper.itemSelected(index)
}
