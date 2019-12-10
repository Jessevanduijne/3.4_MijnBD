package nl.bezorgdirect.mijnbd.MyBD

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import nl.bezorgdirect.mijnbd.R

class MyBDActivity : Fragment() {

    companion object {
        fun newInstance() = MyBDActivity()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.activity_my_bd, container, false)

        if(activity != null) {
            val custom_toolbar_title: TextView = activity!!.findViewById(R.id.custom_toolbar_title)
            custom_toolbar_title.text = getString(R.string.lbl_mybdpersonalia)
        }
        val btn_info: Button = root.findViewById(R.id.btn_info)
        val btn_availability: Button = root.findViewById(R.id.btn_availability)
        val btn_meansoftransport: Button = root.findViewById(R.id.btn_meansoftransport)


        btn_info.setOnClickListener{
            val intent : Intent = Intent(root.context, MyBDInfo::class.java)
            startActivity(intent)
        }

        btn_availability.setOnClickListener {
            val intent : Intent = Intent(root.context, MyBDAvailability::class.java)
            startActivity(intent)
        }

        btn_meansoftransport.setOnClickListener {
            val intent : Intent = Intent(root.context, MyBDMoS::class.java)
            startActivity(intent)
        }
        return root
    }



}
