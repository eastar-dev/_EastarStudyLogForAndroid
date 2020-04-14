@file:Suppress("unused")

package android.util

import android.content.ContentResolver
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.telephony.ServiceState
import androidx.annotation.FloatRange
import java.util.*

object BU {
    fun getUriDrawable(packageName: String, drawable_name: String): Uri {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/drawable/" + drawable_name)
    }

    @JvmStatic
    fun dummy(count: Int): String {
        val chs = CharArray(count)
        Arrays.fill(chs, '*')
        return String(chs)
    }

    @JvmStatic
    fun isEmpty(cs: CharSequence?): Boolean = cs.isNullOrBlank()

    fun getStateListDrawable(normal: Drawable, pressed: Drawable): StateListDrawable {
        val drawable = StateListDrawable()
        drawable.addState(intArrayOf(android.R.attr.state_pressed), pressed)
        drawable.addState(intArrayOf(android.R.attr.state_selected), pressed)
        drawable.addState(intArrayOf(), normal)
        return drawable
    }

    fun getServiceStatus(): Int {
        return ServiceState().state
    }

    /**
     * @see [What's HUE](https://ko.wikipedia.org/wiki/HSV_%EC%83%89_%EA%B3%B5%EA%B0%84)
     */
    fun changeHue(color: Int, @FloatRange(from = 0.0, to = 1.0) s: Float, @FloatRange(from = 0.0, to = 1.0) v: Float): Int {
        val alpha: Int = Color.alpha(color)
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        //        hsv[0] = hue;
        hsv[1] *= s
        hsv[2] *= v
        return Color.HSVToColor(alpha, hsv)
    }
}
