package ru.tech.easysearch.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.tech.easysearch.database.bookmarks.Bookmark
import ru.tech.easysearch.database.bookmarks.dao.BookmarkDao
import ru.tech.easysearch.database.hist.History
import ru.tech.easysearch.database.hist.dao.HistoryDao

@Database(entities = [Bookmark::class, History::class], version = 1)
abstract class ESearchDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao

    companion object {
        private var instance: ESearchDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): ESearchDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, ESearchDatabase::class.java,
                    "room_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            return instance!!
        }
    }
}