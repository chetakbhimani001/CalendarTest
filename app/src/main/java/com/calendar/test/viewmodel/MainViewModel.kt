package com.calendar.test.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calendar.test.models.EventItem
import com.calendar.test.repository.local.LocalRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: LocalRepository) : ViewModel() {

     fun fetchEvents() {
        viewModelScope.launch {
            try {
                val getEvents = repository.getEvents()
            } catch (e: Exception) {
                // handler error
            }
        }
    }

     fun insertEvents(event: EventItem) {
        viewModelScope.launch {
            try {
               repository.insert(event)
            } catch (e: Exception) {
                // handler error
            }
        }
    }
    fun deleteAllEvents() {
        viewModelScope.launch {
            try {
              repository.delete()
            } catch (e: Exception) {
                // handler error
            }
        }
    }

}