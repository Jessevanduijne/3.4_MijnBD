package nl.bezorgdirect.mijnbd.recyclerviews

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_my_bdavailability.view.*
import kotlinx.android.synthetic.main.activity_my_bdavailability.view.lbl_endtime
import kotlinx.android.synthetic.main.activity_my_bdavailability.view.lbl_starttime
import kotlinx.android.synthetic.main.activity_my_bdhistory_details.view.lbl_date
import kotlinx.android.synthetic.main.availability_item.view.*
import nl.bezorgdirect.mijnbd.MijnbdApplication
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Availability
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AvailabilityAdapter(var list: ArrayList<Availability>) : RecyclerView.Adapter<AvailabilityAdapter.MyViewHolder>() {

    var cont: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.availability_item, parent, false)
        cont = parent.context
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        if(item.Date != null){
            var formattedDate = item.Date!!.substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date")
            formattedDate = formattedDate.substring(5)
            holder.date.text = formattedDate
        }
        else holder.date.text = "??-??"
        if(item.StartTime != null){
            var formattedStartTime = item.StartTime!!.substring(0, 5)
            holder.starttime.text = formattedStartTime
        }
        else holder.starttime.text = "??:??"
        if(item.EndTime != null){
            var formattedEndTime = item.EndTime!!.substring(0, 5)
            holder.endtime.text = formattedEndTime
        }
        else holder.endtime.text = "??:??"

        holder.btn_delete.setOnClickListener {
            var id = ""
            if(list[position].Id != null)
            {
                 id = list[position].Id!!
            }
            deleteDialog(id, position)
        }
        holder.btn_edit.setOnClickListener {
            editDialog(list[position], position)
        }
    }

    override fun getItemCount(): Int = list.size


    class MyViewHolder(availability_item: View) : RecyclerView.ViewHolder(availability_item){
        val date: TextView = availability_item.lbl_date
        val starttime: TextView = availability_item.lbl_starttime
        val endtime: TextView = availability_item.lbl_endtime
        val btn_delete: ImageButton = availability_item.btn_delete
        val btn_edit: ImageButton = availability_item.btn_edit
    }
    fun deleteDialog(id :String, listId :Int)
    {
        val dialog = Dialog(cont!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_delete_availability)
        val window = dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val btn_confirm = dialog .findViewById(R.id.btn_confirmDeleteAvailability) as Button
        btn_confirm.setOnClickListener {
            deleteAvailability(id, listId)
            dialog.dismiss()
        }
        val btn_cancel = dialog .findViewById(R.id.btn_closeDeleteAvailability) as Button
        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun editDialog(params :Availability, listId :Int)
    {
        val dialog = Dialog(cont!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_add_availability)

        val title = dialog.findViewById(R.id.lbl_dialog_title) as TextView
        title.text = "Edit availability"

        val window = dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val fromHourPicker = dialog.findViewById(R.id.pkr_from_hour) as NumberPicker
        val fromMinPicker = dialog.findViewById(R.id.pkr_from_min) as NumberPicker
        val toHourPicker = dialog.findViewById(R.id.pkr_to_hour) as NumberPicker
        val toMinPicker = dialog.findViewById(R.id.pkr_to_min) as NumberPicker
        val dayPicker = dialog.findViewById(R.id.pkr_date) as NumberPicker
        val hours = arrayOf("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
        val mins = arrayOf("00", "15", "30", "45")
        val dates = getDates()

        val fromParts = params.StartTime!!.split(":")
        val toParts = params.EndTime!!.split(":")

        val fromHourIndex = hours.indexOf(fromParts[0])
        val fromMinIndex = hours.indexOf(fromParts[1])
        val toHourIndex = hours.indexOf(toParts[0])
        val toMinIndex = hours.indexOf(toParts[1])


        fromHourPicker.minValue = 0
        fromHourPicker.maxValue = hours.size-1
        fromHourPicker.displayedValues = hours
        fromHourPicker.value = fromHourIndex


        fromMinPicker.minValue = 0
        fromMinPicker.maxValue = mins.size-1
        fromMinPicker.displayedValues = mins
        fromMinPicker.value = fromMinIndex

        toHourPicker.value = toHourIndex
        toHourPicker.minValue = 0
        toHourPicker.maxValue = hours.size-1
        toHourPicker.displayedValues = hours
        toHourPicker.value = toHourIndex


        toMinPicker.minValue = 0
        toMinPicker.maxValue = mins.size-1
        toMinPicker.displayedValues = mins
        toMinPicker.value = toMinIndex
        toHourPicker.setOnValueChangedListener{ _, _, newVal ->

            val availableHours = hours.slice(fromHourPicker.minValue..newVal)

            fromHourPicker.displayedValues = null
            fromHourPicker.maxValue = newVal
            fromHourPicker.displayedValues = availableHours.toTypedArray()


            val availableMins = mins.slice(fromHourPicker.minValue..toMinPicker.value)
            fromMinPicker.displayedValues = null
            fromMinPicker.maxValue = toMinPicker.value
            fromMinPicker.displayedValues = availableMins.toTypedArray()
        }
        fromHourPicker.setOnValueChangedListener{ _, _, newVal ->
            val availableHours = hours.slice(newVal..toHourPicker.maxValue)
            toHourPicker.displayedValues = null
            toHourPicker.minValue = newVal
            toHourPicker.displayedValues = availableHours.toTypedArray()

            val availableMins = mins.slice(fromHourPicker.minValue..toMinPicker.value)
            fromMinPicker.displayedValues = null
            fromMinPicker.maxValue = toMinPicker.value
            fromMinPicker.displayedValues = availableMins.toTypedArray()
        }
        fromMinPicker.setOnValueChangedListener{ _, _, newVal ->

            val availableMins = mins.slice(newVal..toMinPicker.maxValue)
            toMinPicker.displayedValues = null
            toMinPicker.minValue = newVal
            toMinPicker.displayedValues = availableMins.toTypedArray()
        }
        toMinPicker.setOnValueChangedListener{ _, _, newVal ->
            println("toMinPicker $newVal")
            val availableMins = mins.slice(fromHourPicker.minValue..newVal)
            fromMinPicker.displayedValues = null
            fromMinPicker.maxValue = toMinPicker.value
            fromMinPicker.displayedValues = availableMins.toTypedArray()
        }

        var dateString = params.Date!!.substringBefore(delimiter = 'T', missingDelimiterValue = "")
        val dateParts = dateString.split("-")
        dateString = dateParts[2]+"-"+dateParts[1]+"-"+dateParts[0]

        val dateIndex = dates.indexOf(dateString)
        dayPicker.value = dateIndex
        dayPicker.minValue = 0
        dayPicker.maxValue = dates.size-1
        println(dates.size)
        val dateArray = arrayOfNulls<String>(dates.size)
        dayPicker.displayedValues = dates.toArray(dateArray)
        dayPicker.value = dateIndex


        val btn_confirm = dialog.findViewById(R.id.btn_confirmAvailability) as Button
        btn_confirm.setOnClickListener {

            val fromHour = hours[fromHourPicker.value]
            val fromMin = mins[fromMinPicker.value]
            val toHour = hours[toHourPicker.value]
            val toMin = mins[toMinPicker.value]

            val inputFormat = SimpleDateFormat("dd-MM-yyyy")
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val fullDate: Date = inputFormat.parse(dates[dayPicker.value])
            val date = outputFormat.format(fullDate)

            params.Date = date
            params.StartTime = "$fromHour:$fromMin"
            params.EndTime = "$toHour:$toMin"

            updateAvailability(params, listId)
            dialog.dismiss()
        }
        val btn_cancel = dialog .findViewById(R.id.btn_closeAddAvailability) as Button
        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun getDates() : ArrayList<String>
    {
        val fromatter = SimpleDateFormat("dd-MM-yyyy")
        val today = Date()

        val dates = ArrayList<String>()
        for (i in 0..62)
        {
            val day = Date(today.time + (1000 * 60 * 60 * 24) * i)
            dates.add(fromatter.format(day))
        }
        return dates
    }

    fun deleteAvailability(id: String, listId: Int)
    {
        val service = getApiService()
        val decryptedToken = getDecryptedToken(cont!!)

        service.availablitiesDelete(auth = decryptedToken, id = id).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                println(response)
                if (response.code() == 500) {
                    Toast.makeText(
                        MijnbdApplication.appContext,
                        MijnbdApplication.appContext.resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        MijnbdApplication.appContext,
                        MijnbdApplication.appContext.resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                } else if (response.isSuccessful && response.body() != null) {
                    list.removeAt(listId)
                    notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    cont!!, MijnbdApplication.appContext.resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

        })
    }
    fun updateAvailability(availability: Availability, listId: Int)
    {
        val service = getApiService()
        val decryptedToken = getDecryptedToken(cont!!)
        val params  = ArrayList<Availability>()
        params.add(availability)
        service.availablitiesPut(auth = decryptedToken, availability = params).enqueue(object :
            Callback<ArrayList<Availability>> {
            override fun onResponse(
                call: Call<ArrayList<Availability>>,
                response: Response<ArrayList<Availability>>
            ) {
                if (response.code() == 500) {
                    Toast.makeText(
                        cont!!,
                        cont!!.resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        cont!!,
                        cont!!.resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    list[listId] = values[0]
                    sortAvailabilities()
                    notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    cont!!, cont!!.resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

        })
    }

    fun sortAvailabilities()
    {
        list = ArrayList(list.sortedWith(compareBy({it.Date}, {it.StartTime})))
    }



}