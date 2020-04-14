package smart.util

import android.log.Log
import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("gone")
fun setGone(view: View, gone: Boolean) {
    Log.e(view, gone)
    view.visibility = if (gone) View.GONE else View.VISIBLE
}


