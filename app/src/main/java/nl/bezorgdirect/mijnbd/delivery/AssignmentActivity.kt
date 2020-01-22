package nl.bezorgdirect.mijnbd.delivery


import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.MijnbdApplication.Companion.canReceiveNotification
import nl.bezorgdirect.mijnbd.R.*
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.helpers.*
import nl.bezorgdirect.mijnbd.history.MyBDHistory
import nl.bezorgdirect.mijnbd.mijnBD.MyBDActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response




class AssignmentActivity : AppCompatActivity() {
    val apiService = getApiService()
    val PERMISSION_ID = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_assignment)

        LocationHelper(this).checkLocationPermission()

        bottom_navigation.selectedItemId = id.action_deliveries
        custom_toolbar_title.text = getString(string.title_assignment)
        setSupportActionBar(custom_toolbar)
        setFragment() // Sets the initial state

        hideNotification()
    }

    private fun hideNotification(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun setFragment() {
        val view = window.decorView.rootView
        showSpinner(view)

        val decryptedToken = getDecryptedToken(this)
        apiService.deliveryGetCurrent(decryptedToken)
            .enqueue(object: Callback<Delivery> {
                override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                    if(response.isSuccessful && response.body() != null) {
                        val delivery = response.body()
                        val currentLocation = LatLng(delivery!!.current.latitude!!, delivery!!.current.longitude!!)
                        val deliveringFragment = RetrievingFragment(delivery, currentLocation)
                        replaceFragment(id.delivery_fragment, deliveringFragment)
                        canReceiveNotification = false
                    }
                    else {
                        if(canReceiveNotification) {
                            val noAssignmentFragment = NoAssignmentFragment()
                            replaceFragment(id.delivery_fragment, noAssignmentFragment)
                            setBottomNav()
                        }
                        else {
                            val newAssignmentFragment = NewAssignmentFragment()
                            replaceFragment(id.delivery_fragment, newAssignmentFragment)
                            setBottomNav()
                        }
                    }
                    hideSpinner(view)
                }
                override fun onFailure(call: Call<Delivery>, t: Throwable) {
                    Log.e("ASSIGNMENT", "Something went wrong with the Get Delivery call in AssignmentActivity")
                    val noAssignmentFragment = NoAssignmentFragment()
                    replaceFragment(id.delivery_fragment, noAssignmentFragment)
                    hideSpinner(view)
                    setBottomNav()
                }
            })
    }

    private fun setBottomNav(){

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                id.action_history -> {
                    val myBDHistory = MyBDHistory()
                    supportFragmentManager.beginTransaction().replace(id.delivery_fragment, myBDHistory).commit()
                }
                id.action_deliveries -> setFragment()
                id.action_mybd -> {
                    val myBD = MyBDActivity()
                    supportFragmentManager.beginTransaction().replace(id.delivery_fragment, myBD).commit()
                }
            }
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.e("PERMISSIONS", "Location permissions granted by user")
            }
            else
            {
                Log.e("PERMISSIONS", "Location permissions denied by user")
                //todo dialog app doesnt work without location permission
            }
        }
    }
    override fun onBackPressed() {

    }
}
