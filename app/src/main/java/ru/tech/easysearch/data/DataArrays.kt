package ru.tech.easysearch.data

object DataArrays {
    val prefixDict: Map<String, String> = mapOf(
        Pair("ic_amazon_logo", "https://www.amazon.com/s?k="),
        Pair("ic_avito_logo", "https://m.avito.ru/rossiya?q="),
        Pair("ic_bing_logo", "https://www.bing.com/search?q="),
        Pair("ic_duckduckgo_logo", "https://duckduckgo.com/?q="),
        Pair("ic_ebay_logo", "https://www.ebay.com/sch/i.html?_nkw="),
        Pair("ic_ekatalog_logo", "https://www.e-katalog.ru/ek-list.php?search_="),
        Pair("ic_facebook_logo", "https://m.facebook.com/search/top/?q="),
        Pair("ic_google_logo", "https://www.google.com/search?q="),
        Pair("ic_imdb_logo", "https://m.imdb.com/find?q="),
        Pair("ic_mailru_logo", "https://go.mail.ru/msearch?q="),
        Pair("ic_ozon_logo", "https://www.ozon.ru/search/?from_global=true&text="),
        Pair("ic_translate_logo", "https://translate.google.ru/?sl=auto&tl=ru&text="),
        Pair("ic_twitter_logo", "https://mobile.twitter.com/search?q="),
        Pair("ic_vk_logo", "https://m.vk.com/search?c[section]=auto&c[q]="),
        Pair("ic_wikipedia_logo", "https://ru.m.wikipedia.org/w/index.php?search="),
        Pair("ic_yahoo_logo", "https://search.yahoo.com/search?p="),
        Pair("ic_yandex_logo", "https://yandex.ru/search/touch/?text="),
        Pair("ic_youla_logo", "https://youla.ru/?q="),
        Pair("ic_youtube_logo", "https://m.youtube.com/results?sp=mAEA&search_query="),
        Pair("ic_github_logo", "https://github.com/search?q=")
    )

    const val userAgentString =
        "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.104 Mobile Safari/537.36"

    const val desktopUserAgentString =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 12_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.0 Safari/605.1.15"

    const val translateSite = "https://translate.yandex.ru/translate?url="
    const val faviconParser = "https://www.google.com/s2/favicons?sz=64&domain_url="

    val brokenIcons = listOf(
        "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARzQklUCAgICHwIZIgAAAKzSURBVDiNpZPNTxsHEMV/tXfX9tpef6w/agN27FoJkUqqIoMQNIcoosqJGznkkI9DTvTPIYeKeyvFuTSRekGqxMEpBRuprUBQKAWTQLy2u/Zis2bXLr2Alai95Z1mpHlv3sxo4CPxyVUwP//cU7VqkZl8dsK2egt608y3jK4PQPG726GgpyS5hGfF0v56XIrWC4X75kBgfv65p23rydz1+NN253wul1FjqWxYtew+kugkk1JZXt5uHFR0TfFLL/f+qC35xNBxoXDfFACqVi3yxY2hp2dm78HntxIjmbSKV5YYHxumc2aRTipkUxH19+1j9bvvy75cLsqvuyfPgCMHwEw+O2G0u3NPHk2MZNIqAJbdZ3XjcDBrTJURRSePH06OtDrnczP57ASAA8A67y1cGwnFpr5MDQi23ce2+4QCMgAds0ezZRIKeEgNhWLdrr0wENCbZn52dlTtmL0PNnz3q+soXgEAr0fgxmcx9it/c+/eTVXXzTyAANAyTF88oqB4BbyyxHAiyHAiOCADOB1QrZ8CEI8oGIbpGzgA8MoSAFPj13hz0sSyP3RzBVF0AnBxmQtXd96v1JVENIXiFfj6do5iucLOZVE2FSERlbkzlaHe7LLzp4bid7cHDsIhubS8vN14v9PYaBKt3kart1ndOGDtt2MAIkE3r15tNsJBuTQQEEVh8aCia8Vy5X9tA7w9adL/B4rlCsfVluZyi4sATgBHYLqTTgYDP/9ymI5+qgRSyQAuyYEoimiXixsbTbKzr/Ht0uujoN/1orR59MPhzo+GE2Dy1mP7Xa2xm0go0uvVA7W0ceRw+1xys3XGiWZgnHZZWdlr/LSyexj0u17s7dWWwpJa3doq9P7zTNPj2fy5ZX+j62beOO36Li4uUPzuthqW12W3sLhW+qsceO+ZPhr/AkthE9HLdn4kAAAAAElFTkSuQmCC",
        "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAAXNSR0IArs4c6QAAAARzQklUCAgICHwIZIgAAAAnSURBVHic7cEBDQAAAMKg909tDjegAAAAAAAAAAAAAAAAAAAAgHcDQEAAAY/yyVEAAAAASUVORK5CYII="
    )

    val sizeSuffixes = listOf("B", "kB", "MB", "GB", "TB")

    val NEGATIVE_COLOR = floatArrayOf(
        -1.0f,
        0f,
        0f,
        0f,
        255f,
        0f,
        -1.0f,
        0f,
        0f,
        255f,
        0f,
        0f,
        -1.0f,
        0f,
        255f,
        0f,
        0f,
        0f,
        1.0f,
        0f
    )

}