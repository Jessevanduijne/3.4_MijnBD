package nl.bezorgdirect.mijnbd.delivery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_cancel_assignment.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.api.UpdateStatusParams
import nl.bezorgdirect.mijnbd.helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CancelAssignmentFragment(val delivery: Delivery, val orderPickedUp: Boolean) : Fragment() {

    private val apiService = getApiService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cancel_assignment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
        setLayout()
        hideSpinner(view)
        this.activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setLayout(){
        if(!orderPickedUp) {
            cb_driver_can_return.visibility = View.GONE
            cb_driver_can_return_text.visibility = View.GONE
        }
    }

    private fun setListeners(){
        btn_cancel_finish.onSlideCompleteListener = object: SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(slider: SlideToActView) {
                showSpinner(view!!)

                // 1 patch delivery (new notification should be created automatically)
                // 2 go to details page

                updateDelivery()
            }
        }
    }

    private fun updateDelivery(){
        val decryptedToken = getDecryptedToken(this.activity!!)
        val locationHelper = LocationHelper(this.activity!!)

        // Location on time of cancelling assignment:
        locationHelper.getLastLocation { location -> run {
            val updateStatusBody = UpdateStatusParams(0, location.latitude, location.longitude) // status 0 = afgemeld

            apiService.deliverystatusPatch(decryptedToken, delivery.id!!, updateStatusBody)
                .enqueue(object: Callback<Delivery> {
                    override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                        if(response.isSuccessful && response.body() != null) {
                            val updatedAssignment = response.body()!!

                            if(!cb_driver_can_return.isChecked) {
                                val fragment = CancelToWarehouseFragment(updatedAssignment)
                                replaceFragment(R.id.delivery_fragment, fragment)
                            }
                            else {
                                Toast.makeText(
                                    context,
                                    resources.getString(R.string.lbl_cancel_no_return),
                                    Toast.LENGTH_LONG
                                ).show()

                                val fragment = AssignmentFinishedFragment(updatedAssignment)
                                replaceFragment(R.id.delivery_fragment, fragment)
                            }
                        }
                        else Log.e("CANCEL_ASSIGNMENT", "Updating delivery status response unsuccessful")
                    }
                    override fun onFailure(call: Call<Delivery>, t: Throwable) {
                        Log.e("CANCEL_ASSIGNMENT", "Updating delivery by delivery by deliveryId failed")
                    }
                })
        } }
    }
}
