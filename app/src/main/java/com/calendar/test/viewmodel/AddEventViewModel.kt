package com.calendar.test.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.os.Handler
import android.provider.CalendarContract
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.calendar.test.utils.Constants.EVENT_PROJECTION
import com.calendar.test.utils.Constants.PROJECTION_ID_INDEX
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class AddEventViewModel(application: Application) : AndroidViewModel(application) {
    var mutableData: MutableLiveData<Boolean> = MutableLiveData()
    var isEventExist: LiveData<Boolean> = mutableData

    fun findCalendarEvents(startFromParam: Long? = null, startToParam: Long? = null) {
        val result = JSONArray()
        val uri = CalendarContract.Events.CONTENT_URI
        try {
            Handler().post {
                try {
                    var selection: String? = ""
                    val selectionList: MutableList<String> = ArrayList()
                    val resolver: ContentResolver =
                        getApplication<Application>().applicationContext.contentResolver
                    if (startFromParam != null) {
                        if (!TextUtils.isEmpty(selection)) {
                            selection += " AND "
                        }
                        selection += " deleted = 0 AND " + CalendarContract.Events.DTSTART + " < ? AND " + CalendarContract.Events.DTSTART
                        selectionList.add("" + startFromParam + "")
                    }
                    if (startToParam != null) {
                        if (!TextUtils.isEmpty(selection)) {
                            selection += " AND "
                        }
                        selection += " deleted = 0 AND " + CalendarContract.Events.DTEND + " <= ?"
                        selectionList.add("" + startToParam + "")
                    }

                    val selection1 =
                        " (" + CalendarContract.Events.DTSTART + " < '" + startFromParam + "' AND '" + startFromParam + "' < " + CalendarContract.Events.DTEND + ") OR " + " (" + CalendarContract.Events.DTSTART + " < '" + startToParam + "' AND '" + startToParam + "' < " + CalendarContract.Events.DTEND + ") OR " + " (" + CalendarContract.Events.DTSTART + " < '" + startFromParam + "' AND '" + startToParam + "' < " + CalendarContract.Events.DTEND + ") OR " + " ('" + startFromParam + "' < " + CalendarContract.Events.DTSTART + " AND " + CalendarContract.Events.DTEND + " < '" + startToParam + "')"

                    val cur1 = resolver.query(
                        uri,
                        EVENT_PROJECTION,
                        selection1, null, null
                    )
                    while (cur1!!.moveToNext()) {
                        val obj = JSONObject()
                        val eventId1 =
                            cur1.getLong(PROJECTION_ID_INDEX)
                        obj.put("id", eventId1)
                        result.put(obj)
                    }
                    cur1.close()
                    mutableData.value = result.length() > 0
                } catch (e: JSONException) {
                    e.message
                }
            }
        } catch (e: java.lang.Exception) {
            System.err.println("Exception: " + e.message)
        }
    }
}