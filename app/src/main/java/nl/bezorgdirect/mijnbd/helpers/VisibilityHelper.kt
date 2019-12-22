package nl.bezorgdirect.mijnbd.helpers

import android.view.View
import android.widget.ProgressBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import nl.bezorgdirect.mijnbd.R

fun showSpinner(view: View){
    val loadingSpinner: ProgressBar = view.findViewById(R.id.loadingSpinner)
    loadingSpinner.bringToFront()
    loadingSpinner.visibility = View.VISIBLE
}

fun hideSpinner(view: View) {
    val loadingSpinner: ProgressBar = view.findViewById(R.id.loadingSpinner)
    loadingSpinner.visibility = View.GONE
}