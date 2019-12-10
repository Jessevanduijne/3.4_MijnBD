package nl.bezorgdirect.mijnbd.MyBD

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.bezorgdirect.mijnbd.Encryption.CipherWrapper
import nl.bezorgdirect.mijnbd.Encryption.KeyStoreWrapper
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.RecyclerViews.AvailabilityAdapter
import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.Availability
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private lateinit var linearLayoutManager: LinearLayoutManager

class MyBDAvailability : AppCompatActivity() {

    private var availabilities = ArrayList<Availability>()
    private var activeCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bdavailability)

        var listitems = AvailabilityAdapter(availabilities)
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)


        linearLayoutManager = LinearLayoutManager(this)
        list_availabilities.layoutManager = linearLayoutManager
        list_availabilities.adapter = listitems

        getAvailabilities(applicationContext)

    }

    private fun getAvailabilities(context: Context){
        var list_availabilities: RecyclerView = findViewById(R.id.AvailabilityRecyclerView)

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
                    list_availabilities.adapter = AvailabilityAdapter(availabilities)
                    list_availabilities.adapter?.notifyDataSetChanged()
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
