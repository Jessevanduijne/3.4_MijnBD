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
import nl.bezorgdirect.mijnbd.history.MyBDHistoryDetails
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.helpers.hideSpinner
import nl.bezorgdirect.mijnbd.helpers.replaceFragment
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
        lbl_success_earnings.text = delivery!!.Price!!.toBigDecimal().setScale(2).toString()
        lbl_success_total_earnings_day.text = delivery!!.Price!!.toBigDecimal().setScale(2).toString()
        lbl_success_total_earnings_month.text = delivery!!.Price!!.toBigDecimal().setScale(2).toString()
        lbl_success_total_earnings_week.text = delivery!!.Price!!.toBigDecimal().setScale(2).toString()

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

            //warehouse
            intent.putExtra("warehouseAddress", delivery!!.Warehouse.Address)
            intent.putExtra("warehouseDistance", 0)
            intent.putExtra("warehousePickUp", "")
            //customer
            intent.putExtra("customerAddress", delivery!!.Customer.Address)
            intent.putExtra("customerDistance", 0)
            intent.putExtra("customerDeliveredAt", "")
            //price
            intent.putExtra("price", delivery.Price)
            intent.putExtra("tip", 0)
            //status+vehicle
            intent.putExtra("statusDisplayName", delivery!!.StatusDisplayName)
            intent.putExtra("status", delivery!!.Status)
            intent.putExtra("vehicle", delivery!!.Vehicle)
            startActivity(intent)
        }

        btn_new_assignment.setOnClickListener {
            val fragment = NoAssignmentFragment()
            replaceFragment(R.id.delivery_fragment, fragment)
        }
    }

}