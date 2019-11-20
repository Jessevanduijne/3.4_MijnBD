package nl.bezorgdirect.mijnbd.Delivery

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_choose_delivery.*
import kotlinx.android.synthetic.main.activity_new_delivery.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R

class NewDeliveryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_delivery)

        custom_toolbar_title.setText(getString(R.string.title_deliveries))
        setSupportActionBar(custom_toolbar)

        btn_accept_delivery.setOnClickListener {
            val intent = Intent(this@NewDeliveryActivity, ToWarehouseActivity::class.java)
            startActivity(intent)
        }
    }
}
