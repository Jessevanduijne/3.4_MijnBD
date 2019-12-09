package nl.bezorgdirect.mijnbd.Delivery

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.bottom_bar.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.History.MyBDHistory
import nl.bezorgdirect.mijnbd.R.*


class AssignmentActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_assignment)

        custom_toolbar_title.setText(getString(string.title_assignment))
        setSupportActionBar(custom_toolbar)

        bottom_navigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.getItemId()) {
                    nl.bezorgdirect.mijnbd.R.id.action_history -> {
                        //todo make history fragment
                        //replaceFragment(R.id.delivery_fragment, assignment)
                        val intent = Intent(this@AssignmentActivity, MyBDHistory::class.java)
                        startActivity(intent)
                    }
                    nl.bezorgdirect.mijnbd.R.id.action_deliveries -> Toast.makeText(
                        this@AssignmentActivity,
                        "deliveries",
                        Toast.LENGTH_SHORT
                    ).show()
                    nl.bezorgdirect.mijnbd.R.id.action_mybd -> Toast.makeText(
                        this@AssignmentActivity,
                        "mybd",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return true
            }
        })

        val noAssignmentFragment = NoAssignmentFragment()
        supportFragmentManager.beginTransaction().replace(id.delivery_fragment, noAssignmentFragment).commit()
    }

}
