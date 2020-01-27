package nl.bezorgdirect.mijnbd.delivery

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.activity_cancel_to_warehouse.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.GoogleDirections
import nl.bezorgdirect.mijnbd.helpers.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CancelToWarehouseActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private val apiService = getApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_to_warehouse)

        custom_toolbar_title.text = getString(R.string.lbl_cancel)
        setSupportActionBar(custom_toolbar)

        setListeners()
        val view = window.decorView.rootView
        hideSpinner(view)

        val mapFragment = fragment_map as SupportMapFragment
        mapFragment.getMapAsync(this)
        getRoute()
    }

    private fun setListeners() {
        val deliveryId = intent.getStringExtra("deliveryId")
        btn_cancel_finish_finish.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(slider: SlideToActView) {
                    val view = window.decorView.rootView
                    showSpinner(view)
                    val intent = Intent(applicationContext, AssignmentActivity::class.java)
                    startActivity(intent)
                }
            }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val currentLatLong = intent.getStringExtra("currentLatLong")
        val currentLatLongArray = currentLatLong.split(",")

        val warehouseLatLong = intent.getStringExtra("warehouseLatLong")
        val warehouseLatLongArray = warehouseLatLong.split(",")

        this.googleMap = googleMap
        val latLngOrigin = LatLng(currentLatLongArray[0].toDouble(), currentLatLongArray[1].toDouble())
        val latLngDestination = LatLng(warehouseLatLongArray[0].toDouble(), warehouseLatLongArray[1].toDouble())
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title(getString(R.string.lbl_your_position)))
        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title(getString(R.string.lbl_destination)))
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 16.5f))
    }

    private fun getRoute(){

        val currentLatLong = intent.getStringExtra("currentLatLong")
        val warehouseLatLong = intent.getStringExtra("warehouseLatLong")
        val vehicle = intent.getIntExtra("vehicle", 0)

        val service = getGoogleService()
        val path: MutableList<List<LatLng>> = ArrayList()

        var travelmode = ""
        when(vehicle)
        {
            1 or 2 -> travelmode = "cycling" // bike / scooter
            3 or 4 -> travelmode = "driving" // motor / car
        }

        val apiKey = getString(R.string.google_maps_key)
        val call = service.getDirections(currentLatLong, warehouseLatLong, apiKey, travelmode = travelmode)
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
