package ru.tech.easysearch.data.bookmarks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    val description: String,
    val url: String,
    val icon: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)