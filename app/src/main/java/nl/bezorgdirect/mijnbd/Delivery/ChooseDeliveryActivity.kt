package nl.bezorgdirect.mijnbd.Delivery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R

class ChooseDeliveryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_delivery)

        custom_toolbar_title.setText(getString(R.string.title_deliveries))
        setSupportActionBar(custom_toolbar)

    }
}
