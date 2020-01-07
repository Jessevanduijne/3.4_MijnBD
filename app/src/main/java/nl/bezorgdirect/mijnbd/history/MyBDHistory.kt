package nl.bezorgdirect.mijnbd.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_my_bdhistory.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
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

    companion object {
        fun newInstance() = MyBDHistory()
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
        val loadingSpinner: ProgressBar = root.findViewById(R.id.loadingSpinner)

        if(!swp_historie.isRefreshing)
        {
            loadingSpinner.bringToFront()
            loadingSpinner.visibility = View.VISIBLE
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
                    setLoadingDone()
                }
                else if (response.code() == 401) {
                    history_error.visibility = View.VISIBLE
                    history_content.visibility = View.GONE
                    setLoadingDone()

                } else if (response.code() == 204) {
                    history_empty.visibility = View.VISIBLE
                    history_content.visibility = View.GONE
                    setLoadingDone()

                }else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    deliveries = values

                    println(deliveries[0].CurrentId)
                    println(list_historie.adapter)
                    history_empty.visibility = View.GONE
                    history_error.visibility = View.GONE
                    history_content.visibility = View.VISIBLE

                    list_historie.adapter = HistoryAdapter(deliveries, clickHistory)
                    setLoadingDone()
                }
                else
                {
                    history_error.visibility = View.VISIBLE
                    history_content.visibility = View.GONE
                    setLoadingDone()
                }
            }

            override fun onFailure(call: Call<ArrayList<Delivery>>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                history_error.visibility = View.VISIBLE
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
