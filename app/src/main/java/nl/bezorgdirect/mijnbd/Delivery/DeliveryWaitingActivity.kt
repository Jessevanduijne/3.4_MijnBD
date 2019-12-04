package nl.bezorgdirect.mijnbd.Delivery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_delivery_waiting.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R

class DeliveryWaitingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_waiting)

        custom_toolbar_title.setText(getString(R.string.title_deliveries))
        setSupportActionBar(custom_toolbar)

        btn_test_new_activity.setOnClickListener {
            val intent = Intent(this@DeliveryWaitingActivity, DeliveryNewActivity::class.java)
            startActivity(intent)
        }
    }
}
