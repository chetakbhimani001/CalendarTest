package com.calendar.test.repository.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.calendar.test.models.EventItem

@Dao
interface EventDAO {

    @Query("SELECT * FROM events_table")
    suspend fun getAll(): List<EventItem>

    @Insert
    suspend fun insertAll(events: List<EventItem>)

    @Insert
    suspend fun insert(event: EventItem)

    @Delete
    suspend fun delete(event: EventItem)

    @Query("DELETE FROM events_table")
    suspend fun delete()
}