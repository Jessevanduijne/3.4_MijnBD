package nl.bezorgdirect.mijnbd.mijnBD

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_my_bdavailability.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.AddAvailabilityParams
import nl.bezorgdirect.mijnbd.api.Availability
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import nl.bezorgdirect.mijnbd.recyclerviews.AvailabilityAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


private lateinit var linearLayoutManager: LinearLayoutManager

class MyBDAvailability : AppCompatActivity() {

    private var availabilities = ArrayList<Availability>()
    private var cont :Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bdavailability)

        val custom_toolbar_title: TextView = this.findViewById(R.id.custom_toolbar_title)
        custom_toolbar_title.text = getString(R.string.lbl_mybdpersonalia)

        cont = this
        addAvailabilityLayout.visibility = View.GONE

        var listitems = AvailabilityAdapter(availabilities)
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)

        val btn_addAvailability: FloatingActionButton = findViewById(R.id.btn_addAvailability)
        val btn_cancel: Button = findViewById(R.id.btn_cancel)
        val btn_submit: Button = findViewById(R.id.btn_submit)
        val lbl_error: TextView = findViewById(R.id.lbl_error)

        lbl_error.visibility = View.GONE

        btn_addAvailability.setOnClickListener{
            //addAvailabilityLayout.visibility = View.VISIBLE
            addDialog()
        }


        linearLayoutManager = LinearLayoutManager(this)
        list_availabilities.layoutManager = linearLayoutManager
        list_availabilities.adapter = listitems

        getAvailabilities(applicationContext)


    }

    private fun getAvailabilities(context: Context){
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)

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
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    println(values)
                    if(values.size > 0)
                    {
                        if(values[0] != null)
                        {
                            availabilities = values
                            list_availabilities.adapter = AvailabilityAdapter(availabilities)
                        }
                    }
                    //list_availabilities.adapter?.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

        })
    }

    private fun postAvailabilities(context: Context, params: ArrayList<AddAvailabilityParams>){
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)

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
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    availabilities.addAll(values)
                    Toast.makeText(context, "Availability added!", Toast.LENGTH_SHORT).show()
                    list_availabilities.adapter!!.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
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
        println(dates.size)
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
            dialog.dismiss()

            params.add(AddAvailabilityParams(date, "$fromHour:$fromMin", "$toHour:$toMin"))
            postAvailabilities(cont!!, params)


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

}
