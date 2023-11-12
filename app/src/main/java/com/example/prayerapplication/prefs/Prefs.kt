package com.example.prayerapplication.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Prefs @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        const val PREF_NAME = "prayers_shared_pref"
    }

    private val instance = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isOneTimeAlert: Boolean
        get() {
            return instance.getBoolean(::isOneTimeAlert.name, true)
        }
        set(value) {
            instance.edit().putBoolean(::isOneTimeAlert.name, value).apply()
        }


}