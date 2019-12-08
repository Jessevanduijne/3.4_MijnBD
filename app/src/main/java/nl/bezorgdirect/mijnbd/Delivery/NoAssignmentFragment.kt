package nl.bezorgdirect.mijnbd.Delivery

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_no_delivery.view.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.api.UpdateNotificationParams
import nl.bezorgdirect.mijnbd.api.UpdateStatusParams
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import nl.bezorgdirect.mijnbd.helpers.replaceFragment
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

class NoAssignmentFragment : Fragment() {

    val apiService = getApiService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_no_delivery, container, false)

        view.btn_test_new_activity.setOnClickListener {
            acceptAssignment()
        }
        return view
    }

    private fun acceptAssignment(){
        val decryptedToken = getDecryptedToken(this.activity!!)
        val updateNotificationBody = UpdateNotificationParams(true)
        apiService.notificationPatch(decryptedToken, "6972777C-7838-4486-A555-E70C0197A825", updateNotificationBody) // TODO: Get NotificationID from Notification object
            .enqueue(object: Callback<ResponseBody> {
                override fun onResponse( call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.isSuccessful) {
                        updateDeliveryStatusManually()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(context, "Couldn't accept assignment", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun updateDeliveryStatusManually(){ // The API doesn't update the delivery after accepting notification
        val updateStatusBody = UpdateStatusParams(2, 52.3673779F, 4.9581227F)
        val decryptedToken = getDecryptedToken(this.activity!!)
        apiService.deliverystatusPatch(decryptedToken, "C1ADF3C6-72D1-45C8-80DB-08D77C28D80F", updateStatusBody) // TODO: Get DeliveryID from Notification object
            .enqueue(object: Callback<Delivery> {
                override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                    if(response.isSuccessful) {
                        val assignment = NewAssignmentFragment()
                        replaceFragment(R.id.delivery_fragment, assignment)
                    }
                }

                override fun onFailure(call: Call<Delivery>, t: Throwable) {
                }
            })
    }
}
