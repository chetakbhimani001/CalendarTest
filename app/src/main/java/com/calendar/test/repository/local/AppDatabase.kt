package com.calendar.test.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.calendar.test.models.EventItem

@Database(entities = [EventItem::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventsDao(): EventDAO
}