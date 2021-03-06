package nl.bezorgdirect.mijnbd.delivery

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_new_delivery.*
import nl.bezorgdirect.mijnbd.MijnbdApplication.Companion.canReceiveNotification
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.*
import nl.bezorgdirect.mijnbd.helpers.*
import nl.bezorgdirect.mijnbd.services.NotificationService
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
    private var warehouseDistance = ""
    private var clientDistance = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getNotification { notification -> run {
                val delivery = notification.delivery
                setClickListeners(notification.id, delivery)
                setLayoutData(delivery)
                setTimer(notification)

                setDistanceData(delivery) {
                    val content: LinearLayout = activity!!.findViewById(R.id.layout_delivery_decision)
                    content.visibility = View.VISIBLE
                    hideSpinner(this.view!!)
                }
            }
        }
        val custom_toolbar_title: TextView = activity!!.findViewById(R.id.custom_toolbar_title)
        custom_toolbar_title.text = getString(R.string.title_assignment)
        return inflater.inflate(R.layout.fragment_new_delivery, container, false)
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

        when(delivery.vehicle) {
            1 -> {
                img_new_assignment_vehicle.setImageResource(R.drawable.ic_bike_y)
                lbl_new_assignment_vehicle.text = getString(R.string.V1)
            }
            2 or 3 -> {
                img_new_assignment_vehicle.setImageResource(R.drawable.ic_motor_y)
                lbl_new_assignment_vehicle.text = getString(R.string.V2_3)
            }
            4 -> {
                img_new_assignment_vehicle.setImageResource(R.drawable.ic_car_y)
                lbl_new_assignment_vehicle.text = getString(R.string.V4)
            }
        }
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

    private fun getNotification(callback: (BDNotification) -> Unit) {
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
                            replaceFragment(R.id.content, fragment)
                            canReceiveNotification = true
                            startNotificationService()
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
            val updateStatusBody = UpdateStatusParams(2, location.latitude, location.longitude, warehouseDistance.toFloat(), clientDistance.toFloat()) // status 2 = bevestigd

            apiService.deliverystatusPatch(decryptedToken, delivery.id!!, updateStatusBody)
                .enqueue(object: Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if(response.isSuccessful) {
                            timer?.cancel()
                            val fragment = RetrievingFragment(delivery, location)
                            replaceFragment(R.id.content, fragment)
                        }
                        else Log.e("NEW_ASSIGNMENT", "Updating delivery status response unsuccessful")
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("NEW_ASSIGNMENT", "Updating delivery by delivery by deliveryId failed")
                    }
                })
        } }
    }

    private fun setDistanceData(delivery: Delivery, callback:() -> Unit){
        val service = getGoogleService()
        val locationHelper = LocationHelper(this.activity!!)
        val apiKey = getString(R.string.google_maps_key)
        var callCount = 0

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

                        if(lbl_to_client_kilometers != null) {
                            lbl_to_warehouse_kilometers.text = toWarehouseDistance
                            lbl_new_assignment_estimated_minutes.text = travelDuration.toString()
                        }
                        callCount++
                        if(callCount == 2) {
                            callback()
                        }

                        warehouseDistance = toWarehouseDistance
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

                        if(lbl_to_client_kilometers != null) {
                            lbl_to_client_kilometers.text = toClientDistance
                            lbl_new_assignment_estimated_minutes.text = travelDuration.toString()
                        }

                        callCount++
                        if(callCount == 2) {
                            callback()
                        }

                        clientDistance = toClientDistance
                    }
                }
                override fun onFailure(call: Call<GoogleDistance>, t: Throwable) {
                    Log.e("NEW_ASSIGNMENT", "Retrieving distance failed for warehouse - customer")
                }
            })
        } }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setTimer(notification: BDNotification){

        val currentTime = Calendar.getInstance().time
        val expirationTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(notification.expiredAt!!)
        val diffInMilliSec = (expirationTime!!.time - currentTime.time)
        val apiUpdateToExpiredTime = 2
        val maxResponseTimeMilliSec = (10 * 60 * 1000)
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

                if(pgb_decision_timer != null) {
                    if(percentageTimeLeft < minute) {
                        pgb_decision_timer.progress = Color.RED
                    }
                    pgb_decision_timer.progress = percentageTimeLeft.toInt()
                }
            }

            override fun onFinish() {
                // api autmatically updates assignments to expired
                val fragment = NoAssignmentFragment()
                replaceFragment(R.id.content, fragment)
            }
        }
        timer?.start()
    }

    private fun startNotificationService(){
        val activityManager = activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val notificationServiceClass = NotificationService::class.java
        val notificationIntent = Intent(this.context, notificationServiceClass)

        var notificationServiceIsRunning = false
        // Loop through running services
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (notificationServiceClass.name == service.service.className) {
                // If the service is running then return true
                notificationServiceIsRunning = true
            }
        }

        if(!notificationServiceIsRunning) {
            activity?.startService(notificationIntent)
        }
        else Log.e("NOTIFICATION", "Notification service already started")
    }
}
