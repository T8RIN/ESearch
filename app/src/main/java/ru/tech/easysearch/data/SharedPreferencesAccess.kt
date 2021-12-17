package ru.tech.easysearch.data

import android.content.Context

object SharedPreferencesAccess {

    private const val mainSharedPrefsKey = "eSearch"
    private const val defLabels =
        "ic_google_logo+ic_bing_logo+ic_yandex_logo+ic_mailru_logo+ic_yahoo_logo"

    fun loadLabelList(context: Context): String? {
        return context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE)
            .getString("label", defLabels)
    }

    fun saveLabelList(context: Context, string: String) {
        context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE).edit()
            .putString("label", string).apply()
    }

}