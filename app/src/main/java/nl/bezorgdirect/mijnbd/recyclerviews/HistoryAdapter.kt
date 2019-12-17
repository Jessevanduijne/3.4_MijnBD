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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        val outputFormat = SimpleDateFormat("HH:mm")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val start = "2019-12-06T15:16:25.84Z" //needs to be added to api so now dummy value
        if(item.DeliveredAt != null && start != null) {
            val starttime: Date = inputFormat.parse(start)
            val endtime: Date = inputFormat.parse(item.DeliveredAt)
            println("$starttime||$endtime")
            println(starttime.time)
            println(endtime.time)
            val diff = starttime.time - endtime.time
            val seconds = diff / 1000
            var minutes = seconds / 60
            val hours = minutes / 60
            minutes -= hours * 60

            var traveltime = ""
            if (hours > 0) {
                traveltime += "$hours u. "
            }
            if (minutes > 0) {
                traveltime += "$minutes min."
            }
            holder.travel.text = traveltime

            val formattedstart: String = outputFormat.format(starttime)
            val formattedend: String = outputFormat.format(endtime)

            val fromto = "$formattedstart - $formattedend"
            holder.time.text = fromto
        }
        else
        {
            holder.time.text = "? - ?"
            holder.travel.text = "? u. ? min."
        }

        if(item.Status == 4)
        {
            holder.successimage.setImageResource(R.drawable.ic_success_56dp)
        }
        if(item.Status == 0)
        {
            holder.successimage.setImageResource(R.drawable.ic_failed_56dp)
        }
        when(item.Vehicle)
        {
            1 -> holder.vehicleimage.setImageResource(R.drawable.ic_bike_w)
            2 -> holder.vehicleimage.setImageResource(R.drawable.ic_motor_w)
            3 -> holder.vehicleimage.setImageResource(R.drawable.ic_motor_w)
            4 -> holder.vehicleimage.setImageResource(R.drawable.ic_car_w)
        }

        holder.location.text = item.Customer.Address


        println(item)
        if(item.CustomerDistanceInKilometers != null && item.WarehouseDistaceInKilometers != null) {
            var distance = item.CustomerDistanceInKilometers!! + item.WarehouseDistaceInKilometers!!
            distance = round(distance * 100) / 100
            holder.distance.text = "$distance km."
        }
        else
        {
            holder.distance.text = "? km."
        }
        if(item.Price != null) {
            val earnings = round(item.Price!! * 100) / 100
            holder.earnings.text = "€$earnings"
        }
        else
        {
            holder.earnings.text = "?"
        }


        holder.itemView.setOnClickListener{
            clicklistener.onItemClick(position)
        }
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