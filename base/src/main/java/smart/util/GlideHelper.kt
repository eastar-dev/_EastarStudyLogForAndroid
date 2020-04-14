package smart.util

import android.app.Activity
import android.log.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

object GlideHelper {
    @JvmStatic
    fun set(activity: Activity, url: Any, imageView: ImageView) {
        Glide.with(activity).load(url).into(imageView)
    }

    @JvmStatic
    fun set(fragment: Fragment, url: Any, imageView: ImageView) {
        if (fragment.view == null) {
            Log.e(fragment, "!fragment not attached")
            return
        }
        Glide.with(fragment).load(url).into(imageView)
    }

    fun set(url: String, view: ImageView) {
        Glide.with(view).load(url).into(view)
    }
}

@BindingAdapter("android:src")
fun loadImage(imageView: ImageView, src: Any?) {
    if (src is Int && src <= 0) return
    Glide.with(imageView.context)
            .load(src)
            .into(imageView)
}