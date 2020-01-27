package nl.bezorgdirect.mijnbd.helpers

import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
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

fun showContent(view: View) {
    val content: FrameLayout = view.findViewById(R.id.content)
    val loadingOverlay: RelativeLayout = view.findViewById(R.id.loading_overlay)
    content.visibility = View.VISIBLE
    loadingOverlay.visibility = View.GONE
}
fun showLoadingOverlay(view: View) {
    val content: FrameLayout = view.findViewById(R.id.content)
    val loadingOverlay: RelativeLayout = view.findViewById(R.id.loading_overlay)
    loadingOverlay.visibility = View.VISIBLE
    content.visibility = View.GONE
}