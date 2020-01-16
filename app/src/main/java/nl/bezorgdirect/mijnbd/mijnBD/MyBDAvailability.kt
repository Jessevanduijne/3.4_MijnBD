package nl.bezorgdirect.mijnbd.mijnBD

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_my_bdavailability.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.AddAvailabilityParams
import nl.bezorgdirect.mijnbd.api.Availability
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import nl.bezorgdirect.mijnbd.helpers.hideSpinner
import nl.bezorgdirect.mijnbd.helpers.showSpinner
import nl.bezorgdirect.mijnbd.recyclerviews.AvailabilityAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


private lateinit var linearLayoutManager: LinearLayoutManager

class MyBDAvailability : AppCompatActivity() {

    private var availabilities: ArrayList<Availability> = ArrayList()
    private var cont :Context? = null
    private var changed : Int = 1
    private var activeCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bdavailability)

        val custom_toolbar_title: TextView = this.findViewById(R.id.custom_toolbar_title)
        custom_toolbar_title.text = getString(R.string.lbl_mybdpersonalia)
        setSupportActionBar(custom_toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)


        cont = this
        addAvailabilityLayout.visibility = View.GONE

        var listitems = AvailabilityAdapter(availabilities)
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)

        val btn_addAvailability: FloatingActionButton = findViewById(R.id.btn_addAvailability)
        val btn_retry_availability: Button = findViewById(R.id.btn_retry_availability)
        val lbl_error: TextView = findViewById(R.id.lbl_error)

        lbl_error.visibility = View.GONE

        btn_addAvailability.setOnClickListener{
            addDialog()
        }


        linearLayoutManager = LinearLayoutManager(this)
        list_availabilities.layoutManager = linearLayoutManager
        list_availabilities.adapter = listitems

        swp_availability.setColorSchemeResources(
            R.color.colorPrimaryDark
        )

        swp_availability.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.colorPrimary))

        swp_availability.setOnRefreshListener {
            if(!activeCall) {
                getAvailabilities(applicationContext)
            }
            else
            {
                swp_availability.isRefreshing = false
            }
        }

        btn_retry_availability.setOnClickListener{
            getAvailabilities(applicationContext)
        }

        getAvailabilities(applicationContext)


    }

    private fun getAvailabilities(context: Context){

        activeCall = true
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)
        val root : View = this.findViewById(android.R.id.content)
        if(!swp_availability.isRefreshing)
        {
            showSpinner(root)
        }

        val service = getApiService()
        val decryptedToken = getDecryptedToken(this)

        service.availablitiesGet(auth = decryptedToken).enqueue(object : Callback<ArrayList<Availability>> {
            override fun onResponse(
                call: Call<ArrayList<Availability>>,
                response: Response<ArrayList<Availability>>
            ) {

                if (response.code() == 500) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                    swp_availability.isRefreshing = false
                    hideSpinner(root)
                    availability_empty.visibility = View.GONE
                    availability_error.visibility = View.VISIBLE
                    availability_content.visibility = View.GONE
                    activeCall = false
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    swp_availability.isRefreshing = false
                    hideSpinner(root)
                    availability_empty.visibility = View.GONE
                    availability_error.visibility = View.VISIBLE
                    availability_content.visibility = View.GONE
                    activeCall = false
                }
                else if (response.code() == 204) {
                    availability_empty.visibility = View.VISIBLE
                    availability_error.visibility = View.GONE
                    availability_content.visibility = View.GONE
                    activeCall = false
                    hideSpinner(root)
                }else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!

                    if(values.size > 0)
                    {
                        if(values[0] != null)
                        {
                            availabilities = values
                            list_availabilities.adapter = AvailabilityAdapter(availabilities)
                        }
                    }
                    swp_availability.isRefreshing = false
                    hideSpinner(root)
                    availability_empty.visibility = View.GONE
                    availability_error.visibility = View.GONE
                    availability_content.visibility = View.VISIBLE
                    activeCall = false
                    //list_availabilities.adapter?.notifyDataSetChanged()
                }
                else{
                    swp_availability.isRefreshing = false
                    hideSpinner(root)
                    availability_empty.visibility = View.GONE
                    availability_error.visibility = View.VISIBLE
                    availability_content.visibility = View.GONE
                    activeCall = false
                }

            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                swp_availability.isRefreshing = false
                hideSpinner(root)
                availability_empty.visibility = View.GONE
                availability_error.visibility = View.VISIBLE
                availability_content.visibility = View.GONE
                activeCall = false
                return
            }

        })
    }

    private fun postAvailabilities(context: Context, params: ArrayList<AddAvailabilityParams>){
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)

        val root : View = this.findViewById(android.R.id.content)
        showSpinner(root)

        val service = getApiService()
        val decryptedToken = getDecryptedToken(this)

        service.availablitiesPost(auth = decryptedToken, availabilityPost = params).enqueue(object : Callback<ArrayList<Availability>> {
            override fun onResponse(
                call: Call<ArrayList<Availability>>,
                response: Response<ArrayList<Availability>>
            ) {

                if (response.code() == 500) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                    hideSpinner(root)
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    hideSpinner(root)
                }
                else if (response.code() == 204) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    availability_empty.visibility = View.VISIBLE
                    availability_error.visibility = View.GONE
                    availability_content.visibility = View.GONE
                    hideSpinner(root)
                }else if (response.isSuccessful && response.body() != null) {
                    changed = 1
                    val values = response.body()!!
                    availabilities.addAll(values)
                    availabilities.sortBy{it.Date+' '+it.StartTime}
                    list_availabilities.adapter!!.notifyDataSetChanged()
                    Toast.makeText(context, "Availability added!", Toast.LENGTH_SHORT).show()
                    hideSpinner(root)
                    availability_empty.visibility = View.GONE
                    availability_error.visibility = View.GONE
                    availability_content.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                hideSpinner(root)
                return
            }

        })
    }
    fun addDialog()
    {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_add_availability)
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
        initValsPicker(fromHourPicker, hours)
        initValsPicker(toHourPicker, hours)
        initValsPicker(fromMinPicker, mins)
        initValsPicker(toMinPicker, mins)
        initValsPicker(dayPicker, dates.toTypedArray())

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

        val dateArray = arrayOfNulls<String>(dates.size)
        dayPicker.displayedValues = dates.toArray(dateArray)

        val btn_confirm = dialog .findViewById(R.id.btn_confirmAvailability) as Button
        btn_confirm.setOnClickListener {

            val fromHour = hours[fromHourPicker.value]
            val fromMin = mins[fromMinPicker.value]
            val toHour = hours[toHourPicker.value]
            val toMin = mins[toMinPicker.value]
            val params = ArrayList<AddAvailabilityParams>()

            val inputFormat = SimpleDateFormat("dd-MM-yyyy")
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val fullDate: Date = inputFormat.parse(dates[dayPicker.value])
            val date = outputFormat.format(fullDate)
            if("$fromHour:$fromMin" != "$toHour:$toMin") {
                dialog.dismiss()

                params.add(AddAvailabilityParams(date, "$fromHour:$fromMin", "$toHour:$toMin"))
                postAvailabilities(cont!!, params)
            }
            else
            {
                Toast.makeText(
                    this, "Start en eind tijd mogen niet hetzelfde zijn.",
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
            day.add(Calendar.DATE, 1)
            dates.add(fromatter.format(day.time))
        }
        return dates
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                val returnIntent = Intent()
                returnIntent.putExtra("result", changed)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

}
