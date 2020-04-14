package android.view

import android.log.Log
import android.widget.Checkable
import androidx.core.view.forEach

/**
 * <pre>
 * child view에 대하여 라디오 그룹의 버튼처럼 작동하게 만든다
 * 3가지모드가 있으며
 * RADIO         체크상태의 것이 1
 * SINGLE_CHECK  체크상태의 것이 0 or 1
 * NONE          체크상태의 것이 0 or N
 * ONEMORE       체크상태의 것이 1 or N
 * setFilter()를 이용하여
 * 클릭이 가능한것을 지정할수 있다.
 * 지정하지 않으면 첫번째 Child를 대상으로 한다.
 *
 * Checked 항목은 setSelected() 함수를 이용하여 상태를 변경한다.
 *
</pre> *
 */
class CheckerHelper {
    companion object {
        const val TAG_FILTER = "checkable"
    }

    private val children = mutableListOf<View>()

    private var onItemClick: ((CheckerHelper, ViewGroup?, View, Int, Int) -> Unit)? = null
    private var onItemSelectedListener: OnItemSelectedListener? = null
    private var onItemCheckedChange: ((CheckerHelper, ViewGroup?, View, Int, Int) -> Unit)? = null

    interface OnItemSelectedListener {
        fun onItemSelected(checkerHelper: CheckerHelper, parent: ViewGroup?, view: View, position: Int, id: Int)
        fun onNothingSelected(checkerHelper: CheckerHelper, parent: ViewGroup?)
    }

    fun getCheckedIndex(): Int = children.indexOfFirst { it.isSelected }
    fun getCheckedCount(): Int = children.count { it.isSelected }
    fun getCheckedIndexs(): List<Int> = children.mapIndexedNotNull { index, view -> if (view.isSelected) index else null }
    fun getCheckedView(): View? = children.firstOrNull { it.isSelected }
    fun getCheckedViews(): List<View> = children.filter { it.isSelected }
    fun getCount(): Int = children.size
    enum class MODE { ZERO_N, ONE, ZERO_ONE, ONE_N }

    var mode = MODE.ONE

    var group: ViewGroup? = null
        set(value) {
            field = value
            updateOnClickListener()
        }
    var tagFilter: String? = null

    @JvmOverloads
    fun set(parent: ViewGroup, tagFilter: String? = null) {
        this.group = parent
        this.tagFilter = tagFilter
        updateOnClickListener()
    }

    private fun updateOnClickListener() {
        children.forEach { it.setOnClickListener(null) }
        setItems()
        children.forEach { it.setOnClickListener { v -> itemClick(v) } }
    }

    private fun setItems() {
        if (tagFilter.isNullOrBlank())
            group?.forEach { children.add(it) }
        else
            setItemsByTag(group)
    }

    // 재귀함수
    private fun setItemsByTag(parent: ViewGroup?) {
        parent?.forEach { child ->
            if (child is ViewGroup) {
                setItemsByTag(child)
            } else if (tagFilter == child.tag) {
                children.add(child)
            }
        }
    }

    private fun fireNothingSelected() {
        onItemSelectedListener?.onNothingSelected(this, group)
    }

    private fun fireItemSelected(v: View) {
        onItemSelectedListener?.onItemSelected(this, group, v, children.indexOf(v), v.id)
    }

    private fun fireItemCheckedChagne(v: View) {
        onItemCheckedChange?.invoke(this, group, v, children.indexOf(v), v.id)
    }

    private fun fireItemClick(v: View) {
        onItemClick?.invoke(this, group, v, children.indexOf(v), v.id)
    }

    private fun setState(v: View, b: Boolean) {
        v.isSelected = b
        if (v is Checkable)
            v.isChecked = b
    }

    //-----------------------------------------------------------------------------------------------------------------
    fun getView(index: Int): View = children[index]

    fun setOnItemClickListener(onItemClickListener: ((CheckerHelper, ViewGroup?, View, Int, Int) -> Unit)? = null) {
        this.onItemClick = onItemClickListener
    }

    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener
    }

    fun setOnItemCheckedChangeListener(onItemCheckedChangeListener: ((CheckerHelper, ViewGroup?, View, Int, Int) -> Unit)? = null) {
        this.onItemCheckedChange = onItemCheckedChangeListener
    }

    fun itemClick(index: Int) {
        if (index !in 0 until children.size)
            return
        itemClick(children[index])
    }

    fun itemSelected(index: Int) {
        if (index !in 0 until children.size)
            return
        itemSelected(children[index])
    }

    fun itemClick(v: View) {
        itemSelected(v)
        fireItemClick(v)
    }

    fun itemSelected(v: View?) {
        if (v == null) {
            children.forEach { setState(it, false) }
            return
        }
        when (mode) {
            MODE.ZERO_N -> setState(v, !v.isSelected)
            MODE.ONE -> {
                val checkedChange = !v.isSelected
                children.forEach { setState(it, it === v) }
                fireItemSelected(v)
                if (checkedChange)
                    fireItemCheckedChagne(v)
            }
            MODE.ZERO_ONE -> {
                children.forEach { setState(it, false) }
                setState(v, true)
                if (!v.isSelected)
                    fireNothingSelected()
                else
                    fireItemSelected(v)
            }
            MODE.ONE_N -> {
                if (getCheckedCount() <= 1 && v.isSelected)
                    return
                setState(v, !v.isSelected)
            }
        }
    }
}
