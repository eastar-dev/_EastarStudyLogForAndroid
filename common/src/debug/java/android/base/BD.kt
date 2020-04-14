package android.base

import android.annotation.SuppressLint
import android.content.Context

class BD {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var CONTEXT: Context
        @JvmField
        var DEVELOP = false
        @JvmField
        var PASS = false
    }
}