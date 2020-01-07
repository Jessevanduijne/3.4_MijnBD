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
                getAvailabilities(this)
            }
            else
            {
                swp_availability.isRefreshing = false
            }
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
                println(response)
                if (response.code() == 500) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                    swp_availability.isRefreshing = false
                    hideSpinner(root)
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
                    activeCall = false
                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    println(values)
                    if(values.size > 0)
                    {
                        if(values[0] != null)
                        {
                            availabilities = values
                            sortAvailabilities()
                            list_availabilities.adapter = AvailabilityAdapter(availabilities)
                        }
                    }
                    swp_availability.isRefreshing = false
                    hideSpinner(root)
                    activeCall = false
                    //list_availabilities.adapter?.notifyDataSetChanged()
                }
                else{
                    swp_availability.isRefreshing = false
                    hideSpinner(root)
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
                println(response)
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
                } else if (response.isSuccessful && response.body() != null) {
                    changed = 1
                    val values = response.body()!!
                    availabilities.addAll(values)
                    sortAvailabilities()
                    Toast.makeText(context, "Availability added!", Toast.LENGTH_SHORT).show()
                    list_availabilities.adapter!!.notifyDataSetChanged()
                    hideSpinner(root)
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
    fun sortAvailabilities()
    {
        availabilities = ArrayList(availabilities.sortedWith(compareBy({it.Date}, {it.StartTime})))
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

        fromHourPicker.minValue = 0
        fromHourPicker.maxValue = hours.size-1
        fromHourPicker.displayedValues = hours

        fromMinPicker.minValue = 0
        fromMinPicker.maxValue = mins.size-1
        fromMinPicker.displayedValues = mins

        toHourPicker.minValue = 0
        toHourPicker.maxValue = hours.size-1
        toHourPicker.displayedValues = hours

        toMinPicker.minValue = 0
        toMinPicker.maxValue = mins.size-1
        toMinPicker.displayedValues = mins

        dayPicker.minValue = 0
        dayPicker.maxValue = dates.size-1

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
