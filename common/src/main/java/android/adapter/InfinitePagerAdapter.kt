package android.adapter

import androidx.annotation.LayoutRes

@Suppress("unused")
abstract class InfinitePagerAdapter<_DATA> : PagerArrayAdapter<_DATA> {
    companion object {
        private const val REPEAT_COUNT = 20
    }

    @JvmOverloads
    constructor(@LayoutRes layoutID: Int, items: List<_DATA> = emptyList()) : super(layoutID, items)

    constructor(@LayoutRes layoutID: Int, items: Array<_DATA>) : this(layoutID, items.asList())

    val startPosition: Int get() = count / 2
    override fun getItem(position: Int): _DATA = super.getItem(position % super.getCount())
    override fun getCount(): Int = if (super.getCount() <= 1) super.getCount() else super.getCount() * REPEAT_COUNT
}
