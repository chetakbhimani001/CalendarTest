package com.calendar.test.view.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract.Events
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.calendar.test.R
import com.calendar.test.databinding.ActivityCreateEventBinding
import com.calendar.test.utils.Constants
import com.calendar.test.viewmodel.AddEventViewModel
import com.calendar.test.viewmodel.MainViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class CreateEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateEventBinding
    private var calId = 1
    var startMillis: Long = 0
    var endMillis: Long = 0
    private val addEventViewModel: AddEventViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEventBinding.inflate(layoutInflater)
        calId = intent.getIntExtra(Constants.CALENDAR_ID, 1)
        val view = binding.root
        setContentView(view)
        binding.eventStartDate.setOnClickListener {
            pickDateTime(1)
        }
        binding.eventEndDate.setOnClickListener {
            pickDateTime(2)
        }
        binding.saveEvent.setOnClickListener {
                addEventViewModel.findCalendarEvents(
                    startFromParam = startMillis,
                    startToParam = endMillis
                )
        }
        addEventViewModel.isEventExist.observe(this) {
            binding.apply {
                when {
                    TextUtils.isEmpty(eventTitle.text.toString()) -> {
                        Toast.makeText(
                            this@CreateEventActivity,
                            getString(R.string.title_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    startMillis == 0L -> {
                        Toast.makeText(
                            this@CreateEventActivity,
                            getString(R.string.start_time_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    endMillis == 0L -> {
                        Toast.makeText(
                            this@CreateEventActivity,
                            getString(R.string.end_time_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    it -> {
                        Toast.makeText(
                            this@CreateEventActivity,
                            getString(R.string.event_overlaps),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        addCalendarEvent(binding)
                    }
                }
            }

        }
    }
    private fun addCalendarEvent(binding: ActivityCreateEventBinding) {
        val cr = contentResolver
        val values = ContentValues()
        values.put(Events.DTSTART, startMillis)
        values.put(Events.DTEND, endMillis)
        values.put(Events.TITLE, binding.eventTitle.text.toString().trim())
        values.put(Events.CALENDAR_ID, calId)
        values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        val uri: Uri? = cr.insert(Events.CONTENT_URI, values)
        val eventID: Long? = uri?.getLastPathSegment()?.toLong()
        val data = Intent()
        data.putExtra("EVENT_ID", eventID)
        setResult(RESULT_OK, data);
        finish()
    }

    private fun pickDateTime(type: Int) {
        val currentDateTime = Calendar.getInstance()
        val startYear = currentDateTime.get(Calendar.YEAR)
        val startMonth = currentDateTime.get(Calendar.MONTH)
        val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
        val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
        val startMinute = currentDateTime.get(Calendar.MINUTE)

        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val pickedDateTime = Calendar.getInstance()
                pickedDateTime.set(year, month, day, hour, minute)
                val today = System.currentTimeMillis()
                if(pickedDateTime.timeInMillis<today-1000 && type == 1){
                    Toast.makeText(this, getString(R.string.invalid_date_selection), Toast.LENGTH_LONG).show();
                  return@TimePickerDialog
                }
               else if(startMillis>pickedDateTime.timeInMillis && type == 2){
                    Toast.makeText(this, getString(R.string.invalid_date_selection), Toast.LENGTH_LONG).show();
                    return@TimePickerDialog
                }
                else{
                    if (type == 1) {
                        startMillis = pickedDateTime.timeInMillis
                        pickedDateTime.set(year, month, day, hour, minute)
                        binding.eventStartDate.setText(Constants.getFormattedDate(pickedDateTime.timeInMillis,Constants.DATE_TIME_FORMAT))
                    } else {
                        endMillis = pickedDateTime.timeInMillis
                        pickedDateTime.set(year, month, day, hour, minute)
                        binding.eventEndDate.setText(Constants.getFormattedDate(pickedDateTime.timeInMillis,Constants.DATE_TIME_FORMAT))
                    }
                }

            }, startHour, startMinute, false).show()
        }, startYear, startMonth, startDay).show()
    }
}