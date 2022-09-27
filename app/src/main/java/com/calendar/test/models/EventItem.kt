package com.calendar.test.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events_table")
data class EventItem(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val title: String?,
    val eventLocation: String?,
    val status: Int?,
    val dtStart: Long?,
    val dtEnd: Long?,
    val duration: String?,
    val allDay: Boolean?,
    val availability: Int?,
    val rRule: String?,
    val displayColor: Int?,
    val visible: Boolean?,
)