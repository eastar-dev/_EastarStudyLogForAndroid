package android.view

import android.content.Context
import android.util.AttributeSet

import androidx.viewpager.widget.ViewPager

class StoppableViewPager : ViewPager {
    private var mEnabled = true

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return mEnabled && super.onInterceptTouchEvent(event)
    }

    fun setPagingEnabled(enabled: Boolean) {
        mEnabled = enabled
    }
}
