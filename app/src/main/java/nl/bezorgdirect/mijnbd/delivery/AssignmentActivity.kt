package nl.bezorgdirect.mijnbd.delivery


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.history.MyBDHistory
import nl.bezorgdirect.mijnbd.MijnbdApplication.Companion.canReceiveNotification
import nl.bezorgdirect.mijnbd.mijnBD.MyBDActivity
import nl.bezorgdirect.mijnbd.R.*
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AssignmentActivity : AppCompatActivity() {
    val apiService = getApiService()
    val PERMISSION_ID = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_assignment)

        custom_toolbar_title.text = getString(string.title_assignment)
        setSupportActionBar(custom_toolbar)
        setBottomNav()
        setFragment() // Sets the initial state
    }

    private fun setFragment() {
        val view = window.decorView.rootView
        showSpinner(view)

        val decryptedToken = getDecryptedToken(this)
        apiService.deliveryGet(decryptedToken)
            .enqueue(object: Callback<Delivery> {
                override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                    if(response.isSuccessful && response.body() != null) {
                        val delivery = response.body()
                        val deliveringFragment = DeliveringFragment(delivery)
                        replaceFragment(id.delivery_fragment, deliveringFragment)
                        canReceiveNotification = false
                    }
                    else {
                        if(canReceiveNotification) {
                            val noAssignmentFragment = NoAssignmentFragment()
                            replaceFragment(id.delivery_fragment, noAssignmentFragment)
                        }
                        else {
                            val newAssignmentFragment = NewAssignmentFragment()
                            replaceFragment(id.delivery_fragment, newAssignmentFragment)
                        }
                    }
                    hideSpinner(view)
                }
                override fun onFailure(call: Call<Delivery>, t: Throwable) {
                    Log.e("ASSIGNMENT", "Something went wrong with the Get Delivery call in AssignmentActivity")
                    val noAssignmentFragment = NoAssignmentFragment()
                    replaceFragment(id.delivery_fragment, noAssignmentFragment)
                    hideSpinner(view)
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
            else Log.e("PERMISSIONS", "Location permissions denied by user")
        }
    }
}
