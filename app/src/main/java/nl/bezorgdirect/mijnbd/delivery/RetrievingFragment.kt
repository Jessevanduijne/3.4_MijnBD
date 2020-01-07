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
import nl.bezorgdirect.mijnbd.api.UpdateStatusParams
import nl.bezorgdirect.mijnbd.helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import android.net.Uri


class RetrievingFragment(val delivery: Delivery? = null): Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val apiService = getApiService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.activity?.bottom_navigation?.visibility = View.GONE
        return inflater.inflate(R.layout.fragment_delivering, container, false)
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
                showSpinner(view!!)
                updateDeliveryStatus()
            }
        }

        btn_call_warehouse.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${getString(R.string.warehouse_phone_number)}")
            startActivity(intent)
        }

        btn_cancel_delivery.setOnClickListener {
            val fragment = CancelAssignmentFragment(delivery!!, false)
            replaceFragment(R.id.delivery_fragment, fragment)
        }
    }

    private fun setLayout(){
        lbl_delivering_address.text = delivery!!.Warehouse.Address!!.substringBefore(',') // cut zip code off
        lbl_delivering_zip.text = (delivery!!.Warehouse.PostalCode + " " + delivery!!.Warehouse.Place)

        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getRoute()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        val latLngOrigin = LatLng(delivery!!.Current.Latitude!!, delivery!!.Current.Longitude!!)
        val latLngDestination = LatLng(delivery!!.Warehouse.Latitude!!.toDouble(), delivery!!.Warehouse.Longitude!!.toDouble())
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Your position"))
        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title(delivery!!.Warehouse.Address))
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 16.5f))
    }

    private fun getRoute(){
        val service = getGoogleService()
        val path: MutableList<List<LatLng>> = ArrayList()

        val startLatLong = delivery!!.Current.Latitude.toString() + "," + delivery!!.Current.Longitude.toString()
        val endLatLong = delivery!!.Warehouse.Latitude.toString() + "," + delivery!!.Warehouse.Longitude.toString()

        var travelmode = ""
        when(delivery!!.Vehicle)
        {
            1 or 2 -> travelmode = "cycling" // bike / scooter
            3 or 4 -> travelmode = "driving" // motor / car
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
        val updateStatusBody = UpdateStatusParams(3, delivery!!.Warehouse.Latitude!!, delivery!!.Warehouse.Longitude!!) // status 3 = onderweg

        apiService.deliverystatusPatch(decryptedToken, delivery!!.Id!!, updateStatusBody)
            .enqueue(object: Callback<Delivery> {
                override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                    if(response.isSuccessful && response.body() != null) {
                        val updatedAssignment = response.body()!!
                        val fragment = DeliveringFragment(updatedAssignment)
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
