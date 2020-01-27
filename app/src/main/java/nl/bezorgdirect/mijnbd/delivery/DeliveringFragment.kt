package nl.bezorgdirect.mijnbd.delivery

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.fragment_delivering.*
import nl.bezorgdirect.mijnbd.MijnbdApplication.Companion.canReceiveNotification
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.api.GoogleDirections
import nl.bezorgdirect.mijnbd.api.UpdateStatusParams
import nl.bezorgdirect.mijnbd.helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class DeliveringFragment(val delivery: Delivery? = null): Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val apiService = getApiService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.activity?.bottom_navigation?.visibility = View.GONE
        return inflater.inflate(nl.bezorgdirect.mijnbd.R.layout.fragment_delivering, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
        setListeners()
        hideSpinner(view)
    }

    private fun setListeners(){
        btn_delivering_completed.onSlideCompleteListener = object: SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(slider: SlideToActView) {
                canReceiveNotification = true
                showSpinner(view!!)
                updateDeliveryStatus()
            }
        }

        btn_cancel_delivery.setOnClickListener {
            val fragment = CancelAssignmentFragment(delivery!!, true)
            replaceFragment(nl.bezorgdirect.mijnbd.R.id.content, fragment)
        }

        btn_call_warehouse.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${getString(nl.bezorgdirect.mijnbd.R.string.warehouse_phone_number)}")
            startActivity(intent)
        }
    }

    private fun setLayout(){
        lbl_delivering_address.text = delivery!!.customer.address!!.substringBefore(',') // cut zip code off
        lbl_delivering_zip.text = (delivery!!.customer.postalCode + " " + delivery!!.warehouse.place)
        btn_delivering_completed.text = getString(nl.bezorgdirect.mijnbd.R.string.lbl_delivery_delivered)
        lbl_assignment.text = getString(nl.bezorgdirect.mijnbd.R.string.lbl_assignment_client)
        img_delivering_destination.setImageResource(nl.bezorgdirect.mijnbd.R.drawable.ic_house_w)

        val mapFragment = childFragmentManager.findFragmentById(nl.bezorgdirect.mijnbd.R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getRoute()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        val latLngOrigin = LatLng(delivery!!.warehouse.latitude!!.toDouble(), delivery!!.warehouse.longitude!!.toDouble())
        val latLngDestination = LatLng(delivery!!.customer.latitude!!.toDouble(), delivery!!.customer.longitude!!.toDouble())
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Current"))
        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title(delivery!!.customer.address))
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 12.5f))
    }

    private fun getRoute(){
        val service = getGoogleService()
        val path: MutableList<List<LatLng>> = ArrayList()

        val startLatLong = delivery!!.warehouse.latitude.toString() + "," + delivery!!.warehouse.longitude.toString()
        val endLatLong = delivery!!.customer.latitude.toString() + "," + delivery!!.customer.longitude.toString()

        var travelmode = ""
        when(delivery!!.vehicle)
        {
            1 -> travelmode = "cycling" // bike
            2 -> travelmode = "cycling" // scooter
            3 -> travelmode = "driving" // motor
            4 -> travelmode = "driving" // car
        }

        val apiKey = getString(nl.bezorgdirect.mijnbd.R.string.google_maps_key)
        val call = service.getDirections(startLatLong, endLatLong, apiKey, travelmode = travelmode)
        call.enqueue(object: Callback<GoogleDirections> {

            override fun onResponse(call: Call<GoogleDirections>, response: Response<GoogleDirections>) {
                if(response.isSuccessful && response.body() != null) {
                    val result = response.body()

                    val routes = result?.routes
                    val legs = routes?.get(0)?.legs
                    val steps = legs?.get(0)?.steps
                    for(i in 0 until steps!!.count()) {
                        val polyline = steps[i].polyline
                        val points = polyline.points
                        path.add(PolyUtil.decode(points))
                    }
                    for(i in 0 until path.size) {
                        googleMap!!.addPolyline(PolylineOptions().addAll(path[i]).color(Color.RED))
                    }
                }
                else {
                    Log.e("HTTP", "Google directions call unsuccessful")
                }
            }
            override fun onFailure(call: Call<GoogleDirections>, t: Throwable) {
                Log.e("HTTP", "Google directions call failed")
            }
        })
    }

    private fun updateDeliveryStatus(){
        val decryptedToken = getDecryptedToken(this.activity!!)
        val updateStatusBody = UpdateStatusParams(4, delivery!!.customer.latitude!!, delivery!!.customer.longitude!!) // status 4 = afgeleverd

        apiService.deliverystatusPatch(decryptedToken, delivery.id, updateStatusBody)
            .enqueue(object: Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if(response.isSuccessful) {

                        val fragment = AssignmentFinishedFragment(delivery.id)
                        replaceFragment(nl.bezorgdirect.mijnbd.R.id.content, fragment)
                    }
                    else Log.e("DELIVERING", "Updating delivery status response unsuccessful")
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("DELIVERING", "Updating delivery by delivery by deliveryId failed")
                }
            })
    }


}
