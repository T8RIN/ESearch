package ru.tech.easysearch.data

import android.content.Context

object SharedPreferencesAccess {

    const val mainSharedPrefsKey = "eSearch"
    private const val defLabels =
        "ic_google_logo+ic_bing_logo+ic_yandex_logo+ic_mailru_logo+ic_yahoo_logo"

    const val EYE_PROTECTION = "495454"
    const val AD_BLOCK = "463677"
    const val IMAGE_LOADING = "746758"
    const val HIDE_PANELS = "476836"
    const val LOCATION_ACCESS = "873937"
    const val CAMERA_ACCESS = "932938"
    const val MIC_ACCESS = "8984790"
    const val SAVE_HISTORY = "3874946"
    const val SAVE_TABS = "408954"
    const val COOKIES = "254873"
    const val JS = "359474"
    const val POPUPS = "984372"
    const val DOM_STORAGE = "189783"
    const val GET = 1
    const val SET = 2

    fun loadLabelList(context: Context): String? {
        return context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE)
            .getString("label", defLabels)
    }

    fun saveLabelList(context: Context, string: String) {
        context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE).edit()
            .putString("label", string).apply()
    }

    fun getSetting(context: Context, key: String): Boolean {
        return context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE)
            .getBoolean(key, key != EYE_PROTECTION)
    }

    fun setSetting(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE).edit()
            .putBoolean(key, value).apply()
    }

    fun needToChangeBrowserSettings(context: Context, modifier: Int): Boolean {
        when (modifier) {
            GET -> return context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE)
                .getBoolean("needSetting", false)
            SET -> context.getSharedPreferences(mainSharedPrefsKey, Context.MODE_PRIVATE).edit()
                .putBoolean("needSetting", true).apply()
        }
        return false
    }
}