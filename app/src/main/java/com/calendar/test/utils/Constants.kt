package com.calendar.test.utils

import android.provider.CalendarContract
import com.calendar.test.roundToFloat
import com.calendar.test.roundToInt
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    const val CALENDAR_ID: String = "CALENDAR_ID"
    const val DATE_FORMAT = "MMM dd yyyy"
    const val TIME_FORMAT = "hh:mm aaa"
    const  val DATE_FORMAT_1 = "dd/MM/yyyy"
    const  val DATE_TIME_FORMAT = "dd/MM/yyyy hh:mm aaa"

     val EVENT_PROJECTION = arrayOf(
        CalendarContract.Events._ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.STATUS,
        CalendarContract.Events.DTSTART,
        CalendarContract.Events.DTEND,
        CalendarContract.Events.DURATION,
        CalendarContract.Events.ALL_DAY,
        CalendarContract.Events.AVAILABILITY,
        CalendarContract.Events.RRULE,
        CalendarContract.Events.DISPLAY_COLOR,
        CalendarContract.Events.VISIBLE,
    )

     val CALENDAR_PROJECTION = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.NAME,
        CalendarContract.Calendars.CALENDAR_COLOR,
        CalendarContract.Calendars.VISIBLE,
        CalendarContract.Calendars.SYNC_EVENTS,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.ACCOUNT_TYPE,
        CalendarContract.Calendars.IS_PRIMARY
    )

    const val PROJECTION_ID_INDEX = 0
    const val PROJECTION_TITLE_INDEX = 1
    const val PROJECTION_EVENT_LOCATION_INDEX = 2
    const val PROJECTION_STATUS_INDEX = 3
    const val PROJECTION_DTSTART_INDEX = 4
    const val PROJECTION_DTEND_INDEX = 5
    const val PROJECTION_DURATION_INDEX = 6
    const val PROJECTION_ALL_DAY_INDEX = 7
    const val PROJECTION_AVAILABILITY_INDEX = 8
    const val PROJECTION_RRULE_INDEX = 9
    const val PROJECTION_DISPLAY_COLOR_INDEX = 10
    const val PROJECTION_VISIBLE_INDEX = 11


    fun getFormattedDate(time: Long, format: String): String {
        val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        return simpleDateFormat.format(time)
    }
    fun findDuration(
        start_date: Long?,
        end_date: Long?
    ):String {
        val sb = StringBuilder()
        try {
            val differenceInTime = end_date!!- start_date!!
            val differenceInSeconds = ((differenceInTime.toFloat()
                    / 1000))
            val differenceInMinutes = ((differenceInTime.toFloat()
                    / (1000 * 60)))
            val differenceInHours = ((differenceInTime.toFloat()
                    / (1000 * 60 * 60)))
            val difference_In_Days = ((differenceInTime.toFloat()
                    / (1000 * 60 * 60 * 24)))

            if(difference_In_Days>1f)
            {
                val time = difference_In_Days.roundToFloat()
                if(time==1f) sb.append(time.toInt()).append(" day")
                else sb.append(time).append(" days")
            }
            else if(differenceInHours>1f)
            {
                val time = differenceInHours.roundToFloat()
                if(time==1f) sb.append(time.toInt()).append(" hour")
                else sb.append(time).append(" hours")
            }
            else if(differenceInMinutes>1f)
            {
                val time = differenceInMinutes.roundToFloat()
                if(time==1f) sb.append(time.toInt()).append(" min")
                else sb.append(time).append(" mins")
            }
            else if(differenceInSeconds>1f)
            {
                val time = differenceInHours.roundToFloat()
                if(time==1f) sb.append(time.toInt()).append(" sec")
               else  sb.append(time).append(" seconds")
            }

        } // Catch the Exception
        catch (e: ParseException) {
            e.printStackTrace()
        }
        return sb.toString()
    }
}