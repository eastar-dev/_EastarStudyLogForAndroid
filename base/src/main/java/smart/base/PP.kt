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
        //@formatter:off
        val version100                    : Boolean,
        val androidId                     : String,
        val appUuid                       : String,
        val isStarted                     : Boolean, // 앱 최초 실행 여부
        val menu                          : String, // 최근 사용한 메뉴
        val isReadedPush                  : Boolean, // 푸시 읽음 여부 (다 읽었으면 true, 안 읽었으면 false)
        val pushToken                     : String,
        val cmsData                       : String ,
        val appInfo                       : String,
        val isAutoLogin                   : Boolean, // 자동로그인설정여부
        val lastLogin                     : String // 자동로그인설정여부

        //@formatter:on
) {
    companion object {
        fun create(context: Context) {
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            pref.registerOnSharedPreferenceChangeListener { _, key -> Log.w(key, pref.all[key].toString().replace("\n", "_")) }

            if (pref.getString("appUuid", null).isNullOrBlank()) {
                pref.edit(true) { putString("androidId", UUID.randomUUID().toString()) }
                pref.edit(true) { putString("appUuid", UUID.randomUUID().toString()) }
            }
        }
    }
}

