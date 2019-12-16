package nl.bezorgdirect.mijnbd.MyBD

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import nl.bezorgdirect.mijnbd.Encryption.CipherWrapper
import nl.bezorgdirect.mijnbd.Encryption.KeyStoreWrapper
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.Location
import nl.bezorgdirect.mijnbd.api.Availability
import nl.bezorgdirect.mijnbd.api.User
import nl.bezorgdirect.mijnbd.helpers.getApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.ArrayList





class MyBDActivity : Fragment() {

    private var user = User(null, null, null , null, Location(null, null, null, null , null, null ,null),
        null, null ,null ,null, null, null )
    private var availabilities = ArrayList<Availability>()
    private var activeCall = false

    var email: TextView? = null
    var vehicle: TextView? = null

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

        email = root.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_name)
        vehicle = root.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_mosvar)

        val btn_info: Button = root.findViewById(nl.bezorgdirect.mijnbd.R.id.btn_info)
        val btn_availability: Button = root.findViewById(nl.bezorgdirect.mijnbd.R.id.btn_availability)
        val btn_meansoftransport: Button = root.findViewById(nl.bezorgdirect.mijnbd.R.id.btn_meansoftransport)


        //change date format to show day of week,
        /*
        val lbl_monday_var: TextView = root.findViewById(R.id.lbl_monday_var)

        val outputFormat = SimpleDateFormat("EEE")
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val start = "2019-12-06T15:16:25.84Z"
        val starttime: Date = inputFormat.parse(start)
        val formattedstart: String = outputFormat.format(starttime)
        lbl_monday_var.text = formattedstart */

        btn_info.setOnClickListener{
            gotoInfo(root.context)
        }

        btn_availability.setOnClickListener {
            val intent : Intent = Intent(root.context, MyBDAvailability::class.java)
            startActivity(intent)
        }

        btn_meansoftransport.setOnClickListener {
            gotoMeansoftransport(root.context)
        }

        getDeliverer(root.context, root)

        return root
    }

    private fun getDeliverer(context: Context, root: View){

        val service = getApiService()
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
                        resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                    doneLoading(root)
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(nl.bezorgdirect.mijnbd.R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    doneLoading(root)
                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    user = values
                    println("vals")
                    println(values)
                    val lbl_name: TextView = root.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_name)
                    val lbl_vehicle: TextView = root.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_mosvar)
                    val lbl_pd: TextView = root.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_payoutpdvar)
                    val lbl_total_earnings: TextView = root.findViewById(nl.bezorgdirect.mijnbd.R.id.lbl_payouttotalvar)
                    lbl_vehicle.text = values.vehicleDisplayName
                    lbl_name.text = values.emailAddress?.substringBefore(delimiter= '@', missingDelimiterValue = "string does not contain delimiter @")
                    val fare = values.fare
                    lbl_pd.text = "$fare"
                    val total = values.totalEarnings
                    lbl_total_earnings.text = "$total"
                    doneLoading(root)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                doneLoading(root)
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
    fun gotoInfo(context: Context)
    {
        val intent : Intent = Intent(context, MyBDInfo::class.java)

        //val address = user.home.Address + "; " + user.home.Place + "; " + user.home.PostalCode
        putObjects(intent)
        startActivityForResult(intent, 1)
    }
    fun gotoMeansoftransport(context: Context)
    {
        val intent : Intent = Intent(context, MyBDMoS::class.java)
        putObjects(intent)
        startActivityForResult(intent,3)
    }
    fun putObjects(intent: Intent)
    {
        intent.putExtra("email", user.emailAddress)
        intent.putExtra("vehicle", user.vehicle)
        intent.putExtra("range", user.range)
        intent.putExtra("phonenumber", user.phoneNumber)
        intent.putExtra("vehicledisplayname", user.vehicleDisplayName)
        intent.putExtra("dateofbirth",user.dateOfBirth)
        intent.putExtra("fare",user.fare)
        intent.putExtra("totalearnings",user.totalEarnings)
    }
    fun doneLoading(root: View)
    {
        val content: LinearLayout = root.findViewById(nl.bezorgdirect.mijnbd.R.id.mybd_content)
        val loadingSpinner: ProgressBar = root.findViewById(nl.bezorgdirect.mijnbd.R.id.loadingSpinner)
        content.visibility = View.VISIBLE
        loadingSpinner.visibility = View.GONE
    }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                email!!.text = data!!.getStringExtra("email")
                user.emailAddress = data!!.getStringExtra("email")
                user.phoneNumber = data!!.getStringExtra("phonenumber")
        }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
         else if(requestCode == 2)
        {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getStringExtra("result")

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
                    1 -> vehicle!!.text = resources.getString(R.string.V1)
                    2 -> vehicle!!.text = resources.getString(R.string.V2_3)
                    3 -> vehicle!!.text = resources.getString(R.string.V2_3)
                    4 -> vehicle!!.text = resources.getString(R.string.V4)
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }//onActivityResult
}