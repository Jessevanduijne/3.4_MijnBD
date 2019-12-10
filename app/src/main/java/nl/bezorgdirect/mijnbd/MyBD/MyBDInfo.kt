package nl.bezorgdirect.mijnbd.MyBD

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_my_bdinfo.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.UpdateUserParams
import nl.bezorgdirect.mijnbd.api.User
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyBDInfo : AppCompatActivity() {

    var email = ""
    var dateofbirth = ""
    var range = 0
    var vehicle = 0
    var vehicledisplayname= ""
    var fare = 0.0f
    var total = 0.0f
    var phonenumber= ""
    val apiService = getApiService()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bdinfo)

        custom_toolbar_title.text = getString(R.string.lbl_mybdinfo)
        setSupportActionBar(custom_toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        getintentextra()

        txt_email.setText(email)
        txt_phonenumber.setText(phonenumber)

        setButtons()
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

    fun setButtons()
    {
        btn_edit_email.setOnClickListener {
            if(txt_email.isEnabled)
            {
                txt_email.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)
                txt_email.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                email = txt_email.text.toString()
                update()
                btn_edit_email.setImageResource(R.drawable.ic_edit_y_24dp)
                txt_email.isEnabled = false
            }
            else
            {
                txt_email.background = ContextCompat.getDrawable(applicationContext,R.drawable.rounded_white_input)
                txt_email.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
                btn_edit_email.setImageResource(R.drawable.ic_save_black_24dp)
                txt_email.isEnabled = true
            }
        }

        btn_edit_phonenumber.setOnClickListener {
            if(txt_phonenumber.isEnabled)
            {
                txt_phonenumber.background = ContextCompat.getDrawable(applicationContext, R.drawable.rounded_gray_section)
                txt_phonenumber.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                phonenumber = txt_phonenumber.text.toString()
                update()
                btn_edit_phonenumber.setImageResource(R.drawable.ic_edit_y_24dp)
                txt_phonenumber.isEnabled = false
            }
            else
            {
                txt_phonenumber.background = ContextCompat.getDrawable(applicationContext,R.drawable.rounded_white_input)
                txt_phonenumber.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark))
                btn_edit_phonenumber.setImageResource(R.drawable.ic_save_black_24dp)
                txt_phonenumber.isEnabled = true
            }
        }
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
