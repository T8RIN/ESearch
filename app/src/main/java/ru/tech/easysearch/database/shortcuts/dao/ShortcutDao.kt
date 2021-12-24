package ru.tech.easysearch.database.shortcuts.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.tech.easysearch.database.shortcuts.Shortcut

@Dao
interface ShortcutDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(shortcut: Shortcut)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg shortcut: Shortcut)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(shortcut: Shortcut)

    @Delete
    fun delete(shortcut: Shortcut)

    @Query("delete from shortcuts")
    fun deleteAllShortcuts()

    @Query("select * from shortcuts order by id desc")
    fun getAllShortcuts(): LiveData<List<Shortcut>>

}