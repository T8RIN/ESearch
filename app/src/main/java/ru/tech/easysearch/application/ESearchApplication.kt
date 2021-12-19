package ru.tech.easysearch.application

import android.app.Application
import ru.tech.easysearch.data.bookmarks.database.BookmarksDatabase

class ESearchApplication : Application() {

    companion object {
        lateinit var bookmarkDatabase: BookmarksDatabase
    }

}