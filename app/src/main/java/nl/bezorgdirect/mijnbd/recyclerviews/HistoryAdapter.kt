package nl.bezorgdirect.mijnbd.recyclerviews

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.history_item.view.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


class HistoryAdapter (val list: ArrayList<Delivery>, val clicklistener: HistoryListener) : RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {

    private var cont: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        cont = parent.context
        return HistoryAdapter.MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        if(item.deliveredAt != null && item.warehousePickUpAt != null) {
            val starttime: Date = inputFormat.parse(item.warehousePickUpAt)//acceptedAt wharhouse pickup for now
            val endtime: Date = inputFormat.parse(item.deliveredAt)
            holder.travel.text = setTravelTime(starttime, endtime)
            holder.time.text = setTimeFromTo(starttime, endtime)
        }
        else
        {
            holder.time.text = String.format("%s - %s",cont?.resources?.getString(R.string.unknown),cont?.resources?.getString(R.string.unknown))
            holder.travel.text = String.format("%s %s - %s %s",cont?.resources?.getString(R.string.unknown),cont?.resources?.getString(R.string.lbl_hours_short),cont?.resources?.getString(R.string.unknown),cont?.resources?.getString(R.string.lbl_minutes_short))
        }
        if(item.status == 4)
        {
            holder.successimage.setImageResource(R.drawable.ic_success_56dp)
        }
        else
        {
            holder.successimage.setImageResource(R.drawable.ic_failed_56dp)
        }
        when(item.vehicle)
        {
            1 -> holder.vehicleimage.setImageResource(R.drawable.ic_bike_w)
            2 -> holder.vehicleimage.setImageResource(R.drawable.ic_motor_w)
            3 -> holder.vehicleimage.setImageResource(R.drawable.ic_motor_w)
            4 -> holder.vehicleimage.setImageResource(R.drawable.ic_car_w)
        }
        holder.location.text = item.customer.address
        println(item)
        if(item.customerDistanceInKilometers != null && item.warehouseDistanceInKilometers != null) {
            val distance = item.customerDistanceInKilometers + item.warehouseDistanceInKilometers
           // distance = round(distance * 100) / 100
            holder.distance.text = String.format("%.2f %s",distance,cont?.resources?.getString(R.string.lbl_kilometers_short))
        }
        else
        {
            holder.distance.text = String.format("%s %s",cont?.resources?.getString(R.string.unknown),cont?.resources?.getString(R.string.lbl_kilometers_short))
        }
        if(item.price != null) {
            val earnings = round(item.price!! * 100) / 100
            holder.earnings.text = String.format("%s%.2f",cont?.resources?.getString(R.string.lbl_euro),earnings)
        }
        else
        {
            holder.earnings.text = cont?.resources?.getString(R.string.unknown)
        }
        holder.itemView.setOnClickListener{
            clicklistener.onItemClick(position)
        }
    }

    fun setTravelTime(start: Date, end: Date): String
    {
        val diff = end.time - start.time
        val seconds = diff / 1000
        var minutes = seconds / 60
        val hours = minutes / 60
        minutes -= hours * 60
        println(diff)
        var traveltime = ""
        if (hours > 0) {
            traveltime += String.format("%d %s",hours,cont?.resources?.getString(R.string.lbl_hours_short))
        }
        if (minutes > 0) {
            traveltime += String.format("%d %s",minutes,cont?.resources?.getString(R.string.lbl_minutes_short))
        }
        if(minutes == 0L && hours == 0L)
        {
            traveltime = String.format("%d %s", minutes,cont?.resources?.getString(R.string.lbl_minutes_short))
        }
        else
        {
            traveltime = String.format("%s %s",cont?.resources?.getString(R.string.unknown),cont?.resources?.getString(R.string.lbl_minutes_short))
        }
        return traveltime
    }
    fun setTimeFromTo(start: Date, end: Date): String
    {
        val outputFormat = SimpleDateFormat("HH:mm")
        val formattedstart: String = outputFormat.format(start)
        val formattedend: String = outputFormat.format(end)

        return String.format("%s - %s",formattedstart,formattedend)
    }

    class MyViewHolder(history_item: View) : RecyclerView.ViewHolder(history_item) {
        val successimage: ImageView = history_item.img_succes
        val vehicleimage: ImageView = history_item.img_vehicle

        val location: TextView = history_item.lbl_location
        val time: TextView = history_item.lbl_time
        val travel: TextView = history_item.lbl_traveltime
        val distance: TextView = history_item.lbl_distance
        val earnings: TextView = history_item.lbl_earnings

        val context: Context = history_item.context

    }

}