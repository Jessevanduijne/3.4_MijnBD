package nl.bezorgdirect.mijnbd.mijnBD

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Availability
import nl.bezorgdirect.mijnbd.api.Location
import nl.bezorgdirect.mijnbd.api.User
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import nl.bezorgdirect.mijnbd.helpers.hideSpinner
import nl.bezorgdirect.mijnbd.helpers.showSpinner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList




class MyBDActivity : Fragment() {

    private var user = User(null, null, null , null, null, Location(null, null, null, null , null, null ,null),
        null, null ,null ,null, null, null )
    private var activeCall = false

    var email: TextView? = null
    var vehicle: TextView? = null
    var img_profile: ImageView? = null
    var cont: Context? = null
    var root: View? = null

    var err = 0
    var busy = 0

    var list_availability_day  :LinearLayout? = null
    var list_availability_time :LinearLayout? = null
    var list_availability_type :LinearLayout? = null

    companion object {
        fun newInstance() = MyBDActivity()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(nl.bezorgdirect.mijnbd.R.layout.activity_my_bd, container, false)
        if(activity != null) {
            val custom_toolbar_title: TextView = activity!!.findViewById(nl.bezorgdirect.mijnbd.R.id.custom_toolbar_title)
            custom_toolbar_title.text = getString(nl.bezorgdirect.mijnbd.R.string.lbl_mybdpersonalia)
        }
        this.root = root

        cont = root.context


        email = root.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_name)
        vehicle = root.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_mosvar)
        list_availability_day = root.findViewById(nl.bezorgdirect.mijnbd.R.id.list_availability_day)
        list_availability_time = root.findViewById(nl.bezorgdirect.mijnbd.R.id.list_availability_time)
        list_availability_type = root.findViewById(nl.bezorgdirect.mijnbd.R.id.list_availability_type)


        val btn_info: Button = root.findViewById(nl.bezorgdirect.mijnbd.R.id.btn_info)
        val btn_availability: Button = root.findViewById(nl.bezorgdirect.mijnbd.R.id.btn_availability)
        val btn_meansoftransport: Button = root.findViewById(nl.bezorgdirect.mijnbd.R.id.btn_meansoftransport)
        val btn_retry_mybd: Button = root.findViewById(nl.bezorgdirect.mijnbd.R.id.btn_retry_mybd)

        btn_retry_mybd.setOnClickListener{
            val myBD= MyBDActivity()
            this.fragmentManager!!.beginTransaction().replace(R.id.delivery_fragment, myBD).commit()
        }

        img_profile = root.findViewById(nl.bezorgdirect.mijnbd.R.id.img_profile)

        btn_info.setOnClickListener{
            gotoInfo(root.context)
        }

        btn_availability.setOnClickListener {
            gotoAvailability(root.context)
        }

        btn_meansoftransport.setOnClickListener {
            gotoMeansoftransport(root.context)
        }
        setAvatar()
        getAvailabilities()
        getDeliverer()

        return root
    }
    fun setAvatar()
    {
        val sharedPrefs = activity!!.applicationContext.getSharedPreferences("mybd", Context.MODE_PRIVATE)
        val avataruri = sharedPrefs.getString("avatar", "")
        if(avataruri != "")
        {
            val uri = Uri.parse((avataruri))
            img_profile!!.setImageURI(uri)
        }
    }
    private fun getDeliverer(){
        startLoading()
        busy++
        val service = getApiService()
        val decryptedToken = getDecryptedToken(activity!!.applicationContext)

        service.delivererGet(auth = decryptedToken).enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                println(response)
                if (response.code() == 500) {
                    Toast.makeText(
                        activity?.applicationContext,
                        resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                    err++
                    busy--
                    doneLoading()
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        activity?.applicationContext,
                        resources.getString(nl.bezorgdirect.mijnbd.R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    err++
                    busy--
                    doneLoading()
                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    user = values
                    println("vals")
                    println(values)
                    val lbl_name: TextView = root!!.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_name)
                    val lbl_vehicle: TextView = root!!.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_mosvar)
                    val lbl_pd: TextView = root!!.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_payoutpdvar)
                    val lbl_total_earnings: TextView = root!!.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_payouttotalvar)
                    if(values.vehicleDisplayName != null) {
                        lbl_vehicle.text = values.vehicleDisplayName
                    }
                    else
                    {
                        lbl_vehicle.text = "Geen"
                    }
                    lbl_name.text = values.firstName+" "+values.lastName
                    lbl_pd.text = values.fare.toString()
                    var total = values.totalEarnings
                    if(total == null)
                    {
                        lbl_total_earnings.text = "0"
                    }
                    else {
                        lbl_total_earnings.text = "$total"
                    }
                    busy--
                    doneLoading()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    activity?.applicationContext, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                err++
                busy--
                doneLoading()
                return
            }

        })
    }

    private fun getAvailabilities(){
        startLoading()
        busy++
        val service = getApiService()
        val decryptedToken = getDecryptedToken(activity!!.applicationContext)

        service.availablitiesGet(auth = decryptedToken).enqueue(object : Callback<ArrayList<Availability>> {
            override fun onResponse(
                call: Call<ArrayList<Availability>>,
                response: Response<ArrayList<Availability>>
            ) {
                println(response)
                if (response.code() == 500) {
                    Toast.makeText(
                        activity?.applicationContext,
                        resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                    err++
                    busy--
                    doneLoading()
                }
                else if (response.code() == 424) {
                    fillAvailability(ArrayList())
                    busy--
                    doneLoading()
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        activity?.applicationContext,
                        resources.getString(nl.bezorgdirect.mijnbd.R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    err++
                    busy--
                    doneLoading()
                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    fillAvailability(values)
                    busy--
                    doneLoading()
                }
                else
                {
                    err++
                    busy--
                    doneLoading()
                }
            }

            override fun onFailure(call: Call<ArrayList<Availability>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    activity?.applicationContext, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                err++
                busy--
                doneLoading()
                return
            }

        })
    }

    fun fillAvailability(availabilities: ArrayList<Availability>)
    {

        list_availability_day!!.removeAllViews()
        list_availability_time!!.removeAllViews()
        list_availability_type!!.removeAllViews()
        val week = getWeek()
        var count = 0
        for (day in week) {

            count = 0
            for(availability in availabilities)
            {
                if(availability.date == day)
                {

                    count++
                    val lbl_day  = TextView(cont)
                    val lbl_time = TextView(cont)
                    val lbl_type = TextView(cont)

                    lbl_day.setTextColor(ContextCompat.getColor(activity!!.applicationContext, nl.bezorgdirect.mijnbd.R.color.colorAccent))
                    lbl_time.setTextColor(ContextCompat.getColor(activity!!.applicationContext, nl.bezorgdirect.mijnbd.R.color.colorAccent))
                    lbl_type.setTextColor(ContextCompat.getColor(activity!!.applicationContext, nl.bezorgdirect.mijnbd.R.color.colorAccent))

                    lbl_day.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.0f)
                    lbl_time.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.0f)
                    lbl_type.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.0f)

                    val dayFormat = SimpleDateFormat("EEE")
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd")
                    val availabilityDay: Date = inputFormat.parse(availability.date)
                    if(count == 1) {
                        lbl_day.text = dayFormat.format(availabilityDay)
                    }
                    lbl_time.text = availability.startTime!!.take(5) + " - " + availability.endTime!!.take(5)
                    lbl_type.text = "Beschikbaar"

                    list_availability_day!!.addView(lbl_day)
                    list_availability_time!!.addView(lbl_time)
                    list_availability_type!!.addView(lbl_type)
                }
            }
            if(count == 0)
            {
                val lbl_day  = TextView(cont)
                val lbl_time = TextView(cont)
                val lbl_type = TextView(cont)

                lbl_day.setTextColor(ContextCompat.getColor(activity!!.applicationContext, nl.bezorgdirect.mijnbd.R.color.colorAccent))
                lbl_time.setTextColor(ContextCompat.getColor(activity!!.applicationContext, nl.bezorgdirect.mijnbd.R.color.colorAccent))
                lbl_type.setTextColor(ContextCompat.getColor(activity!!.applicationContext, nl.bezorgdirect.mijnbd.R.color.colorAccent))

                lbl_day.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.0f)
                lbl_time.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.0f)
                lbl_type.setTextSize(TypedValue.COMPLEX_UNIT_SP,18.0f)

                val dayFormat = SimpleDateFormat("EEE")
                val inputFormat = SimpleDateFormat("yyyy-MM-dd")
                val availabilityDay: Date = inputFormat.parse(day)

                lbl_day.text = dayFormat.format(availabilityDay)
                lbl_time.text = "00:00 - 00:00"
                lbl_type.text = "Onbeschikbaar"
                list_availability_day!!.addView(lbl_day)
                list_availability_time!!.addView(lbl_time)
                list_availability_type!!.addView(lbl_type)
            }

        }
    }

    fun getWeek() : ArrayList<String>
    {
        val fromatter = SimpleDateFormat("yyyy-MM-dd")

        val today = Date()

        val week = ArrayList<String>()
        for (i in 0..6)
        {
            val day = Date(today.time + (1000 * 60 * 60 * 24) * i)
            week.add(fromatter.format(day)+"T00:00:00")
        }
        return week
    }
    fun gotoInfo(context: Context)
    {
        val intent : Intent = Intent(context, MyBDInfo::class.java)

        //val address = user.home.Address + "; " + user.home.Place + "; " + user.home.PostalCode
        putObjects(intent)
        startActivityForResult(intent, 1)
    }
    fun gotoAvailability(context: Context)
    {
        val intent : Intent = Intent(context, MyBDAvailability::class.java)
        startActivityForResult(intent, 2)
    }
    fun gotoMeansoftransport(context: Context)
    {
        val intent : Intent = Intent(context, MyBDMoS::class.java)
        putObjects(intent)
        startActivityForResult(intent,3)
    }
    fun putObjects(intent: Intent)
    {
        intent.putExtra("email", checkStringNull(user.emailAddress))
        intent.putExtra("vehicle", user.vehicle)
        intent.putExtra("range", user.range)
        intent.putExtra("phonenumber", checkStringNull(user.phoneNumber))
        intent.putExtra("vehicledisplayname", checkStringNull(user.vehicleDisplayName))
        intent.putExtra("dateofbirth",checkStringNull(user.dateOfBirth))
        intent.putExtra("fare",user.fare)
        intent.putExtra("firstname",checkStringNull(user.firstName))
        intent.putExtra("lastname",checkStringNull(user.lastName))
    }
    fun checkStringNull(string: String?) :String
    {
        var returnString = ""
        if(string != null)
        {
            returnString = string
        }
        return returnString
    }
    fun doneLoading()
    {
        println("b: $busy e: $err")
        if(busy == 0) {
            val content: LinearLayout = root!!.findViewById(nl.bezorgdirect.mijnbd.R.id.mybd_content)
            val error: LinearLayout = root!!.findViewById(nl.bezorgdirect.mijnbd.R.id.mybd_error)
            if(err == 0) {
                content.visibility = View.VISIBLE
                error.visibility = View.GONE
            }
            else
            {
                content.visibility = View.GONE
                error.visibility = View.VISIBLE
            }
            hideSpinner(root!!)
        }
    }
    fun startLoading()
    {
        if(busy == 0) {
            val content: LinearLayout = root!!.findViewById(R.id.mybd_content)
            content.visibility = View.GONE
            showSpinner(root!!)
        }
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                user.emailAddress = data!!.getStringExtra("email")
                user.phoneNumber = data!!.getStringExtra("phonenumber")
                setAvatar()
        }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
         else if(requestCode == 2)
        {
            if (resultCode == Activity.RESULT_OK) {

                val result = data!!.getIntExtra("result", 0)
                if(result == 1)
                {
                    getAvailabilities()
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                 //Write your code if there's no result
             }
        }
        else if(requestCode == 3) //meansoftransport
        {
            if (resultCode == Activity.RESULT_OK) {
                val vehicleval = data!!.getIntExtra("vehicle", -1)
                user.range = data!!.getIntExtra("range", -1)
                user.vehicle = vehicleval
                when(vehicleval)
                {
                    1 -> vehicle!!.text = resources.getString(nl.bezorgdirect.mijnbd.R.string.V1)
                    2 -> vehicle!!.text = resources.getString(nl.bezorgdirect.mijnbd.R.string.V2_3)
                    3 -> vehicle!!.text = resources.getString(nl.bezorgdirect.mijnbd.R.string.V2_3)
                    4 -> vehicle!!.text = resources.getString(nl.bezorgdirect.mijnbd.R.string.V4)
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }//onActivityResult
}