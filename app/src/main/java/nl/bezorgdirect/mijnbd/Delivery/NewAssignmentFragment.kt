package nl.bezorgdirect.mijnbd.Delivery

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat.isLocationEnabled
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_new_delivery.*
import nl.bezorgdirect.mijnbd.MijnbdApplication.Companion.canReceiveNotification
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.BDNotification
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.api.UpdateNotificationParams
import nl.bezorgdirect.mijnbd.api.UpdateStatusParams
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import nl.bezorgdirect.mijnbd.helpers.replaceFragment
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class NewAssignmentFragment : Fragment() {

    private val apiService = getApiService()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lat: Double = 52.44473779
    private var long: Double = 4.661227 // TODO: Edit these values

    val PERMISSION_ID = 42
    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
        getLastLocation()

        getNotificationId { notification -> run {
                getDeliveryById(notification.DeliveryId!!) { delivery -> run {
                        setClickListeners(notification.Id!!, delivery)
                        setLayoutData(delivery)
                    }
                }
            }
        }
        return inflater.inflate(nl.bezorgdirect.mijnbd.R.layout.fragment_new_delivery, container, false)
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
            confirmAssignment(true, notificationId, delivery)
        }

        btn_delivery_refuse.setOnClickListener{
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
        apiService.notificationPatch(decryptedToken, notificationId, updateNotificationBody) // TODO: Get NotificationID from Notification object
            .enqueue(object: Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.isSuccessful) {
                        if(accepted) {
                            updateDeliveryStatusManually(delivery)
                        }
                        else {
                            val fragment = NoAssignmentFragment()
                            replaceFragment(nl.bezorgdirect.mijnbd.R.id.delivery_fragment, fragment)
                        }
                    }
                    else Log.e("NEW_ASSIGNMENT", "Confirming assignment response unsuccessful")
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("NEW_ASSIGNMENT", "Confirming assignment failed")
                }
            })
    }

    private fun updateDeliveryStatusManually(delivery: Delivery){ // The API doesn't update the delivery after accepting notification

        val updateStatusBody = UpdateStatusParams(2, lat.toFloat(), long.toFloat()) // status 2 = bevestigd
        val decryptedToken = getDecryptedToken(this.activity!!)
        apiService.deliverystatusPatch(decryptedToken, delivery.Id!!, updateStatusBody)
            .enqueue(object: Callback<Delivery> {
                override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                    if(response.isSuccessful) {
                        val latlong = LatLng(lat, long)
                        val fragment = DeliveringFragment(delivery, latlong)
                        replaceFragment(R.id.delivery_fragment, fragment)
                    }
                    else Log.e("NEW_ASSIGNMENT", "Updating delivery status response unsuccessful")
                }
                override fun onFailure(call: Call<Delivery>, t: Throwable) {
                    Log.e("NEW_ASSIGNMENT", "Updating delivery by delivery by deliveryId failed")
                }
            })
    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this.activity!!) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        lat = location.latitude
                        long = location.longitude
                    }
                }
            } else {
                Toast.makeText(this.activity!!, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            println(mLastLocation.longitude)
            lat = mLastLocation.latitude
            long = mLastLocation.longitude
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this.activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this.activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this.activity!!,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

}
