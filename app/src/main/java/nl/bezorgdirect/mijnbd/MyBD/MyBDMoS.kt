package nl.bezorgdirect.mijnbd.MyBD

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_my_bdmo_s.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.UpdateUserParams
import nl.bezorgdirect.mijnbd.api.User
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyBDMoS : AppCompatActivity() {

    val apiService = getApiService()

    var email = ""
    var dateofbirth = ""
    var range = 0
    var vehicle = 0
    var vehicledisplayname= ""
    var fare = 0.0f
    var total = 0.0f
    var phonenumber= ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bdmo_s)

        
        custom_toolbar_title.text = getString(R.string.lbl_mos)
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
                lblradiusvar.text = "$progress km."
            }
        })
    }
    fun setMaxRange()
    {
        var maxRange = 0
        when(vehicle)
        {
            -1-> sb_radius.max = 0
            1 -> maxRange = 15
            2 -> maxRange = 30
            3 -> maxRange = 30
            4 -> maxRange = 60
        }
        lbl_radkm2.text = "$maxRange km."
        sb_radius.max = maxRange
    }
    fun setSB()
    {
        setMaxRange()
        lblradiusvar.text = "$range km."
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
        total = intent.getFloatExtra("totalearnings",0.0f)
        range = intent.getIntExtra("range", 0)
    }

    fun update()
    {
        val decryptedToken = getDecryptedToken(applicationContext)
        val params = UpdateUserParams(email, phonenumber, dateofbirth, range, vehicle, vehicledisplayname, fare, total)
        apiService.delivererPut(decryptedToken, params).enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                if (response.isSuccessful && response.body() != null) {

                }
                else
                {
                    Toast.makeText(
                        applicationContext, resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    applicationContext, resources.getString(R.string.E500),
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
            btn_bicycle.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gold_btn)
            btn_motorcycle.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)
            btn_car.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)

            btn_bicycle.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
            btn_motorcycle.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
            btn_car.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))

            vehicle = 1
            setMaxRange()
            range = sb_radius.progress
            update()
        }

        btn_motorcycle.setOnClickListener {
            btn_bicycle.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)
            btn_motorcycle.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gold_btn)
            btn_car.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)

            btn_bicycle.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
            btn_motorcycle.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
            btn_car.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))

            vehicle = 3
            setMaxRange()
            range = sb_radius.progress
            update()
        }

        btn_car.setOnClickListener {
            btn_bicycle.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)
            btn_motorcycle.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)
            btn_car.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gold_btn)

            btn_bicycle.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
            btn_motorcycle.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
            btn_car.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))

            vehicle = 4
            setMaxRange()
            range = sb_radius.progress
            update()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
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
