package nl.bezorgdirect.mijnbd.history

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_my_bdhistory_details.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class MyBDHistoryDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bdhistory_details)

        custom_toolbar_title.setText(getString(R.string.title_order_details))
        setSupportActionBar(custom_toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        history_arrow.bringToFront()

        lbl_status.text = intent.getStringExtra("statusDisplayName")
        lbl_date.text = getDate(intent.getStringExtra("customerDeliveredAt"))
        lbl_time.text = getFromTo(intent.getStringExtra("customerDeliveredAt"), intent.getStringExtra("timeAccepted"))
        lbl_total_reward.text = getTotalReward(intent.getFloatExtra("price", -1.0f), intent.getFloatExtra("tip", -1.0f))
        lbl_total_distance.text = getTotalDistance(intent.getIntExtra("customerDistance", -1), intent.getIntExtra("warehouseDistance", -1))

        lbl_ready_warehouse_time.text = getTime(intent.getStringExtra("wareHouseReady"))
        lbl_accepted_time.text = getTime(intent.getStringExtra("timeAccepted"))

        lbl_warehouse_address.text = checkNullString(intent.getStringExtra("warehouseAddress"))
        lbl_warehouse_pickup_time.text = getTime(intent.getStringExtra("warehousePickUp"))
        lbl_warehouse_distance_value.text = checkNullInt(intent.getIntExtra("warehouseDistance", -1))

        lbl_customer_address.text = checkNullString(intent.getStringExtra("customerAddress"))
        lbl_customer_delivery_time.text = getTime(intent.getStringExtra("customerDeliveredAt"))
        lbl_customer_distance_value.text = checkNullInt(intent.getIntExtra("customerDistance", -1))

        lbl_fail_reason.text = "none"
        lbl_reward_value.text = roundFloat(intent.getFloatExtra("price", -1.0f))
        lbl_reward_type.text = "Auto levering"
        lbl_tip_value.text = roundFloat(intent.getFloatExtra("tip", -1.0f))

        lbl_total_reward_value.text = getTotalReward(intent.getFloatExtra("price", -1.0f), intent.getFloatExtra("tip", -1.0f))
       /* var delivery : Delivery? =  intent.getParcelableExtra("delivery")
        if(delivery !=  null)
        {
            print(delivery)
        }*/

    }

    fun checkNullInt(input: Int?) :String
    {
        var output = ""
        if(input == -1 || input == null)
        {
            output = "?"
        }
        else
        {
            output = "$input"
        }
        return output
    }
    fun checkNullString(input: String?) :String
    {
        var output = ""
        if(input == "" || input == null)
        {
            output = "?"
        }
        else
        {
            output = "$input"
        }
        return output
    }
    fun getDate(input: String?) :String
    {
        var output = "?"
        if(input != null && input != "") {
            val outputFormat = SimpleDateFormat("dd-MM-yyyy")
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val datetime: Date = inputFormat.parse(input)

            output = outputFormat.format(datetime)
        }

        return output
    }
    fun getTime(input: String?) :String
    {
        var output = "?"
        if(input != null && input != "") {
            val outputFormat = SimpleDateFormat("HH:mm")
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

            val datetime: Date = inputFormat.parse(input)

            output = outputFormat.format(datetime)
        }
        return output
    }
    fun getFromTo(from: String?, to:String?) :String
    {
        var start = getTime(from)
        var end = getTime(to)
        var output="$start - $end"

        return output
    }
    fun getTotalDistance(warehouse: Int, customer: Int): String
    {
        var output = ""
        if(warehouse == -1 || customer == -1)
        {
            output = "?"
        }
        else
        {
            var total = warehouse+customer
            output = "$total km."
        }
        return output
    }
    fun roundFloat(input : Float) : String
    {
        var output = "?"
        if(input != -1.0f ) {
            var rounded = round(input * 100) / 100
            output = "$rounded"
        }
        return output
    }
    fun getTotalReward(price: Float, tip: Float): String
    {
        var output = ""
        if(price == -1.0f || tip == -1.0f)
        {
            output = "?"
        }
        else
        {
            val total = price+tip
            val totalstring = roundFloat(total)
            output = "€$totalstring"
        }
        return output
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }
}
