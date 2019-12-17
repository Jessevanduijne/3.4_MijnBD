package nl.bezorgdirect.mijnbd.delivery

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_new_delivery.*
import kotlinx.android.synthetic.main.spinner.*
import nl.bezorgdirect.mijnbd.MijnbdApplication.Companion.canReceiveNotification
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.*
import nl.bezorgdirect.mijnbd.helpers.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class NewAssignmentFragment : Fragment() {

    private val apiService = getApiService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getNotificationId { notification -> run {
                getDeliveryById(notification.DeliveryId!!) { delivery -> run {
                        setClickListeners(notification.Id!!, delivery)
                        setLayoutData(delivery)
                    }
                }
            }
        }
        return inflater.inflate(R.layout.fragment_new_delivery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideSpinner(view)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setLayoutData(delivery: Delivery){
        val formattedTime: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDateTime = LocalDateTime.parse(delivery.DueDate)
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            formattedTime = formatter.format(localDateTime)
        }
        else {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val formatter = SimpleDateFormat("HH:mm")
            formattedTime = formatter.format(parser.parse(delivery.DueDate!!)!!)
        }

        lbl_new_assignment_earnings.text = delivery.Price!!.toBigDecimal().setScale(2).toString()
        lbl_new_assignment_due_date.text = formattedTime
        lbl_new_assignment_vehicle.text = delivery.VehicleDisplayName

        when(delivery.Vehicle)
        {
            1 -> img_new_assignment_vehicle.setImageResource(R.drawable.ic_bike_y)
            2 -> img_new_assignment_vehicle.setImageResource(R.drawable.ic_motor_y)
            3 -> img_new_assignment_vehicle.setImageResource(R.drawable.ic_motor_y)
            4 -> img_new_assignment_vehicle.setImageResource(R.drawable.ic_car_y)
        }

        // TODO: Calculate this info with the CreatedAt property from notification
//        lbl_new_assignment_minutes_to_accept.text = ""
//        lbl_new_assignment_seconds_to_accept.text = ""

        // TODO: Get this info from google API
//        lbl_new_assignment_kilometers.text = ""
//        lbl_new_assignment_estimated_minutes.text = ""
    }

    private fun setClickListeners(notificationId: String, delivery: Delivery){
        btn_delivery_accept.setOnClickListener {

            showSpinner(view!!)
            confirmAssignment(true, notificationId, delivery)
        }

        btn_delivery_refuse.setOnClickListener{
            showSpinner(view!!)
            confirmAssignment(false, notificationId, delivery)
            canReceiveNotification = true
        }
    }

    private fun getDeliveryById(deliveryId: String, callback: (Delivery) -> Unit) {
        val decryptedToken = getDecryptedToken(context!!)
        apiService.deliveryGetById(decryptedToken, deliveryId)
            .enqueue(object: Callback<Delivery> {
                override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                    if(response.isSuccessful && response.body() != null) {
                        val delivery = response.body()!!
                        callback(delivery)
                    }
                    else Log.e("NOTIFICATION", "delivery call unsuccessful or body empty")
                }

                override fun onFailure(call: Call<Delivery>, t: Throwable) {
                    Log.e("NOTIFICATION", "Something went wrong with the delivery call (getDeliveryForNotification)")
                }
            })
    }

    private fun getNotificationId(callback: (BDNotification) -> Unit) {
        val decryptedToken = getDecryptedToken(context!!)
        apiService.notificationGet(decryptedToken)
            .enqueue(object: Callback<BDNotification> {
                override fun onResponse(call: Call<BDNotification>, response: Response<BDNotification>) {
                    if(response.isSuccessful && response.body() != null) {
                        val notification: BDNotification = response.body()!!
                        callback(notification)
                    }
                    else Log.e("NEW_ASSIGNMENT", "Get notification ID unsuccessful or empty body")
                }

                override fun onFailure(call: Call<BDNotification>, t: Throwable) {
                    Log.e("NEW_ASSIGNMENT", "Get notification ID failed miserably")
                }
            })
    }

    private fun confirmAssignment(accepted: Boolean, notificationId: String, delivery: Delivery){
        val decryptedToken = getDecryptedToken(this.activity!!)

        val updateNotificationBody = UpdateNotificationParams(accepted)
        apiService.notificationPatch(decryptedToken, notificationId, updateNotificationBody)
            .enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.isSuccessful) {
                        if(accepted) {
                            updateDeliveryStatus(delivery)
                        }
                        else {
                            val fragment = NoAssignmentFragment()
                            replaceFragment(R.id.delivery_fragment, fragment)
                        }
                    }
                    else Log.e("NEW_ASSIGNMENT", "Confirming assignment response unsuccessful")
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("NEW_ASSIGNMENT", "Confirming assignment failed")
                }
            })
    }

    private fun updateDeliveryStatus(delivery: Delivery){ // Updates status & location

        val decryptedToken = getDecryptedToken(this.activity!!)
        val locationHelper = LocationHelper(this.activity!!)

        locationHelper.getLastLocation { location -> run {
            val updateStatusBody = UpdateStatusParams(2, location.latitude, location.longitude) // status 2 = bevestigd

            apiService.deliverystatusPatch(decryptedToken, delivery.Id!!, updateStatusBody)
                .enqueue(object: Callback<Delivery> {
                    override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                        if(response.isSuccessful && response.body() != null) {
                            val updatedAssignment = response.body()!!

                            val fragment = DeliveringFragment(updatedAssignment)
                            replaceFragment(R.id.delivery_fragment, fragment)
                        }
                        else Log.e("NEW_ASSIGNMENT", "Updating delivery status response unsuccessful")
                    }
                    override fun onFailure(call: Call<Delivery>, t: Throwable) {
                        Log.e("NEW_ASSIGNMENT", "Updating delivery by delivery by deliveryId failed")
                    }
                })
        } }
    }
}
