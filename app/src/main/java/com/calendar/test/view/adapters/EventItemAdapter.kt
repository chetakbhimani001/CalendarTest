package com.calendar.test.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.calendar.test.R
import com.calendar.test.models.EventItem
import com.calendar.test.models.HeaderEvent
import com.calendar.test.utils.Constants


class EventItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val SECTION_VIEW = 0
    val CONTENT_VIEW = 1
    var data: MutableList<HeaderEvent> = mutableListOf()

    fun clearData() {
        data.clear()
        notifyDataSetChanged()
    }

    fun pushData(eventItem: MutableList<HeaderEvent>) {
        data.addAll(eventItem)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SECTION_VIEW) {
            return SectionHeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.date_header, parent, false)
            )
        } else {
            return ItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_event, parent, false)
            )
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (SECTION_VIEW == getItemViewType(position)) {
            val sectionHeaderViewHolder = holder as SectionHeaderViewHolder
            val sectionItem: HeaderEvent = data.get(position)
            sectionHeaderViewHolder.headerTitleTextview.text = sectionItem.date
            return
        }
        val itemViewHolder = holder as ItemViewHolder
        val datum: EventItem = data.get(position).items as EventItem
        var sb = StringBuilder()
        sb.append(datum.title).append(" : ")
        if(datum.dtStart != null)
            sb.append(Constants.getFormattedDate(
            datum.dtStart,
            Constants.TIME_FORMAT
        ))
        sb.append("-")
        if (datum.dtEnd != null)
        sb.append(Constants.getFormattedDate(datum.dtEnd, Constants.TIME_FORMAT))
        sb.append("(")
        sb.append(Constants.findDuration(
            datum.dtStart, datum.dtEnd
        ))
        sb.append(")")
        holder.textTitle.text = sb.toString()
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].isSection) {
            SECTION_VIEW
        } else {
            CONTENT_VIEW
        }
    }

    class ItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView
        init {
            textTitle = itemView.findViewById(R.id.text_title)
        }
    }

    class SectionHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var headerTitleTextview: TextView

        init {
            headerTitleTextview = itemView.findViewById<View>(R.id.txtDate) as TextView
        }
    }


}