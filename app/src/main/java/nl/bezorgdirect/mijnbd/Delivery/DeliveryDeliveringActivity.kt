package nl.bezorgdirect.mijnbd.Delivery

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.GoogleDirections
import nl.bezorgdirect.mijnbd.api.GoogleService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DeliveryDeliveringActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_delivering)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment

        mapFragment.getMapAsync(this)
        getRoute()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        val latLngOrigin = LatLng(52.3673469, 4.958289)
        val latLngDestination = LatLng(52.3647484, 4.941496)
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Thuis"))
        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title("Coffeeshop"))
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))
    }

    private fun getRoute(){
        val service = getGoogleService()
        val path: MutableList<List<LatLng>> = ArrayList()

        val startLatLong = "52.3673469,4.958289"
        val endLatLong = "52.3647484,4.941496"
        val travelmode = "cycling"

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

    private fun getGoogleService(): GoogleService {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GoogleService::class.java)
        return service
    }
}
