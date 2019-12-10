package nl.bezorgdirect.mijnbd.Delivery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.helpers.getApiService


class NoAssignmentFragment : Fragment() {

    val apiService = getApiService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_no_delivery, container, false)
        return view
    }
}
