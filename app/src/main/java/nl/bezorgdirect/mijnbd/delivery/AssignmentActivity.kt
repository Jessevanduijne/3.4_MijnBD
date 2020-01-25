package nl.bezorgdirect.mijnbd.delivery


import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.fromParts
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
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
    var perm = 0

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
    private fun locPermissionDialog()
    {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(layout.dialog_location_permission)

        val window = dialog.window
        window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val btn_settings = dialog.findViewById(id.btn_perm_go_settings) as Button
        btn_settings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = fromParts("package", packageName, null)
            intent.data = uri
            dialog.hide()
            startActivity(intent)
            perm--
        }
        val btn_close = dialog.findViewById(id.btn_perm_close_app) as Button
        btn_close.setOnClickListener {
            dialog.hide()
            finishAndRemoveTask()
        }
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.e("PERMISSIONS", "Location permissions granted by user")
            }
            else
            {
                //Log.e("PERMISSIONS", "Location permissions denied by user")
                perm++
                locPermissionDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(perm == 0)
        {
            LocationHelper(this).checkLocationPermission()
        }

    }
    override fun onBackPressed() {

    }
}
