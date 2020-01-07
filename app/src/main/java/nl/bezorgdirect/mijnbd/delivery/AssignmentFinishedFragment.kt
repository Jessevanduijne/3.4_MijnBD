package nl.bezorgdirect.mijnbd.delivery

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.fragment_assignment_finished.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.helpers.hideSpinner
import nl.bezorgdirect.mijnbd.helpers.replaceFragment
import nl.bezorgdirect.mijnbd.history.MyBDHistoryDetails
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AssignmentFinishedFragment(val delivery: Delivery? = null): Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.activity?.bottom_navigation?.visibility = View.VISIBLE
        return inflater.inflate(R.layout.fragment_assignment_finished, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setLayout()
        hideSpinner(view)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    fun setLayout(){

        val d = delivery!!

        if(d.Status != 4) {  // 4 = Afgeleverd
            lbl_status_text.text = getString(R.string.lbl_delivery_cancelled)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                lbl_status_text.setTextColor(activity!!.resources.getColor(R.color.light_red, activity!!.theme))
            }
        }

        // Todo: Edit if cloud group makes api call possible:
        lbl_success_earnings.text = d.Price!!.toBigDecimal().setScale(2).toString()
        lbl_success_total_earnings_day.text = d.Price.toBigDecimal().setScale(2).toString()
        lbl_success_total_earnings_month.text = d.Price!!.toBigDecimal().setScale(2).toString()
        lbl_success_total_earnings_week.text = d.Price!!.toBigDecimal().setScale(2).toString()

        val formattedTime: String
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDateTime = LocalDateTime.parse(LocalDateTime.now().toString())
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            formattedTime = formatter.format(localDateTime)
        }
        else {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val formatter = SimpleDateFormat("HH:mm")
            formattedTime = formatter.format(parser.parse(delivery.DueDate!!)!!)
        }

        lbl_successful_datetime.text = formattedTime
    }

    fun setOnClickListeners(){
        btn_check_details.setOnClickListener {
            val intent = Intent(activity, MyBDHistoryDetails::class.java)


            val d = delivery!!

            //warehouse
            intent.putExtra("warehouseAddress", d.Warehouse.Address)
            intent.putExtra("warehouseDistance", d.WarehouseDistanceInKilometers)
            intent.putExtra("warehousePickUp", d.WarehousePickUpAt)
            //customer
            intent.putExtra("customerAddress", d.Customer.Address)
            intent.putExtra("customerDistance", d.CustomerDistanceInKilometers)
            intent.putExtra("customerDeliveredAt", d.DeliveredAt)
            //price
            intent.putExtra("price", d.Price)
            intent.putExtra("tip", 0)
            //status+vehicle
            intent.putExtra("statusDisplayName", d.StatusDisplayName)
            intent.putExtra("status", d.Status)
            intent.putExtra("vehicle", d.Vehicle)
            startActivity(intent)
        }

        btn_new_assignment.setOnClickListener {
            val fragment = NoAssignmentFragment()
            replaceFragment(R.id.delivery_fragment, fragment)
        }
    }

}