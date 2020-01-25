package nl.bezorgdirect.mijnbd.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import nl.bezorgdirect.mijnbd.helpers.hideSpinner
import nl.bezorgdirect.mijnbd.helpers.showSpinner
import nl.bezorgdirect.mijnbd.recyclerviews.HistoryAdapter
import nl.bezorgdirect.mijnbd.recyclerviews.HistoryListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyBDHistory : Fragment() {

    private var deliveries = ArrayList<Delivery>()
    private var activeCall = false
    val clickHistory =  object:HistoryListener
    {
        override fun onItemClick(position: Int)
        {
            val intent = Intent(activity, MyBDHistoryDetails::class.java)
            println("Pos "+position)
            println(deliveries[position])

            intent.putExtra("orderSubmitted", deliveries[position].warehousePickUpAt)//needs to be added to api is in ntoifcations
            //accepted
            intent.putExtra("timeAccepted",deliveries[position].warehousePickUpAt)//needs to be added to api warhouse pickup for now
            //warehouse
            intent.putExtra("warehouseAddress",deliveries[position].warehouse.address)
            intent.putExtra("warehouseDistance",deliveries[position].warehouseDistanceInKilometers)
            intent.putExtra("warehousePickUp",deliveries[position].warehousePickUpAt)
            //customer
            intent.putExtra("customerAddress",deliveries[position].customer.address)
            intent.putExtra("customerDistance",deliveries[position].customerDistanceInKilometers)
            intent.putExtra("customerDeliveredAt",deliveries[position].deliveredAt)
            //price
            intent.putExtra("price",deliveries[position].price)
            intent.putExtra("tip",deliveries[position].tip)
            //status+vehicle
            intent.putExtra("statusDisplayName",deliveries[position].status) // TODO: get status name
            intent.putExtra("status",deliveries[position].status)
            intent.putExtra("vehicle",deliveries[position].vehicle)

            startActivity(intent)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.activity_my_bdhistory, container, false)

        if(activity != null) {
            val custom_toolbar_title: TextView = activity!!.findViewById(R.id.custom_toolbar_title)
            custom_toolbar_title.text = getString(R.string.lbl_history)
        }


        val verticalList = LinearLayoutManager(root.context)



        var listitems = HistoryAdapter(deliveries, clickHistory)
        val list_historie: RecyclerView = root.findViewById(R.id.list_historie)
        val swp_historie: SwipeRefreshLayout = root.findViewById(R.id.swp_historie)
        val btn_retry_history: Button = root.findViewById(R.id.btn_retry_history)

        btn_retry_history.setOnClickListener{
            println("kaka")
            val myBDHistory = MyBDHistory()
            this.fragmentManager!!.beginTransaction().replace(R.id.delivery_fragment, myBDHistory).commit()
        }

        list_historie.layoutManager = verticalList
        list_historie.adapter = listitems

        swp_historie.setColorSchemeResources(
            R.color.colorPrimaryDark
        )

        swp_historie.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(root.context, R.color.colorPrimary))

        swp_historie.setOnRefreshListener {
            if(!activeCall) {
                getDeliveries(root.context, root)
            }
            else
            {
                swp_historie.isRefreshing = false
            }
        }

        getDeliveries(root.context, root)
        return root
    }

    private fun getDeliveries(context: Context, root: View)
    {
        val history_error: LinearLayout = root.findViewById(R.id.history_error)
        val history_empty: LinearLayout = root.findViewById(R.id.history_empty)
        val history_content: LinearLayout = root.findViewById(R.id.history_content)
        val list_historie: RecyclerView = root.findViewById(R.id.list_historie)
        val swp_historie: SwipeRefreshLayout = root.findViewById(R.id.swp_historie)

        if(!swp_historie.isRefreshing)
        {
            showSpinner(root)
        }

        val service = getApiService()
        val decryptedToken = getDecryptedToken(context)

        activeCall = true
        service.deliveriesGet(auth = decryptedToken).enqueue(object : Callback<ArrayList<Delivery>> {
            override fun onResponse(
                call: Call<ArrayList<Delivery>>,
                response: Response<ArrayList<Delivery>>
            ) {
                println(response)
                if (response.code() == 500) {
                    history_error.visibility = View.VISIBLE
                    history_content.visibility = View.GONE
                    setLoadingDone(root)
                }
                else if (response.code() == 401) {
                    history_error.visibility = View.VISIBLE
                    history_content.visibility = View.GONE
                    setLoadingDone(root)

                } else if (response.code() == 204) {
                    history_empty.visibility = View.VISIBLE
                    history_content.visibility = View.GONE
                    setLoadingDone(root)

                }else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    deliveries = values

                    println(deliveries[0].currentId)
                    println(list_historie.adapter)
                    history_empty.visibility = View.GONE
                    history_error.visibility = View.GONE
                    history_content.visibility = View.VISIBLE

                    list_historie.adapter = HistoryAdapter(deliveries, clickHistory)
                    setLoadingDone(root)
                }
                else
                {
                    history_error.visibility = View.VISIBLE
                    history_content.visibility = View.GONE
                    setLoadingDone(root)
                }
            }

            override fun onFailure(call: Call<ArrayList<Delivery>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                history_error.visibility = View.VISIBLE
                setLoadingDone(root)
                return
            }

        })

    }
    fun setLoadingDone(root: View)
    {
        hideSpinner(root)
        val swp_historie: SwipeRefreshLayout = root.findViewById(R.id.swp_historie)
        swp_historie.isRefreshing = false
        activeCall = false
    }


}
