package nl.bezorgdirect.mijnbd.Delivery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_delivery_waiting.*
import kotlinx.android.synthetic.main.fragment_no_delivery.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R.*


class AssignmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_delivery_waiting)

        custom_toolbar_title.setText(getString(string.title_assignment))
        setSupportActionBar(custom_toolbar)

//        val noAssignmentFragment = NoAssignmentFragment()
//        supportFragmentManager.beginTransaction().replace(R.id.delivery_fragment, noAssignmentFragment).commit()
//
        val fragment = NewAssignmentFragment()
        btn_test_new_activity.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(id.delivery_fragment, fragment).commit()
        }
    }
}
