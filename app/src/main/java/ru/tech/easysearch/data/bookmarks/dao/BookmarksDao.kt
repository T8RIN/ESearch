package ru.tech.easysearch.data.bookmarks.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.tech.easysearch.data.bookmarks.Bookmark

@Dao
interface BookmarksDao {

    @Insert
    fun insert(bookmark: Bookmark)

    @Update
    fun update(bookmark: Bookmark)

    @Delete
    fun delete(bookmark: Bookmark)

    @Query("delete from bookmarks")
    fun deleteAllBookmarks()

    @Query("select * from bookmarks order by id desc")
    fun getAllBookmarks(): LiveData<List<Bookmark>>
}