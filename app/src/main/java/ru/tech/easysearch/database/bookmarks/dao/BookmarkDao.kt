package ru.tech.easysearch.database.bookmarks.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.tech.easysearch.database.bookmarks.Bookmark

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(bookmark: Bookmark)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg bookmark: Bookmark)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(bookmark: Bookmark)

    @Delete
    fun delete(bookmark: Bookmark)

    @Query("delete from bookmarks")
    fun deleteAllBookmarks()

    @Query("select * from bookmarks order by id desc")
    fun getAllBookmarks(): LiveData<List<Bookmark>>

}