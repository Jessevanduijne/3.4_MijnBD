package nl.bezorgdirect.mijnbd.mijnBD

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_my_bdmot.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.UpdateUserParams
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response




class MyBDMot : AppCompatActivity() {

    val apiService = getApiService()

    var email = ""
    var dateofbirth = ""
    var range = 0
    var vehicle = 0
    var vehicledisplayname= ""
    var fare = 0.0f
    var firstname = ""
    var lastname = ""
    var phonenumber= ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bdmot)

        
        custom_toolbar_title.text = getString(R.string.lbl_mot)
        setSupportActionBar(custom_toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        getintentextra()
        setSB()
        setButtons()


        sb_radius.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                update()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                range = progress
                lblradiusvar.text = String.format("%d %s",progress, resources.getString(R.string.lbl_kilometers_short))
            }
        })
    }
    fun setMaxRange()
    {
        var maxRange = 0
        when(vehicle)
        {
            0 -> maxRange = 0
            1 -> maxRange = 15
            2 -> maxRange = 30
            3 -> maxRange = 30
            4 -> maxRange = 60
        }
        lbl_radkmto.text = String.format("%d %s",maxRange, resources.getString(R.string.lbl_kilometers_short))
        sb_radius.max = maxRange
    }
    fun setSB()
    {
        setMaxRange()
        lblradiusvar.text = String.format("%d %s", range, resources.getString(R.string.lbl_kilometers_short))
        sb_radius.progress = range
    }
    fun getintentextra()
    {
        email = intent.getStringExtra("email")
        vehicle = intent.getIntExtra("vehicle", 0)
        vehicledisplayname = intent.getStringExtra("vehicledisplayname")
        dateofbirth = intent.getStringExtra("dateofbirth")
        phonenumber = intent.getStringExtra("phonenumber")
        fare = intent.getFloatExtra("fare", 0.0f)
        range = intent.getIntExtra("range", 0)
        firstname = intent.getStringExtra("firstname")
        lastname = intent.getStringExtra("lastname")
    }

    fun update()
    {
        val decryptedToken = getDecryptedToken(applicationContext)
        val params = UpdateUserParams(email, phonenumber, dateofbirth, range, vehicle, fare,vehicledisplayname, firstname, lastname)
        apiService.delivererPut(decryptedToken, params).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful && response.body() != null) {

                }
                else
                {
                    Toast.makeText(
                        applicationContext, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    applicationContext, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

        })
    }
    fun setButtons()
    {
        when(vehicle)
        {
            1-> {
                btn_bicycle.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gold_btn)
                btn_bicycle.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
            }
            2-> {
                btn_motorcycle.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gold_btn)
                btn_motorcycle.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
            }
            3-> {
                btn_motorcycle.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gold_btn)
                btn_motorcycle.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
            }
            4-> {
                btn_car.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gold_btn)
                btn_car.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
            }
        }

        btn_bicycle.setOnClickListener {
            handleMOTClick(btn_bicycle,btn_car, btn_motorcycle, 1)
        }

        btn_motorcycle.setOnClickListener {
            handleMOTClick(btn_motorcycle,btn_car, btn_bicycle, 3)
        }

        btn_car.setOnClickListener {
            handleMOTClick(btn_car,btn_motorcycle, btn_bicycle, 4)
        }
    }
    fun handleMOTClick(button: Button, otherButton1:Button, otherButton2: Button ,vehicle: Int)
    {
        button.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gold_btn)
        otherButton1.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)
        otherButton2.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)

        button.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
        otherButton1.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        otherButton2.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
        this.vehicle = vehicle
        vehicledisplayname= button.text.toString()
        setMaxRange()
        range = sb_radius.progress
        update()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                val returnIntent = Intent()
                returnIntent.putExtra("vehicle", vehicle)
                returnIntent.putExtra("range", range)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }
}
