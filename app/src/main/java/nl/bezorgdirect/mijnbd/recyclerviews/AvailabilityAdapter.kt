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
import nl.bezorgdirect.mijnbd.helpers.hideSpinner
import nl.bezorgdirect.mijnbd.helpers.showSpinner
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AvailabilityAdapter(var list: ArrayList<Availability>) : RecyclerView.Adapter<AvailabilityAdapter.MyViewHolder>() {

    var cont: Context? = null
    var root: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.availability_item, parent, false)
        cont = parent.context
        root = parent.rootView
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        if(item.date != null){
            var formattedDate = item.date!!.substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date")
            formattedDate = formattedDate.substring(5)
            holder.date.text = formattedDate
        }
        else holder.date.text = "??-??"
        if(item.startTime != null){
            var formattedStartTime = item.startTime!!.substring(0, 5)
            holder.starttime.text = formattedStartTime
        }
        else holder.starttime.text = "??:??"
        if(item.endTime != null){
            var formattedEndTime = item.endTime!!.substring(0, 5)
            holder.endtime.text = formattedEndTime
        }
        else holder.endtime.text = "??:??"

        holder.btn_delete.setOnClickListener {
            var id = ""
            if(list[position].id != null)
            {
                 id = list[position].id!!
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
        val hours = arrayOf("09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
        val mins = arrayOf("00", "15", "30", "45")
        val dates = getDates()

        val fromParts = params.startTime!!.split(":")
        val toParts = params.endTime!!.split(":")

        println(fromParts)
        println(toParts)
        val fromHourIndex = hours.indexOf(fromParts[0])
        val fromMinIndex = mins.indexOf(fromParts[1])
        val toHourIndex = hours.indexOf(toParts[0])
        val toMinIndex = mins.indexOf(toParts[1])

        initValsPicker(fromHourPicker, hours)
        initValsPicker(toHourPicker, hours)
        initValsPicker(fromMinPicker, mins)
        initValsPicker(toMinPicker, mins)

        toHourPicker.value = toHourIndex
        fromHourPicker.value = fromHourIndex
        toMinPicker.value = toMinIndex
        fromMinPicker.value = fromMinIndex


        var dateString = params.date!!.substringBefore(delimiter = 'T', missingDelimiterValue = "")
        val dateParts = dateString.split("-")
        dateString = dateParts[2]+"-"+dateParts[1]+"-"+dateParts[0]
        val dateIndex = dates.indexOf(dateString)
        initValsPicker(dayPicker, dates.toTypedArray())
        dayPicker.value = dateIndex

        toHourPicker.setOnValueChangedListener{ _, _, newVal ->
            toHourPickerChanged(fromHourPicker, toHourPicker, fromMinPicker, toMinPicker, hours, mins)
        }
        fromHourPicker.setOnValueChangedListener{ _, _, newVal ->
            fromHourPickerChanged(fromHourPicker, toHourPicker, fromMinPicker, toMinPicker, hours, mins)
        }
        fromMinPicker.setOnValueChangedListener{ _, _, newVal ->
            fromMinPickerChanged(fromHourPicker, toHourPicker, fromMinPicker, toMinPicker, hours, mins)
        }
        toMinPicker.setOnValueChangedListener{ _, _, newVal ->
            toMinPickerChanged(fromHourPicker, toHourPicker, fromMinPicker, toMinPicker, hours, mins)
        }

        val btn_confirm = dialog.findViewById(R.id.btn_confirmAvailability) as Button
        btn_confirm.setOnClickListener {
            val updateParams = params
            val fromHour = hours[fromHourPicker.value]
            val fromMin = mins[fromMinPicker.value]
            val toHour = hours[toHourPicker.value]
            val toMin = mins[toMinPicker.value]

            val inputFormat = SimpleDateFormat("dd-MM-yyyy")
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val fullDate: Date = inputFormat.parse(dates[dayPicker.value])
            val date = outputFormat.format(fullDate)

            updateParams.date = date
            updateParams.startTime = "$fromHour:$fromMin"
            updateParams.endTime = "$toHour:$toMin"

            if("$fromHour:$fromMin" != "$toHour:$toMin") {
                dialog.dismiss()
                updateAvailability(updateParams, listId)
            }
            else
            {
                Toast.makeText(
                    cont, "Start en eind tijd mogen niet hetzelfde zijn.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        val btn_cancel = dialog .findViewById(R.id.btn_closeAddAvailability) as Button
        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    fun initValsPicker(picker: NumberPicker, values: Array<String>)
    {
        picker.displayedValues = null
        picker.minValue = 0
        picker.maxValue = values.size-1
        picker.displayedValues = values
    }

    fun toHourPickerChanged(fromHourPicker: NumberPicker, toHourPicker: NumberPicker, fromMinPicker: NumberPicker, toMinPicker: NumberPicker, hours: Array<String>, mins: Array<String>)
    {
        val availableHours = hours.slice(fromHourPicker.minValue..toHourPicker.value)
        initValsPicker(fromHourPicker, availableHours.toTypedArray())

        if(hours[fromHourPicker.value] == hours[toHourPicker.value])
        {
            val availableMins = ArrayList<String>()
            availableMins.addAll(mins.slice(fromMinPicker.minValue..toMinPicker.value))
            initValsPicker(fromMinPicker, availableMins.toTypedArray())
        }
        else
        {
            initValsPicker(fromMinPicker, mins)
            initValsPicker(toMinPicker, mins)
        }
    }
    fun fromHourPickerChanged(fromHourPicker: NumberPicker, toHourPicker: NumberPicker, fromMinPicker: NumberPicker, toMinPicker: NumberPicker, hours: Array<String>, mins: Array<String>)
    {
        val availableHours = hours.slice(fromHourPicker.value..toHourPicker.maxValue)
        toHourPicker.displayedValues = null
        toHourPicker.minValue = fromHourPicker.value
        toHourPicker.displayedValues = availableHours.toTypedArray()

        if(hours[fromHourPicker.value] == hours[toHourPicker.value])
        {
            val availableMins = ArrayList<String>()
            availableMins.addAll(mins.slice(fromMinPicker.minValue..toMinPicker.value))
            initValsPicker(fromMinPicker, availableMins.toTypedArray())
        }
        else
        {
            initValsPicker(fromMinPicker, mins)
            initValsPicker(toMinPicker, mins)
        }
    }
    fun fromMinPickerChanged(fromHourPicker: NumberPicker, toHourPicker: NumberPicker, fromMinPicker: NumberPicker, toMinPicker: NumberPicker, hours: Array<String>, mins: Array<String>)
    {
        if(hours[fromHourPicker.value] == hours[toHourPicker.value])
        {
            val availableMins = mins.slice(fromMinPicker.value..toMinPicker.maxValue)
            toMinPicker.displayedValues = null
            toMinPicker.minValue = fromMinPicker.value
            toMinPicker.displayedValues = availableMins.toTypedArray()
        }
        else
        {
            initValsPicker(toMinPicker, mins)
        }
    }
    fun toMinPickerChanged(fromHourPicker: NumberPicker, toHourPicker: NumberPicker, fromMinPicker: NumberPicker, toMinPicker: NumberPicker, hours: Array<String>, mins: Array<String>)
    {
        if(hours[fromHourPicker.value] == hours[toHourPicker.value])
        {
            val availableMins = mins.slice(fromHourPicker.minValue..toMinPicker.value)
            fromMinPicker.displayedValues = null
            fromMinPicker.maxValue = toMinPicker.value
            fromMinPicker.displayedValues = availableMins.toTypedArray()
        }
        else
        {
            initValsPicker(fromMinPicker, mins)
        }
    }
    fun getDates() : ArrayList<String>
    {
        val fromatter = SimpleDateFormat("dd-MM-yyyy")
        val day = Calendar.getInstance()
        day.time = Date()

        val dates = ArrayList<String>()
        for (i in 0..62)
        {
            dates.add(fromatter.format(day.time))
            day.add(Calendar.DATE, 1)
        }
        return dates
    }
    fun deleteAvailability(id: String, listId: Int)
    {
        showSpinner(root!!)
        val service = getApiService()
        val decryptedToken = getDecryptedToken(cont!!)

        val availability_content: LinearLayout = root!!.findViewById(R.id.availability_content)
        val availability_empty: LinearLayout = root!!.findViewById(R.id.availability_empty)


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
                    hideSpinner(root!!)
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        MijnbdApplication.appContext,
                        MijnbdApplication.appContext.resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    hideSpinner(root!!)
                } else if (response.isSuccessful && response.body() != null) {
                    list.removeAt(listId)
                    notifyDataSetChanged()
                    if(list.size == 0)
                    {
                        availability_content.visibility = View.GONE
                        availability_empty.visibility = View.VISIBLE
                    }
                    hideSpinner(root!!)

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    cont!!, MijnbdApplication.appContext.resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                hideSpinner(root!!)
            }

        })
    }
    fun updateAvailability(availability: Availability, listId: Int)
    {
        showSpinner(root!!)
        val service = getApiService()
        val decryptedToken = getDecryptedToken(cont!!)
        val params  = ArrayList<Availability>()
        println(availability)
        params.add(availability)
        service.availablitiesPut(auth = decryptedToken, availability = params).enqueue(object :
            Callback<ArrayList<Availability>> {
            override fun onResponse(
                call: Call<ArrayList<Availability>>,
                response: Response<ArrayList<Availability>>
            ) {
                println(response)
                if (response.code() == 500) {
                    Toast.makeText(
                        cont!!,
                        cont!!.resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                    hideSpinner(root!!)
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        cont!!,
                        cont!!.resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    hideSpinner(root!!)
                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    list[listId] = values[0]
                    list.sortBy{it.date+' '+it.startTime}
                    notifyDataSetChanged()
                    hideSpinner(root!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    cont!!, cont!!.resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                hideSpinner(root!!)
            }

        })
    }




}