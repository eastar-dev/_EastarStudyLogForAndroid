@file:Suppress("unused", "SpellCheckingInspection", "FunctionName")

package smart.base

import android.content.Context
import android.log.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.eastar.pref.annotation.Pref
import java.util.*

@Pref(defaultSharedPreferences = true)
data class PPSharedPreferences(
    val pushToken: String,
    val isReadedPush: Boolean, // 푸시 읽음 여부 (다 읽었으면 true, 안 읽었으면 false)
    val userId: String
) {
    companion object {
        fun create(context: Context) {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            pref.registerOnSharedPreferenceChangeListener { _, key -> Log.w(key, pref.all[key].toString().replace("\n", "_")) }
        }
    }
}

