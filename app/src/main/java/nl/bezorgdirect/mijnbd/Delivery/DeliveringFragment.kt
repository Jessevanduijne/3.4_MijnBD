package nl.bezorgdirect.mijnbd.Delivery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import nl.bezorgdirect.mijnbd.R

class DeliveringFragment : Fragment() {

    companion object {
        fun newInstance() = DeliveringFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_delivering, container, false)
    }
}