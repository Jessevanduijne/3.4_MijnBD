package nl.bezorgdirect.mijnbd.Delivery


import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.History.MyBDHistory
import nl.bezorgdirect.mijnbd.MijnbdApplication.Companion.canReceiveNotification
import nl.bezorgdirect.mijnbd.MyBD.MyBDActivity
import nl.bezorgdirect.mijnbd.R.*
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import nl.bezorgdirect.mijnbd.helpers.replaceFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AssignmentActivity : AppCompatActivity(), NewAssignmentListener {
    val apiService = getApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_assignment)

        custom_toolbar_title.setText(getString(string.title_assignment))
        setSupportActionBar(custom_toolbar)
        setBottomNav()
        setFragment()
    }

    override fun onNewAssignment() {
        val newAssignmentFragment = NewAssignmentFragment()
        replaceFragment(id.delivery_fragment, newAssignmentFragment)
    }

    private fun setFragment() {
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
                }
                override fun onFailure(call: Call<Delivery>, t: Throwable) {
                    Log.e("ASSIGNMENT", "Something went wrong with the Get Delivery call in AssignmentActivity")
                }
            })
    }

    private fun setBottomNav(){
        bottom_navigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.getItemId()) {
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
                return true
            }
        })
    }


}
