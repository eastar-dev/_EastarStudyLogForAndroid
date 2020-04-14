package android.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

class StopableScrollView : ScrollView {

    var isScrollable = true
        private set

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    fun setScrollingEnabled(enabled: Boolean) {
        isScrollable = enabled
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                return if (isScrollable) super.onTouchEvent(ev) else isScrollable
// mScrollable is always false at this point
            }
            else -> return super.onTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (!isScrollable)
            false
        else
            super.onInterceptTouchEvent(ev)
    }

}