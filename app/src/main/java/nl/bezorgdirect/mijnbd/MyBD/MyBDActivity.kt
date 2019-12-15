package nl.bezorgdirect.mijnbd.MyBD

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_my_bd.*
import nl.bezorgdirect.mijnbd.Encryption.CipherWrapper
import nl.bezorgdirect.mijnbd.Encryption.KeyStoreWrapper
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.Availability
import nl.bezorgdirect.mijnbd.api.User
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MyBDActivity : Fragment() {

    private var availabilities = ArrayList<Availability>()
    private var activeCall = false
    private var count = 0

    companion object {
        fun newInstance() = MyBDActivity()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.activity_my_bd, container, false)

        if(activity != null) {
            val custom_toolbar_title: TextView = activity!!.findViewById(R.id.custom_toolbar_title)
            custom_toolbar_title.text = getString(R.string.lbl_mybdpersonalia)
        }

        val btn_info: Button = root.findViewById(R.id.btn_info)
        val btn_availability: Button = root.findViewById(R.id.btn_availability)
        val btn_meansoftransport: Button = root.findViewById(R.id.btn_meansoftransport)


        //fill in dates on schedule
        val calendar = Calendar.getInstance()
        val currentDateFormat = SimpleDateFormat("MM-dd")
        val currentDateFormatDay = SimpleDateFormat("EEE")
        val lbl_mondaydate: TextView = root.findViewById(R.id.lbl_mondaydate)
        val lbl_tuesdaydate: TextView = root.findViewById(R.id.lbl_tuesdaydate)
        val lbl_wednesdaydate: TextView = root.findViewById(R.id.lbl_wednesdaydate)
        val lbl_thursdaydate: TextView = root.findViewById(R.id.lbl_thursdaydate)
        val lbl_fridaydate: TextView = root.findViewById(R.id.lbl_fridaydate)
        val lbl_saturdaydate: TextView = root.findViewById(R.id.lbl_saturdaydate)
        val lbl_sundaydate: TextView = root.findViewById(R.id.lbl_sundaydate)

        val lbl_monday: TextView = root.findViewById(R.id.lbl_monday)
        val lbl_tuesday: TextView = root.findViewById(R.id.lbl_tuesday)
        val lbl_wednesday: TextView = root.findViewById(R.id.lbl_wednesday)
        val lbl_thursday: TextView = root.findViewById(R.id.lbl_thursday)
        val lbl_friday: TextView = root.findViewById(R.id.lbl_friday)
        val lbl_saturday: TextView = root.findViewById(R.id.lbl_saturday)
        val lbl_sunday: TextView = root.findViewById(R.id.lbl_sunday)

        val lbl_monday_var: TextView = root.findViewById(R.id.lbl_monday_var)
        val lbl_tuesday_var: TextView = root.findViewById(R.id.lbl_tuesday_var)
        val lbl_wednesday_var: TextView = root.findViewById(R.id.lbl_wednesday_var)
        val lbl_thursday_var: TextView = root.findViewById(R.id.lbl_thursday_var)
        val lbl_friday_var: TextView = root.findViewById(R.id.lbl_friday_var)
        val lbl_saturday_var: TextView = root.findViewById(R.id.lbl_saturday_var)
        val lbl_sunday_var: TextView = root.findViewById(R.id.lbl_sunday_var)

        val formattedDateOne = currentDateFormat.format(calendar.time)
        val formattedDateOneDay = currentDateFormatDay.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val formattedDateTwo = currentDateFormat.format(calendar.time)
        val formattedDateTwoDay = currentDateFormatDay.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val formattedDateThree = currentDateFormat.format(calendar.time)
        val formattedDateThreeDay = currentDateFormatDay.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val formattedDateFour = currentDateFormat.format(calendar.time)
        val formattedDateFourDay = currentDateFormatDay.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val formattedDateFive = currentDateFormat.format(calendar.time)
        val formattedDateFiveDay = currentDateFormatDay.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val formattedDateSix = currentDateFormat.format(calendar.time)
        val formattedDateSixDay = currentDateFormatDay.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val formattedDateSeven = currentDateFormat.format(calendar.time)
        val formattedDateSevenDay = currentDateFormatDay.format(calendar.time)

        lbl_mondaydate.text = formattedDateOne
        lbl_tuesdaydate.text = formattedDateTwo
        lbl_wednesdaydate.text = formattedDateThree
        lbl_thursdaydate.text = formattedDateFour
        lbl_fridaydate.text = formattedDateFive
        lbl_saturdaydate.text = formattedDateSix
        lbl_sundaydate.text = formattedDateSeven

        lbl_monday.text = formattedDateOneDay
        lbl_tuesday.text = formattedDateTwoDay
        lbl_wednesday.text = formattedDateThreeDay
        lbl_thursday.text = formattedDateFourDay
        lbl_friday.text = formattedDateFiveDay
        lbl_saturday.text = formattedDateSixDay
        lbl_sunday.text = formattedDateSevenDay

        getAvailabilities(root.context)


        btn_info.setOnClickListener{
            val intent : Intent = Intent(root.context, MyBDInfo::class.java)
            startActivity(intent)
        }

        btn_availability.setOnClickListener {
            val intent : Intent = Intent(root.context, MyBDAvailability::class.java)
            startActivity(intent)
        }

        btn_meansoftransport.setOnClickListener {
            val intent : Intent = Intent(root.context, MyBDMoS::class.java)
            startActivity(intent)
        }

        getDeliverer(root.context, root)

        return root
    }

    private fun getDeliverer(context: Context, root: View){
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:7071/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val sharedPref: SharedPreferences = context.getSharedPreferences("mybd", Context.MODE_PRIVATE)
        val encryptedToken = sharedPref.getString("T", "")

        val keyStoreWrapper = KeyStoreWrapper(context, "mybd")
        val Key = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair("BD_KEY")
        var token = ""
        if(encryptedToken != "" && Key != null)
        {
            val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")
            token = cipherWrapper.decrypt(encryptedToken!!, Key?.private)
        }
        else
        {
            return
        }

        service.delivererGet(auth = token).enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
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
                    println("vals")
                    println(values)
                    val lbl_name: TextView = root.findViewById(R.id.lbl_name)
                    val lbl_vehicle: TextView = root.findViewById(R.id.lbl_mosvar)
                    val lbl_pd: TextView = root.findViewById(R.id.lbl_payoutpdvar)
                    val lbl_total_earnings: TextView = root.findViewById(R.id.lbl_payouttotalvar)
                    lbl_vehicle.text = values.vehicleDisplayName
                    lbl_name.text = values.emailAddress?.substringBefore(delimiter= '@', missingDelimiterValue = "string does not contain delimiter @")
                    val fare = values.fare
                    lbl_pd.text = "$fare"
                    val total = values.totalEarnings
                    lbl_total_earnings.text = "$total"

                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

        })
    }

    private fun getAvailabilities(context: Context){

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:7071/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val sharedPref: SharedPreferences = context.getSharedPreferences("mybd", Context.MODE_PRIVATE)
        val encryptedToken = sharedPref.getString("T", "")

        val keyStoreWrapper = KeyStoreWrapper(context, "mybd")
        val Key = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair("BD_KEY")
        var token = ""
        if(encryptedToken != "" && Key != null)
        {
            val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")
            token = cipherWrapper.decrypt(encryptedToken!!, Key?.private)
        }
        else
        {
            return
        }

        service.availablitiesGet(auth = token).enqueue(object : Callback<ArrayList<Availability>> {
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
                    var countmax = values.size
                    if (lbl_mondaydate.text == values[count].Date.toString().substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date").substring(5)){
                        lbl_monday_var.text = (values[count].StartTime.toString().substring(0, 5) + " - " + values[0].EndTime.toString().substring(0, 5))
                        //if (count < countmax){count += 1}
                    }
                    else{lbl_monday_var.text = "Vrij"}
                    if (lbl_tuesdaydate.text == values[count].Date.toString().substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date").substring(5)){
                        lbl_tuesday_var.text = (values[count].StartTime.toString().substring(0, 5) + " - " + values[0].EndTime.toString().substring(0, 5))
                        //if (count < countmax){count += 1}
                    }
                    else{lbl_tuesday_var.text = "vrij"}
                    if (lbl_wednesdaydate.text == values[count].Date.toString().substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date").substring(5)){
                        lbl_wednesday_var.text = (values[count].StartTime.toString().substring(0, 5) + " - " + values[0].EndTime.toString().substring(0, 5))
                        //if (count < countmax){count += 1}
                    }
                    else{lbl_wednesday_var.text = "vrij"}
                    if (lbl_thursdaydate.text == values[count].Date.toString().substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date").substring(5)){
                        lbl_thursday_var.text = (values[count].StartTime.toString().substring(0, 5) + " - " + values[0].EndTime.toString().substring(0, 5))
                        //if (count < countmax){count += 1}
                    }
                    else{lbl_thursday_var.text = "vrij"}
                    if (lbl_fridaydate.text == values[count].Date.toString().substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date").substring(5)){
                        lbl_friday_var.text = (values[count].StartTime.toString().substring(0, 5) + " - " + values[0].EndTime.toString().substring(0, 5))
                        //if (count < countmax){count += 1}
                    }
                    else{lbl_friday_var.text = "vrij"}
                    if (lbl_saturdaydate.text == values[count].Date.toString().substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date").substring(5)){
                        lbl_saturday_var.text = (values[count].StartTime.toString().substring(0, 5) + " - " + values[0].EndTime.toString().substring(0, 5))
                        //if (count < countmax){count += 1}
                    }
                    else{lbl_saturday_var.text = "Vrij"}
                    if (lbl_sundaydate.text == values[count].Date.toString().substringBefore(delimiter = 'T', missingDelimiterValue = "missing delimiter T in date").substring(5)){
                        lbl_sunday_var.text = (values[count].StartTime.toString().substring(0, 5) + " - " + values[0].EndTime.toString().substring(0, 5))
                    }
                    else{lbl_sunday_var.text = "Vrij"}
                    availabilities = values
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
}