package de.raveberry.shareberry

import android.content.Context
import android.preference.PreferenceManager

object Storage {

    fun getUrls(context: Context): List<String> {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val defaultUrls = listOf(
            "http://raveberry",
            "http://raveberry.local",
            "http://192.168.4.1",
            "http://raveberry:8080",
            "http://raveberry.local:8080",
            "http://192.168.4.1:8080"
        ).joinToString(";")
        val urlString = sharedPreferences.getString("urls", defaultUrls)
        return urlString!!.split(";")
    }

    fun storeUrls(context: Context, urls: List<String>) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPreferences.edit()

        // join all urls to a single ;-seperated string. ;s are not useful in urls anyway
        editor.putString("urls", urls.joinToString(";"))
        editor.apply()
    }

}