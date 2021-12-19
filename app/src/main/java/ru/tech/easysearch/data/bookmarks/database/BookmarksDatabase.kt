package ru.tech.easysearch.data.bookmarks.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.tech.easysearch.data.bookmarks.Bookmark
import ru.tech.easysearch.data.bookmarks.dao.BookmarksDao

@Database(entities = [Bookmark::class], version = 1)
abstract class BookmarksDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarksDao

    companion object {
        private var instance: BookmarksDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): BookmarksDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, BookmarksDatabase::class.java,
                    "bookmark_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            return instance!!
        }
    }
}