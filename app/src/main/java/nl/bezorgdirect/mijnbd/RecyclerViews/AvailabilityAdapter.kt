package nl.bezorgdirect.mijnbd.RecyclerViews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.availability_item.view.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Availability

class AvailabilityAdapter(val list: ArrayList<Availability>) : RecyclerView.Adapter<AvailabilityAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.availability_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvailabilityAdapter.MyViewHolder, position: Int) {
        val item = list[position]
        if(item.Date != null){
            var formattedDate = item.Date.substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date")
            formattedDate = formattedDate.substring(5)
            holder.date.text = formattedDate
        }
        else holder.date.text = "??-??"
        if(item.StartTime != null){
            var formattedStartTime = item.StartTime.substring(0, 5)
            holder.starttime.text = formattedStartTime
        }
        else holder.starttime.text = "??:??"
        if(item.EndTime != null){
            var formattedEndTime = item.EndTime.substring(0, 5)
            holder.endtime.text = formattedEndTime
        }
        else holder.endtime.text = "??:??"
    }

    override fun getItemCount(): Int = list.size


    class MyViewHolder(availability_item: View) : RecyclerView.ViewHolder(availability_item){
        val date: TextView = availability_item.lbl_date
        val starttime: TextView = availability_item.lbl_starttime
        val endtime: TextView = availability_item.lbl_endtime
    }


}