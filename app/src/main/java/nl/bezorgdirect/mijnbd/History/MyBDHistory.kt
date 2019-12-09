package nl.bezorgdirect.mijnbd.History

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_my_bdhistory.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.Encryption.CipherWrapper
import nl.bezorgdirect.mijnbd.Encryption.KeyStoreWrapper
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.RecyclerViews.HistoryAdapter
import nl.bezorgdirect.mijnbd.RecyclerViews.HistoryListener
import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.Delivery
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MyBDHistory : AppCompatActivity() {

    private var deliveries = ArrayList<Delivery>()
    private var activeCall = false
    val clickHistory =  object:HistoryListener
    {
        override fun onItemClick(position: Int)
        {
            val intent = Intent(this@MyBDHistory, MyBDHistoryDetails::class.java)
            println("Pos "+position)
            println(deliveries[position])
            //warehouse
            intent.putExtra("warehouseAddress",deliveries[position].Warehouse.Address)
            intent.putExtra("warehouseDistance",deliveries[position].WarehouseDistaceInKilometers)
            intent.putExtra("warehousePickUp",deliveries[position].WarehousePickUpAt)
            //customer
            intent.putExtra("customerAddress",deliveries[position].Customer.Address)
            intent.putExtra("customerDistance",deliveries[position].CustomerDistanceInKilometers)
            intent.putExtra("customerDeliveredAt",deliveries[position].DeliveredAt)
            //price
            intent.putExtra("price",deliveries[position].Price)
            intent.putExtra("tip",deliveries[position].tip)
            //status+vehicle
            intent.putExtra("statusDisplayName",deliveries[position].StatusDisplayName)
            intent.putExtra("status",deliveries[position].Status)
            intent.putExtra("vehicle",deliveries[position].Vehicle)

            startActivity(intent)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(nl.bezorgdirect.mijnbd.R.layout.activity_my_bdhistory)

        custom_toolbar_title.setText(getString(nl.bezorgdirect.mijnbd.R.string.lbl_history))
        setSupportActionBar(custom_toolbar)

        val verticalList = LinearLayoutManager(this)

        getDeliveries(this)

        var listitems = HistoryAdapter(deliveries, clickHistory)

        list_historie.layoutManager = verticalList
        list_historie.adapter = listitems

        swp_historie.setColorSchemeResources(
            nl.bezorgdirect.mijnbd.R.color.colorPrimaryDark
        )

        swp_historie.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.colorPrimary))

        swp_historie.setOnRefreshListener {
            if(!activeCall) {
                getDeliveries(this)
            }
            else
            {
                swp_historie.isRefreshing = false
            }
        }
        getDeliveries(this)
    }

    private fun getDeliveries(context: Context)
    {

        if(!swp_historie.isRefreshing)
        {
            loadingSpinner.bringToFront()
            loadingSpinner.visibility = View.VISIBLE
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.178.18:7071/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val sharedPref: SharedPreferences = this.getSharedPreferences("mybd", Context.MODE_PRIVATE)
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
            setLoadingDone()
            return
        }

        activeCall = true
        service.deliveriesGet(auth = token).enqueue(object : Callback<ArrayList<Delivery>> {
            override fun onResponse(
                call: Call<ArrayList<Delivery>>,
                response: Response<ArrayList<Delivery>>
            ) {
                println(response)
                if (response.code() == 500) {
                    Toast.makeText(
                        context,
                        resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                    setLoadingDone()
                }
                else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(nl.bezorgdirect.mijnbd.R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    setLoadingDone()

                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    deliveries = values

                    println(deliveries[0].CurrentId)
                    println(list_historie.adapter)

                    list_historie.adapter = HistoryAdapter(deliveries, clickHistory)
                    setLoadingDone()
                }
            }

            override fun onFailure(call: Call<ArrayList<Delivery>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                setLoadingDone()
                return
            }

        })

    }
    fun setLoadingDone()
    {
        loadingSpinner.visibility = View.GONE
        swp_historie.isRefreshing = false
        activeCall = false
    }


}
