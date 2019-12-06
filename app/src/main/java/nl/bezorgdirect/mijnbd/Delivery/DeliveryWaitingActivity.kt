package nl.bezorgdirect.mijnbd.Delivery

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_delivery_waiting.*
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.History.MyBDHistory


class DeliveryWaitingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(nl.bezorgdirect.mijnbd.R.layout.activity_delivery_waiting)

        custom_toolbar_title.setText(getString(nl.bezorgdirect.mijnbd.R.string.title_deliveries))
        setSupportActionBar(custom_toolbar)

        bottom_navigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.getItemId()) {
                    nl.bezorgdirect.mijnbd.R.id.action_history -> {
                        val intent = Intent(this@DeliveryWaitingActivity, MyBDHistory::class.java)
                        finish()  //Kill the activity from which you will go to next activity
                        startActivity(intent)
                    }
                    nl.bezorgdirect.mijnbd.R.id.action_deliveries -> Toast.makeText(
                        this@DeliveryWaitingActivity,
                        "deliveries",
                        Toast.LENGTH_SHORT
                    ).show()
                    nl.bezorgdirect.mijnbd.R.id.action_mybd -> Toast.makeText(
                        this@DeliveryWaitingActivity,
                        "mybd",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
        })

        btn_test_new_activity.setOnClickListener {
            val intent = Intent(this@DeliveryWaitingActivity, DeliveryNewActivity::class.java)
            startActivity(intent)
        }
    }
}
