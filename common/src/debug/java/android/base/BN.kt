package android.base

import android.content.Context
import androidx.preference.PreferenceManager

enum class BN {
    REAL, STG, DEV, DEV2;

    companion object {
        fun getLastServer(context: Context) = valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(BN::class.java.name, REAL.name)!!)
        fun setLastServer(context: Context, server: BN) = PreferenceManager.getDefaultSharedPreferences(context).edit().putString(BN::class.java.name, server.name).apply()
    }
}


