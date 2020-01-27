package nl.bezorgdirect.mijnbd.delivery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.activity_cancel_assignment.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.UpdateStatusParams
import nl.bezorgdirect.mijnbd.helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CancelAssignmentActivity :  AppCompatActivity() {

    private val apiService = getApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_assignment)

        custom_toolbar_title.text = getString(R.string.title_order_details)
        setSupportActionBar(custom_toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        setListeners()
        setLayout()

        val view = window.decorView.rootView
        hideSpinner(view)
    }

    private fun setLayout(){
        val orderPickedUp = intent.getBooleanExtra("orderpickedup", false)
        if(!orderPickedUp) {
            cb_driver_can_return.visibility = View.GONE
            cb_driver_can_return_text.visibility = View.GONE
        }
    }

    private fun setListeners(){
        btn_cancel_finish.onSlideCompleteListener = object: SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(slider: SlideToActView) {
                val view = window.decorView.rootView
                showSpinner(view)
                updateDelivery()
            }
        }
    }

    private fun updateDelivery(){
        val decryptedToken = getDecryptedToken(this)
        val locationHelper = LocationHelper(this)

        val deliveryId = intent.getStringExtra("deliveryId")
        val currentLatLong = intent.getStringExtra("currentLatLong")
        val warehouseLatLong = intent.getStringExtra("warehouseLatLong")
        val vehicle = intent.getIntExtra("vehicle", 0)

        // Location on time of cancelling assignment:
        locationHelper.getLastLocation { location -> run {
            val updateStatusBody = UpdateStatusParams(0, location.latitude, location.longitude) // status 0 = afgemeld

            apiService.deliverystatusPatch(decryptedToken, deliveryId, updateStatusBody)
                .enqueue(object: Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if(response.isSuccessful) {
                            if(!cb_driver_can_return.isChecked) {
                                val intent = Intent(applicationContext, CancelToWarehouseActivity::class.java)
                                intent.putExtra("deliveryId", deliveryId)
                                intent.putExtra("currentLatLong", currentLatLong)
                                intent.putExtra("warehouseLatLong", warehouseLatLong)
                                intent.putExtra("vehicle", vehicle)
                                startActivity(intent)
                            }
                            else {
                                Toast.makeText(
                                    applicationContext,
                                    resources.getString(R.string.lbl_cancel_no_return),
                                    Toast.LENGTH_LONG
                                ).show()

                                val intent = Intent(applicationContext, AssignmentActivity::class.java)
                                startActivity(intent)//
                            // TODO: navigate to finished screen instead?
                            }
                        }
                        else Log.e("CANCEL_ASSIGNMENT", "Updating delivery status response unsuccessful")
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("CANCEL_ASSIGNMENT", "Updating delivery by delivery by deliveryId failed")
                    }
                })
        } }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
