package com.calendar.test.view.activities

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.calendar.test.R
import com.calendar.test.databinding.ActivityMainBinding
import com.calendar.test.models.CalendarData
import com.calendar.test.models.EventItem
import com.calendar.test.models.HeaderEvent
import com.calendar.test.utils.Constants
import com.calendar.test.utils.Constants.CALENDAR_PROJECTION
import com.calendar.test.utils.Constants.DATE_FORMAT
import com.calendar.test.utils.Constants.EVENT_PROJECTION
import com.calendar.test.utils.Constants.PROJECTION_ALL_DAY_INDEX
import com.calendar.test.utils.Constants.PROJECTION_AVAILABILITY_INDEX
import com.calendar.test.utils.Constants.PROJECTION_DISPLAY_COLOR_INDEX
import com.calendar.test.utils.Constants.PROJECTION_DTEND_INDEX
import com.calendar.test.utils.Constants.PROJECTION_DTSTART_INDEX
import com.calendar.test.utils.Constants.PROJECTION_DURATION_INDEX
import com.calendar.test.utils.Constants.PROJECTION_EVENT_LOCATION_INDEX
import com.calendar.test.utils.Constants.PROJECTION_ID_INDEX
import com.calendar.test.utils.Constants.PROJECTION_RRULE_INDEX
import com.calendar.test.utils.Constants.PROJECTION_STATUS_INDEX
import com.calendar.test.utils.Constants.PROJECTION_TITLE_INDEX
import com.calendar.test.utils.Constants.PROJECTION_VISIBLE_INDEX
import com.calendar.test.utils.Constants.getFormattedDate
import com.calendar.test.view.adapters.EventItemAdapter
import com.calendar.test.viewmodel.MainViewModel
import com.calendar.test.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private lateinit var eventItemAdapter: EventItemAdapter
    private var sharedPreferences: SharedPreferences? = null
    private var calendarPermissionGranted = false
    private val MY_SHARED_PREFERENCES = "MYSHAREDPREF"
    var USER_ASKED_CALENDAR_PERMISSION_BEFORE = "USER_ASKED_CALENDAR_PERMISSION_BEFORE"
    private val PERMISSION_REQUEST_READ_CALENDAR = 1
    var eventList: MutableList<HeaderEvent> = mutableListOf()
    var calendarId = -1
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        sharedPreferences = applicationContext.getSharedPreferences(
            MY_SHARED_PREFERENCES,
            MODE_PRIVATE
        )
        getDefaultCalendarIdOfDevice()
        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { _ ->
            if (calendarPermission) {
                addCalendarEvent()
            }
        }
    }

    private fun getDefaultCalendarIdOfDevice() {
        if (calendarPermission) {
            var calendarId = 1
            val calendars: List<CalendarData>? = getAvailableCalendarsList()
            if (calendars != null && calendars.isNotEmpty()) {
                for (calendar in calendars) {
                    if (calendar.defaultCalendar) {
                        calendarId = calendar.id.trim().toInt()
                    }
                }
            } else {
                print("No calendars found.")
            }
            // Setup RecyclerView adapter
            eventItemAdapter = EventItemAdapter()
            findViewById<RecyclerView>(R.id.recycler_view_events).let {
                it.layoutManager = LinearLayoutManager(this)
                it.adapter = eventItemAdapter
            }
            this.calendarId = calendarId
            if (calendarId != null) {
                getEvents(calendarId)
            }
        }
    }

    protected fun getAvailableCalendarsList(): List<CalendarData>? {
        val cursor: Cursor? = queryCalendars(
            CALENDAR_PROJECTION,
            Calendars.VISIBLE + "=1",
            null,
            null
        )
        val availableCalendars: MutableList<CalendarData> = ArrayList()
        var defaultSelected = false
        if (cursor?.moveToFirst() == true) {
            do {
                val col =
                    cursor.getColumnIndex(Calendars._ID)
                val primaryCol =
                    cursor.getColumnIndex(Calendars.IS_PRIMARY)
                val nameCol =
                    cursor.getColumnIndex(Calendars.NAME)
                val displayNameCol =
                    cursor.getColumnIndex(Calendars.CALENDAR_DISPLAY_NAME)
                if (primaryCol != -1) {
                    var defaultCalendar = false
                    if (!defaultSelected && cursor.getInt(primaryCol) == 1) {
                        defaultSelected = true
                        defaultCalendar = true
                    }
                    val data =
                        CalendarData(
                            cursor.getString(col), cursor.getString(nameCol),
                            cursor.getString(displayNameCol), defaultCalendar
                        )
                    availableCalendars.add(data)
                }
            } while (cursor.moveToNext())
            cursor.close()
        }
        return availableCalendars
    }

    //Permission not granted
    private val calendarPermission: Boolean
        private get() {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.WRITE_CALENDAR
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //Permission not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_CALENDAR
                    ) || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_CALENDAR
                    )
                ) {
                    //Can ask user for permission
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR),
                        PERMISSION_REQUEST_READ_CALENDAR
                    )
                } else {
                    val userAskedPermissionBefore = sharedPreferences?.getBoolean(
                        USER_ASKED_CALENDAR_PERMISSION_BEFORE,
                        false
                    )
                    if (userAskedPermissionBefore == true) {
                        //If User was asked permission before and denied
                        val alertDialogBuilder: android.app.AlertDialog.Builder =
                            android.app.AlertDialog.Builder(this)
                        alertDialogBuilder.setTitle(getString(R.string.permission_title))
                        alertDialogBuilder.setMessage(getString(R.string.permission_error))
                        val positiveButton = alertDialogBuilder.setPositiveButton("Open Setting",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                val intent = Intent()
                                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                val uri: Uri = Uri.fromParts(
                                    "package", this@MainActivity.packageName,
                                    null
                                )
                                intent.data = uri
                                this@MainActivity.startActivity(intent)
                            })
                        alertDialogBuilder.setNegativeButton("Cancel",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                Log.d(
                                    " Companion.TAG", "onClick: Cancelling"
                                )
                            })
                        val dialog: android.app.AlertDialog? = alertDialogBuilder.create()
                        dialog?.show()
                    } else {
                        //If user is asked permission for first time
                        ActivityCompat.requestPermissions(
                            this, arrayOf(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR),
                            PERMISSION_REQUEST_READ_CALENDAR
                        )
                        val editor = sharedPreferences!!.edit()
                        editor.putBoolean(USER_ASKED_CALENDAR_PERMISSION_BEFORE, true)
                        editor.apply()
                    }
                }
            } else {
                calendarPermissionGranted = true
            }
            return calendarPermissionGranted
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        calendarPermissionGranted = false
        when (requestCode) {
            PERMISSION_REQUEST_READ_CALENDAR -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Granted
                    calendarPermissionGranted = true
                    getDefaultCalendarIdOfDevice()
                } else {
                    //Denied
                }
            }
        }
    }

    protected fun queryCalendars(
        projection: Array<String>, selection: String?,
        selectionArgs: Array<String?>?, sortOrder: String?
    ): Cursor? {
        return try {
            getContentResolver().query(
                Calendars.CONTENT_URI, projection, selection, selectionArgs, sortOrder
            )
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Permission denied", e)
            null
        }
    }

    private fun getEvents(calendarId: Int) {
        eventItemAdapter.clearData()
        mainViewModel.deleteAllEvents()
        eventList.clear()
        val sortOrder = CalendarContract.Events.DTSTART + " DESC"
        val uri = CalendarContract.Events.CONTENT_URI
        val selection = "(${CalendarContract.Events.CALENDAR_ID} = ?)"
        val selectionArgs = arrayOf(calendarId.toString())
        val cur = contentResolver.query(
            uri,
            EVENT_PROJECTION,
            selection, selectionArgs,
            sortOrder
        ,
        )
        var date = 0L
        while (cur?.moveToNext() == true) {
            val eventId = cur.getLong(PROJECTION_ID_INDEX)
            val title = cur.getStringOrNull(PROJECTION_TITLE_INDEX)
            val eventLocation = cur.getStringOrNull(PROJECTION_EVENT_LOCATION_INDEX)
            val status = cur.getIntOrNull(PROJECTION_STATUS_INDEX)
            val dtStart = cur.getLongOrNull(PROJECTION_DTSTART_INDEX)
            val dtEnd = cur.getLongOrNull(PROJECTION_DTEND_INDEX)
            val duration = cur.getStringOrNull(PROJECTION_DURATION_INDEX)
            val allDay = cur.getIntOrNull(PROJECTION_ALL_DAY_INDEX) == 1
            val availability = cur.getIntOrNull(PROJECTION_AVAILABILITY_INDEX)
            val rRule = cur.getStringOrNull(PROJECTION_RRULE_INDEX)
            val displayColor = cur.getIntOrNull(PROJECTION_DISPLAY_COLOR_INDEX)
            val visible = cur.getIntOrNull(PROJECTION_VISIBLE_INDEX) == 1

            if (dtStart != null && getFormattedDate(dtStart,Constants.DATE_FORMAT_1) != getFormattedDate(date,Constants.DATE_FORMAT_1) ) {
                    date = dtStart
                eventList.add(HeaderEvent(getFormattedDate(date,DATE_FORMAT),null,true))
            }
            val event = EventItem(
                id = eventId,
                title = title,
                eventLocation = eventLocation,
                status = status,
                dtStart = dtStart,
                dtEnd = dtEnd,
                duration = duration,
                allDay = allDay,
                availability = availability,
                rRule = rRule,
                displayColor = displayColor,
                visible = visible,
            )
            eventList.add(HeaderEvent(null,event,false))

            CoroutineScope(Dispatchers.IO).launch {
                mainViewModel.insertEvents(event)
            }
        }
        cur?.close()
        eventItemAdapter.pushData(
            eventList
        )
        if(eventList.isEmpty())
        {
            binding.noEvents.visible()
        }
    }

    private fun addCalendarEvent() {
        val intent  = Intent(this@MainActivity,CreateEventActivity::class.java)
        intent.putExtra(Constants.CALENDAR_ID,this.calendarId)
        getResult.launch(intent)
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                getDefaultCalendarIdOfDevice()
            }
        }

}
