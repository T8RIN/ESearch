package ru.tech.easysearch.database.hist.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.tech.easysearch.database.hist.History

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(history: History)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(history: History)

    @Delete
    fun delete(history: History)

    @Query("delete from history")
    fun clearHistory()

    @Query("select * from history order by id desc limit 1")
    fun getLastRecord(): History

    @Query("select * from history order by id desc")
    fun getHistory(): LiveData<List<History>>

}