package android.view

import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import dev.eastar.ktx.dp
import dev.eastar.ktx.dpf

class DashItemDecoration : RecyclerView.ItemDecoration() {
    private val paint = Paint().apply {
        color = Color.parseColor("#1f000000")
        style = Paint.Style.STROKE
        strokeWidth = 1.dpf
        pathEffect = DashPathEffect(floatArrayOf(3.dpf, 3.dpf), 0f)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop = child.bottom + params.bottomMargin
            val dividerBottom = dividerTop + 1.dp
            c.drawLine(dividerLeft.toFloat(), (dividerTop + dividerBottom) / 2f, dividerRight.toFloat(), (dividerTop + dividerBottom) / 2f, paint)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, 1.dp)
    }
}
