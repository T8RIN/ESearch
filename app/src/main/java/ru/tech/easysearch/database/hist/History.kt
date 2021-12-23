package ru.tech.easysearch.database.hist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History(
    val description: String,
    val url: String,
    val icon: ByteArray?,
    val time: String,
    val date: String,
    val sortingString: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)