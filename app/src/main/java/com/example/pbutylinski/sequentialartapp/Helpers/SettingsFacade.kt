package com.example.pbutylinski.sequentialartapp.Helpers

import android.app.Activity
import android.content.Context

class SettingsFacade(activity: Activity) {
    private var activity: Activity? = activity

    fun saveReadingStrip(number: Int) {
        with (this.activity!!.getPreferences(Context.MODE_PRIVATE).edit()) {
            putInt("strip-id", number)
            apply()
        }
    }

    fun getLastReadingStrip(maxStripNumber: Int): Int {
        return this.activity!!
                .getPreferences(Context.MODE_PRIVATE)
                .getInt("strip-id", maxStripNumber)
    }
}