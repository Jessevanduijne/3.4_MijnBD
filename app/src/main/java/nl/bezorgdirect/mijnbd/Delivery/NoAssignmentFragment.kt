package nl.bezorgdirect.mijnbd.Delivery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_no_delivery.view.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.helpers.replaceFragment

class NoAssignmentFragment : Fragment() {

    companion object {
        fun newInstance() = NoAssignmentFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_no_delivery, container, false)

        view.btn_test_new_activity.setOnClickListener {
             val testAssignment = NewAssignmentFragment()
            replaceFragment(R.id.delivery_fragment, testAssignment)
        }
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
}
