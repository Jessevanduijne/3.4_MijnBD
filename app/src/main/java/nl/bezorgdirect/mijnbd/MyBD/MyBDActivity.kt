package nl.bezorgdirect.mijnbd.MyBD

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_my_bd.*
import nl.bezorgdirect.mijnbd.R

class MyBDActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bd)

        btn_info.setOnClickListener{
            val intent : Intent = Intent(this@MyBDActivity, MyBDInfo::class.java)
            startActivity(intent)
        }

        btn_availability.setOnClickListener {
            val intent : Intent = Intent(this@MyBDActivity, MyBDAvailability::class.java)
            startActivity(intent)
        }

        btn_meansoftransport.setOnClickListener {
            val intent : Intent = Intent(this@MyBDActivity, MyBDMoS::class.java)
            startActivity(intent)
        }
    }


}
