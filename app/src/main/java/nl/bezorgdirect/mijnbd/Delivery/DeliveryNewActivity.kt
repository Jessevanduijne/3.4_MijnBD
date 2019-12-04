package nl.bezorgdirect.mijnbd.Delivery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_delivery_new.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R

class DeliveryNewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_new)

        custom_toolbar_title.setText(getString(R.string.title_deliveries))
        setSupportActionBar(custom_toolbar)

        btn_delivery_accept.setOnClickListener {
            val intent = Intent(this@DeliveryNewActivity, DeliveryDeliveringActivity::class.java)
            startActivity(intent)
        }
    }
}
