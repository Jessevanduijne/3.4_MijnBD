package nl.bezorgdirect.mijnbd.history

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        setVehicle()
        setStatus()
        lbl_date.text = getDate(intent.getStringExtra("customerDeliveredAt"))
        lbl_time.text = getFromTo(intent.getStringExtra("customerDeliveredAt"), intent.getStringExtra("timeAccepted"))
        lbl_total_reward.text = getTotalReward(intent.getFloatExtra("price", -1.0f), intent.getFloatExtra("tip", -1.0f))
        lbl_total_distance.text = getTotalDistance(intent.getFloatExtra("customerDistance", 0.0f), intent.getFloatExtra("warehouseDistance", 0.0f))

        lbl_ready_warehouse_time.text = getTime(intent.getStringExtra("wareHouseReady"))
        lbl_accepted_time.text = getTime(intent.getStringExtra("timeAccepted"))

        lbl_warehouse_address.text = checkNullString(intent.getStringExtra("warehouseAddress"))
        lbl_warehouse_pickup_time.text = getTime(intent.getStringExtra("warehousePickUp"))
        lbl_warehouse_distance_value.text = roundFloat(intent.getFloatExtra("warehouseDistance", -1.0f))

        lbl_customer_address.text = checkNullString(intent.getStringExtra("customerAddress"))
        lbl_customer_delivery_time.text = getTime(intent.getStringExtra("customerDeliveredAt"))
        lbl_customer_distance_value.text = roundFloat(intent.getFloatExtra("customerDistance", -1.0f))

        lbl_fail_reason.text = "none"
        lbl_reward_value.text = roundFloat(intent.getFloatExtra("price", -1.0f))
        lbl_tip_value.text = roundFloat(intent.getFloatExtra("tip", -1.0f))
        if("?" == roundFloat(intent.getFloatExtra("tip", -1.0f)))
        {
            lbl_tip_value.visibility = View.GONE
            lbl_tip_text.visibility = View.GONE
        }

        lbl_total_reward_value.text = getTotalReward(intent.getFloatExtra("price", -1.0f), intent.getFloatExtra("tip", -1.0f))

    }

    fun setStatus()
    {
        when(intent.getIntExtra("status", -1))
        {
            0 -> lbl_status.setTextColor(ContextCompat.getColor(this, R.color.red))
            1 -> lbl_status.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            2 -> lbl_status.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            3 -> lbl_status.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
            4 -> lbl_status.setTextColor(ContextCompat.getColor(this, R.color.lightgreen))
        }
        lbl_status.text = intent.getStringExtra("statusDisplayName")
        lbl_customer_status.text = intent.getStringExtra("statusDisplayName")
    }
    fun setVehicle()
    {
        val vehicle = intent.getIntExtra("vehicle", 0)
        when(vehicle)
        {
            1 -> {
                img_transport.setImageResource(R.drawable.ic_bike_y)
                lbl_reward_type.text = "Fiets levering"
            }
            2 -> {
                img_transport.setImageResource(R.drawable.ic_motor_y)
                lbl_reward_type.text = "Motor/scooter levering"
            }
            3 -> {
                img_transport.setImageResource(R.drawable.ic_motor_y)
                lbl_reward_type.text = "Motor/scooter levering"
            }
            4 -> {
                img_transport.setImageResource(R.drawable.ic_car_y)
                lbl_reward_type.text = "Auto levering"
            }
        }
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
    fun getTotalDistance(warehouse: Float, customer: Float): String
    {
        var output = ""

        var total = roundFloat(warehouse+customer)
        output = "$total km."

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

        if(price == -1.0f)
        {
            output = "?"
        }
        else
        {
            var total = 0.0f
            if(tip == -1.0f)
            {
                total = price
            }
            else
            {
                total = price + tip
            }
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
