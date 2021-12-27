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