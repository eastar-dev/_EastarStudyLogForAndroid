package android.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import dev.eastar.ktx.appName
import dev.eastar.ktx.getDrawableId
import dev.eastar.ktx.getRawString
import java.util.*

object BV {
    lateinit var CONTEXT: Context
    lateinit var APP_NAME: String

    var PACKAGE_NAME = ""
    @JvmField
    var L: Locale = Locale.getDefault()
    lateinit var DISPLAY_METRICS: DisplayMetrics
    var DENSITY_DPI: Int = 0
    @JvmField
    var WIDTH_PIXELS: Int = 0

    fun create(context: Context) {
        CONTEXT = context

        with(context.resources.displayMetrics) {
            DISPLAY_METRICS = this
            DENSITY_DPI = densityDpi
            WIDTH_PIXELS = widthPixels
        }
        PACKAGE_NAME = context.packageName
        APP_NAME = context.appName.toString()
    }

    @JvmStatic
    fun getResIdDrawable(drawable_name: String): Int = CONTEXT.getDrawableId(drawable_name)

    fun getUriDrawable(drawable_name: String): Uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + PACKAGE_NAME + "/drawable/" + drawable_name)

    fun raw2string(@RawRes rawResId: Int): String = CONTEXT.getRawString(rawResId)
}
