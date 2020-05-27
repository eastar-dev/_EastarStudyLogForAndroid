@file:Suppress("unused")

package android.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.KeyguardManager
import android.base.BD
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.telephony.TelephonyManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import java.io.File

class KK

fun Activity.hideKeyboard() = window.decorView.hideKeyboard()
fun Fragment.hideKeyboard() = requireActivity().hideKeyboard()
fun View.hideKeyboard() = context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, 0)

fun Activity.showKeyboard() = window.decorView.showKeyboard()
fun Fragment.showKeyboard() = requireActivity().showKeyboard()
fun View.showKeyboard() = context.getSystemService<InputMethodManager>()?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)


@Suppress("UNCHECKED_CAST")
private fun Array<*>.toPair(): Array<kotlin.Pair<String, Any?>> {
    require(count() % 2 != 1) { "!!key value must pair" }
    return toList().zipWithNext { a, b ->
        (a to b)
    }.filterIndexed { index, _ ->
        index % 2 == 0
    }.map {
        it as kotlin.Pair<String, Any?>
    }.toTypedArray()
}

fun Context.isDeviceLock(): Boolean {
    if (BD.DEVELOP)
        return true
    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) keyguardManager.isDeviceSecure else keyguardManager.isKeyguardSecure
}

//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
@SuppressLint("MissingPermission", "HardwareIds")
fun Context.getLine1Number(): String {
    if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        && PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS)
        && PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE))
        return ""

    val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return tm.line1Number ?: ""
}

fun Bitmap.toFile(file: File) = runCatching {
    file.run {
        parentFile?.let {
            if (!it.exists())
                it.mkdirs()
        }
        outputStream().use { output ->
            this@runCatching.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }
}.getOrDefault(false)

val ByteArray.toHex
    get() = joinToString("") { "%02X".format(it) }


