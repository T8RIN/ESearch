package ru.tech.easysearch.database.shortcuts

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shortcuts")
data class Shortcut(
    val description: String,
    val url: String,
    val icon: ByteArray?,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)