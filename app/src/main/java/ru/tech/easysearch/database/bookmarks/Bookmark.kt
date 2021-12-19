package ru.tech.easysearch.database.bookmarks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    val description: String,
    val url: String,
    val icon: ByteArray?,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)