package nl.bezorgdirect.mijnbd.delivery

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.api.GoogleDirections
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import nl.bezorgdirect.mijnbd.MijnbdApplication.Companion.canReceiveNotification
import nl.bezorgdirect.mijnbd.api.UpdateStatusParams
import nl.bezorgdirect.mijnbd.helpers.*

class ToClientFragment(val delivery: Delivery? = null): Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val apiService = getApiService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.activity?.bottom_navigation?.visibility = View.GONE
        return inflater.inflate(R.layout.fragment_delivering, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
        setOnSlideListener()
        hideSpinner(view)
    }

    private fun setOnSlideListener(){
        btn_delivering_completed.onSlideCompleteListener = object: SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(slider: SlideToActView) {
                canReceiveNotification = true
                showSpinner(view!!)
                updateDeliveryStatus()
            }
        }
    }

    private fun setLayout(){
        lbl_delivering_address.text = delivery!!.Customer.Address!!.substringBefore(',') // cut zip code off
        lbl_delivering_zip.text = (delivery!!.Customer.PostalCode + " " + delivery!!.Warehouse.Place)
        btn_delivering_completed.text = getString(R.string.lbl_delivery_delivered)
        lbl_assignment.text = getString(R.string.lbl_assignment_client)
        img_delivering_destination.setImageResource(R.drawable.ic_house_w)

        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getRoute()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        val latLngOrigin = LatLng(delivery!!.Warehouse.Latitude!!.toDouble(), delivery!!.Warehouse.Longitude!!.toDouble())
        val latLngDestination = LatLng(delivery!!.Customer.Latitude!!.toDouble(), delivery!!.Customer.Longitude!!.toDouble())
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Current"))
        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title(delivery!!.Customer.Address))
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 16.5f))
    }

    private fun getRoute(){
        val service = getGoogleService()
        val path: MutableList<List<LatLng>> = ArrayList()

        val startLatLong = delivery!!.Warehouse.Latitude.toString() + "," + delivery!!.Warehouse.Longitude.toString()
        val endLatLong = delivery!!.Customer.Latitude.toString() + "," + delivery!!.Customer.Longitude.toString()

        var travelmode = ""
        when(delivery!!.Vehicle)
        {
            1 -> travelmode = "cycling" // bike
            2 -> travelmode = "cycling" // scooter
            3 -> travelmode = "driving" // motor
            4 -> travelmode = "driving" // car
        }

        val apiKey = getString(R.string.google_maps_key)
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
        val updateStatusBody = UpdateStatusParams(4, delivery!!.Customer.Latitude!!, delivery!!.Customer.Longitude!!) // status 4 = afgeleverd

        apiService.deliverystatusPatch(decryptedToken, delivery!!.Id!!, updateStatusBody)
            .enqueue(object: Callback<Delivery> {
                override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                    if(response.isSuccessful && response.body() != null) {
                        val updatedAssignment = response.body()!!

                        val fragment = AssignmentFinishedFragment(updatedAssignment)
                        replaceFragment(R.id.delivery_fragment, fragment)
                    }
                    else Log.e("DELIVERING", "Updating delivery status response unsuccessful")
                }
                override fun onFailure(call: Call<Delivery>, t: Throwable) {
                    Log.e("DELIVERING", "Updating delivery by delivery by deliveryId failed")
                }
            })
    }
}
