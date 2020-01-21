package nl.bezorgdirect.mijnbd.delivery

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_new_delivery.*
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
import java.util.*


class NewAssignmentFragment : Fragment() {

    private val apiService = getApiService()
    private var timer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getNotificationId { notification -> run {
                getDeliveryById(notification.delivery.id!!) { delivery -> run {
                        setClickListeners(notification.id!!, delivery)
                        setLayoutData(delivery)
                        setTimer(notification, delivery)
                    }
                }
            }
        }
        val custom_toolbar_title: TextView = activity!!.findViewById(R.id.custom_toolbar_title)
        custom_toolbar_title.text = getString(R.string.title_assignment)
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
            val localDateTime = LocalDateTime.parse(delivery.dueDate)
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            formattedTime = formatter.format(localDateTime)
        }
        else {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val formatter = SimpleDateFormat("HH:mm")
            formattedTime = formatter.format(parser.parse(delivery.dueDate!!)!!)
        }

        lbl_new_assignment_earnings.text = delivery.price!!.toBigDecimal().setScale(2).toString()
        lbl_new_assignment_due_date.text = formattedTime
        lbl_new_assignment_vehicle.text = delivery.vehicle.toString() // TODO: get display name

        when(delivery.vehicle)
        {
            1 -> img_new_assignment_vehicle.setImageResource(R.drawable.ic_bike_y)
            2 -> img_new_assignment_vehicle.setImageResource(R.drawable.ic_motor_y)
            3 -> img_new_assignment_vehicle.setImageResource(R.drawable.ic_motor_y)
            4 -> img_new_assignment_vehicle.setImageResource(R.drawable.ic_car_y)
        }

        // Google API data:
        setDistanceData(delivery)
    }

    private fun setClickListeners(notificationId: String, delivery: Delivery){
        btn_delivery_accept.setOnClickListener {
            confirmAssignment(true, notificationId, delivery)
        }

        btn_delivery_refuse.setOnClickListener{
            canReceiveNotification = true
            confirmAssignment(false, notificationId, delivery)
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
        showSpinner(view!!)

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

        // Location on time of accepting assignment:
        locationHelper.getLastLocation { location -> run {
            val updateStatusBody = UpdateStatusParams(2, location.latitude, location.longitude) // status 2 = bevestigd

            apiService.deliverystatusPatch(decryptedToken, delivery.id!!, updateStatusBody)
                .enqueue(object: Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if(response.isSuccessful) {

                            timer!!.cancel()
                            val fragment = RetrievingFragment(delivery)
                            replaceFragment(R.id.delivery_fragment, fragment)
                        }
                        else Log.e("NEW_ASSIGNMENT", "Updating delivery status response unsuccessful")
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("NEW_ASSIGNMENT", "Updating delivery by delivery by deliveryId failed")
                    }
                })
        } }
    }

    private fun setDistanceData(delivery: Delivery){
        val service = getGoogleService()
        val locationHelper = LocationHelper(this.activity!!)
        val apiKey = getString(R.string.google_maps_key)

        val warehouseDestination = "${delivery.warehouse.latitude},${delivery.warehouse.longitude}"
        val clientDestination = "${delivery.customer.latitude},${delivery.customer.longitude}"

        var travelDuration = 0
        var travelMode = ""
        when(delivery.vehicle)
        {
            1 -> travelMode = "bicycling"
            2 or 3 or 4 -> travelMode = "driving"
        }

        // Location on opening screen (won't be saved):
        locationHelper.getLastLocation { location -> run {
            val startLocation = "${location.latitude},${location.longitude}"
            val toWarehouseDistanceCall = service.getDistance(startLocation, warehouseDestination, apiKey, travelMode)
            toWarehouseDistanceCall.enqueue(object: Callback<GoogleDistance> {
                override fun onResponse(call: Call<GoogleDistance>, response: Response<GoogleDistance>) {
                    if(response.isSuccessful && response.body() != null) {
                        val result = response.body()
                        val distanceInformation = result!!.rows.get(0).elements.get(0)
                        val toWarehouseDistance = distanceInformation.distance.text.removeLetters()
                        val toWarehouseDuration = distanceInformation.duration.text.removeLetters()
                        travelDuration += toWarehouseDuration.toInt()
                        lbl_to_warehouse_kilometers.text = toWarehouseDistance
                        lbl_new_assignment_estimated_minutes.text = travelDuration.toString()
                    }
                }
                override fun onFailure(call: Call<GoogleDistance>, t: Throwable) {
                    Log.e("NEW_ASSIGNMENT", "Retrieving distance failed for current - warehouse")
                }
            })

            val toClientDistanceCall = service.getDistance(warehouseDestination, clientDestination, apiKey, travelMode)
            toClientDistanceCall.enqueue(object: Callback<GoogleDistance> {
                override fun onResponse(call: Call<GoogleDistance>, response: Response<GoogleDistance>) {
                    if(response.isSuccessful && response.body() != null) {
                        val result = response.body()
                        val distanceInformation = result!!.rows.get(0).elements.get(0)
                        val toClientDistance = distanceInformation.distance.text.removeLetters()
                        val toClientDuration = distanceInformation.duration.text.removeLetters()
                        travelDuration += toClientDuration.toInt()

                        lbl_to_client_kilometers.text = toClientDistance
                        lbl_new_assignment_estimated_minutes.text = travelDuration.toString()
                    }
                }
                override fun onFailure(call: Call<GoogleDistance>, t: Throwable) {
                    Log.e("NEW_ASSIGNMENT", "Retrieving distance failed for warehouse - customer")
                }
            })
        } }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setTimer(notification: BDNotification, delivery: Delivery){

        val currentTime = Calendar.getInstance().time
        val expirationTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(notification.expiredAt!!)
        val diffInMilliSec = (expirationTime!!.time - currentTime.time)
        val maxResponseTimeMilliSec = 10 * 60 * 1000
        val minute = 60 * 1000

        timer = object: CountDownTimer(diffInMilliSec, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutesLeft = millisUntilFinished / 1000 / 60
                val secondsLeft = (millisUntilFinished / 1000) - (minutesLeft * 60)
                val percentageTimeLeft = (100 - ((millisUntilFinished.toFloat() / maxResponseTimeMilliSec.toFloat()) * 100))

                if(lbl_new_assignment_minutes_to_accept != null) {
                    lbl_new_assignment_minutes_to_accept.text = minutesLeft.toString()
                    lbl_new_assignment_seconds_to_accept.text = secondsLeft.toString()
                }

                if(percentageTimeLeft < minute) {
                    pgb_decision_timer.progress = Color.RED
                }
                pgb_decision_timer.progress = percentageTimeLeft.toInt()
            }

            override fun onFinish() {
                //confirmAssignment(false, notification.id!!, delivery) TODO: repair timer
            }
        }
        timer!!.start()
    }
}
