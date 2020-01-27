package nl.bezorgdirect.mijnbd.delivery

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.fragment_assignment_finished.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.helpers.*
import nl.bezorgdirect.mijnbd.history.MyBDHistoryDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AssignmentFinishedFragment(val deliveryId: String): Fragment(){
    private val apiService = getApiService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.activity?.bottom_navigation?.visibility = View.VISIBLE
        return inflater.inflate(R.layout.fragment_assignment_finished, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoadingOverlay(view)
        getUpdatedDelivery { delivery -> run {
            setOnClickListeners(delivery)
            setLayout(delivery)
            showContent(view)
        } }

    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    fun setLayout(delivery: Delivery){

        if(delivery.status != 4) {  // 4 = Afgeleverd
            lbl_status_text.text = getString(R.string.lbl_delivery_cancelled)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                lbl_status_text.setTextColor(activity!!.resources.getColor(R.color.light_red, activity!!.theme))
            }
        } else{
            lbl_status_text.text = getString(R.string.lbl_delivery_delivered)
        }

        lbl_success_earnings.text = delivery.price.toBigDecimal().setScale(2).toString()

        getEarnings("day") { earnings -> run {
            lbl_success_total_earnings_day.text = earnings
        }}
        getEarnings("week") { earnings -> run {
            lbl_success_total_earnings_week.text = earnings
        }}
        getEarnings("month") { earnings -> run {
            lbl_success_total_earnings_month.text = earnings
        }}

        val formattedTime: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDateTime = LocalDateTime.parse(LocalDateTime.now().toString())
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            formattedTime = formatter.format(localDateTime)
        }
        else {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val formatter = SimpleDateFormat("HH:mm")
            formattedTime = formatter.format(parser.parse(delivery.dueDate!!)!!)
        }
        lbl_successful_datetime.text = formattedTime

        when(delivery.vehicle) {
            1 -> {
                lbl_success_vehicle.text = getString(R.string.V1)
            }
            2 or 3 -> {
                lbl_success_vehicle.text = getString(R.string.V2_3)
            }
            4 -> {
                lbl_success_vehicle.text = getString(R.string.V4)
            }
        }
    }

    private fun setOnClickListeners(delivery: Delivery){
        btn_check_details.setOnClickListener {
            val intent = Intent(activity, MyBDHistoryDetails::class.java)


            val d = delivery!!

            intent.putExtra("timeAccepted", d.warehousePickUpAt)

            //warehouse
            intent.putExtra("warehouseAddress", d.warehouse.address)
            intent.putExtra("warehouseDistance", d.warehouseDistanceInKilometers)
            intent.putExtra("warehousePickUp", d.warehousePickUpAt)
            //customer
            intent.putExtra("customerAddress", d.customer.address)
            intent.putExtra("customerDistance", d.customerDistanceInKilometers)
            intent.putExtra("customerDeliveredAt", d.deliveredAt)
            //price
            intent.putExtra("price", d.price)
            intent.putExtra("tip", d.tip)
            //status+vehicle
            intent.putExtra("statusDisplayName", d.status) // TODO: show display name
            intent.putExtra("status", d.status)
            intent.putExtra("vehicle", d.vehicle)
            startActivity(intent)
        }

        btn_new_assignment.setOnClickListener {
            val fragment = NoAssignmentFragment()
            replaceFragment(R.id.content, fragment)
        }
    }

    private fun getEarnings(timeFrame: String, callback: (String) -> Unit) {
        val decryptedToken = getDecryptedToken(context!!)
        apiService.delivererGetEarnings(decryptedToken, timeFrame)
            .enqueue(object: Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if(response.isSuccessful && response.body() != null) {
                        val earnings = response.body()!!
                        callback(earnings)
                    }
                    else Log.e("NOTIFICATION", "Earnings call unsuccessful or body empty")
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("NOTIFICATION", "Something went wrong with the earnings call (getEarnings)")
                }
            })
    }

    private fun getUpdatedDelivery(callback: (Delivery) -> Unit) {
        val decryptedToken = getDecryptedToken(context!!)
        apiService.deliveryGetById(decryptedToken, deliveryId)
            .enqueue(object: Callback<Delivery> {
                override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                    if(response.isSuccessful && response.body() != null) {
                        val delivery = response.body()!!
                        callback(delivery)
                    }
                    else Log.e("NOTIFICATION", "Delivery call unsuccessful or body empty")
                }

                override fun onFailure(call: Call<Delivery>, t: Throwable) {
                    Log.e("NOTIFICATION", "Something went wrong with the delivery call (getUpdatedDelivery)")
                }
            })
    }
}