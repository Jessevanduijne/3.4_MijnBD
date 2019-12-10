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
import nl.bezorgdirect.mijnbd.Encryption.CipherWrapper
import nl.bezorgdirect.mijnbd.Encryption.KeyStoreWrapper
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyBDActivity : Fragment() {

    private var User = ArrayList<User>()
    private var activeCall = false

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
            .baseUrl("http://192.168.178.18:7071/api/")
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
                    lbl_name.text = values.emailAddress
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
}