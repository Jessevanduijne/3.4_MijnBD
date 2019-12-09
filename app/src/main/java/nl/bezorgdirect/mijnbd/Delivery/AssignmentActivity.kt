package nl.bezorgdirect.mijnbd.Delivery

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_assignment.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R.*
import nl.bezorgdirect.mijnbd.R


class AssignmentActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_assignment)

        custom_toolbar_title.setText(getString(string.title_assignment))
        setSupportActionBar(custom_toolbar)

        val noAssignmentFragment = NoAssignmentFragment()
        supportFragmentManager.beginTransaction().replace(id.delivery_fragment, noAssignmentFragment).commit()
    }

}
