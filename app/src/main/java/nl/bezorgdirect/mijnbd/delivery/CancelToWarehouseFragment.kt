package nl.bezorgdirect.mijnbd.delivery

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_cancel_to_warehouse.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.Delivery
import nl.bezorgdirect.mijnbd.api.GoogleDirections
import nl.bezorgdirect.mijnbd.helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CancelToWarehouseFragment(val delivery: Delivery) : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val apiService = getApiService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cancel_to_warehouse, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
        hideSpinner(view)
        this.activity?.actionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //getRoute()
    }

    private fun setListeners() {
        btn_cancel_finish_finish.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(slider: SlideToActView) {
                    showSpinner(view!!)
                    val fragment = AssignmentFinishedFragment(delivery)
                    replaceFragment(R.id.delivery_fragment, fragment)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
//        val latLngOrigin = LatLng(delivery!!.Current.Latitude!!, delivery!!.Current.Longitude!!)
//        val latLngDestination = LatLng(delivery!!.Warehouse.Latitude!!.toDouble(), delivery!!.Warehouse.Longitude!!.toDouble())
//        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Your position"))
//        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title(delivery!!.Warehouse.Address))
//        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 16.5f))
    }

    private fun getRoute(){
        val service = getGoogleService()
        val path: MutableList<List<LatLng>> = ArrayList()

        val startLatLong = delivery!!.current.latitude.toString() + "," + delivery!!.current.longitude.toString()
        val endLatLong = delivery!!.warehouse.latitude.toString() + "," + delivery!!.warehouse.longitude.toString()

        var travelmode = ""
        when(delivery!!.vehicle)
        {
            1 or 2 -> travelmode = "cycling" // bike / scooter
            3 or 4 -> travelmode = "driving" // motor / car
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
}
