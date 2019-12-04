package nl.bezorgdirect.mijnbd.Delivery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import nl.bezorgdirect.mijnbd.R

class DeliveryDeliveringActivity : AppCompatActivity(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_delivering)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap

        val latLngOrigin = LatLng(10.3181466, 123.9029382) // Ayala
        val latLngDestination = LatLng(10.311795,123.915864) // SM City
        this.googleMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Ayala"))
        this.googleMap!!.addMarker(MarkerOptions().position(latLngDestination).title("SM City"))
        this.googleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 14.5f))
    }
}
