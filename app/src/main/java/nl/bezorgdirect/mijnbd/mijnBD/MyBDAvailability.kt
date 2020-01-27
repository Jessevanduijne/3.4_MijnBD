package nl.bezorgdirect.mijnbd.mijnBD

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
    private var changed : Int = 1
    private var activeCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bdavailability)

        val custom_toolbar_title: TextView = this.findViewById(R.id.custom_toolbar_title)
        custom_toolbar_title.text = getString(R.string.lbl_mybdavailability)
        setSupportActionBar(custom_toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

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
                    setErrorVisibility(root)
                    activeCall = false
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    setErrorVisibility(root)
                    activeCall = false
                }
                else if (response.code() == 424) {
                    setEmptyVisibility(root)
                }else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!

                    if(values.size > 0)
                    {
                        if(values[0] != null)
                        {
                            availabilities = values
                            filterAndSortAvailabilities()
                            list_availabilities.adapter = AvailabilityAdapter(availabilities)
                        }
                    }
                    setSuccessVisibility(root)
                    activeCall = false
                }
                else{
                    setErrorVisibility(root)
                    activeCall = false
                }

            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                setErrorVisibility(root)
                activeCall = false
                return
            }

        })
    }
    fun setSuccessVisibility(root :View)
    {
        swp_availability.isRefreshing = false
        hideSpinner(root)
        availability_empty.visibility = View.GONE
        availability_error.visibility = View.GONE
        availability_content.visibility = View.VISIBLE
    }
    fun setEmptyVisibility(root :View)
    {
        availability_empty.visibility = View.VISIBLE
        availability_error.visibility = View.GONE
        availability_content.visibility = View.GONE
        activeCall = false
        hideSpinner(root)
    }
    fun setErrorVisibility(root: View)
    {
        swp_availability.isRefreshing = false
        hideSpinner(root)
        availability_empty.visibility = View.GONE
        availability_error.visibility = View.VISIBLE
        availability_content.visibility = View.GONE
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
                    setEmptyVisibility(root)
                }else if (response.isSuccessful && response.body() != null) {
                    changed = 1
                    val values = response.body()!!
                    availabilities.addAll(values)
                    filterAndSortAvailabilities()
                    list_availabilities.adapter = AvailabilityAdapter(availabilities)
                    setSuccessVisibility(root)
                }
            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                hideSpinner(root)
                return
            }

        })
    }

    fun filterAndSortAvailabilities()
    {
        val fromatter = SimpleDateFormat("yyyy-MM-dd")
        val today = Date()
        availabilities = ArrayList(availabilities.filter { it.date!! >= String.format("%sT00:00:00",fromatter.format(today))})
        availabilities.sortBy{it.date+' '+it.startTime+' '+it.endTime}
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
        val hours = arrayOf("09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
        val mins = arrayOf("00", "15", "30", "45")
        val dates = getDates()
        initValsPicker(fromHourPicker, hours)
        initValsPicker(toHourPicker, hours)
        initValsPicker(fromMinPicker, mins)
        initValsPicker(toMinPicker, mins)
        initValsPicker(dayPicker, dates.toTypedArray())

        toHourPicker.setOnValueChangedListener{ _, _, _ ->
            toHourPickerChanged(fromHourPicker, toHourPicker, fromMinPicker, toMinPicker, hours, mins)
        }
        fromHourPicker.setOnValueChangedListener{ _, _, _ ->
            fromHourPickerChanged(fromHourPicker, toHourPicker, fromMinPicker, toMinPicker, hours, mins)
        }
        fromMinPicker.setOnValueChangedListener{ _, _, _ ->
            fromMinPickerChanged(fromHourPicker, toHourPicker, fromMinPicker, toMinPicker, hours, mins)
        }
        toMinPicker.setOnValueChangedListener{ _, _, _ ->
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

            if(checkAvailability(fromHour, fromMin, toHour, toMin, date)) {
                dialog.dismiss()
                params.add(AddAvailabilityParams(date, "$fromHour:$fromMin", "$toHour:$toMin"))
                postAvailabilities(applicationContext, params)
            }

        }
        val btn_cancel = dialog .findViewById(R.id.btn_closeAddAvailability) as Button
        btn_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun checkAvailability(fromHour: String, fromMin :String, toHour: String, toMin :String, date:String) : Boolean
    {
        if("$fromHour:$fromMin" == "$toHour:$toMin") {
            Toast.makeText(
                this, resources.getString(R.string.av_startend),
                Toast.LENGTH_LONG
            ).show()
        }
        else if(availabilities.any {it.date == date && it.startTime!! == "$fromHour:$fromMin:00" && it.endTime!! == "$toHour:$toMin:00"})
        {
            Toast.makeText(
                this, resources.getString(R.string.av_same),
                Toast.LENGTH_LONG
            ).show()
        }
        else if(availabilities.any {it.date == date && it.startTime!! < "$toHour:$toMin:00" && it.endTime!! > "$fromHour:$fromMin:00"})
        {
            Toast.makeText(
                this, resources.getString(R.string.av_overlap),
                Toast.LENGTH_LONG
            ).show()
        }
        else
        {
            return true
        }
        return false
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
