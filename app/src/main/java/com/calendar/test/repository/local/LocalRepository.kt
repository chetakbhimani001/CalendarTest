package com.calendar.test.repository.local

import com.calendar.test.models.EventItem

class LocalRepository (private val db: EventDAO) {
     suspend fun getEvents(): List<EventItem> = db.getAll()
     suspend fun insertAll(events: List<EventItem>) = db.insertAll(events)
    suspend fun insert(event: EventItem) = db.insert(event)
    suspend fun delete(event: EventItem) = db.delete(event)
    suspend fun delete() = db.delete()

}