package nl.bezorgdirect.mijnbd.mijnBD

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_my_bdinfo.*
import kotlinx.android.synthetic.main.toolbar.*
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
        setContentView(nl.bezorgdirect.mijnbd.R.layout.activity_my_bdinfo)

        custom_toolbar_title.text = getString(nl.bezorgdirect.mijnbd.R.string.lbl_mybdinfo)
        setSupportActionBar(custom_toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        getintentextra()


        val sharedPrefs = this.getSharedPreferences("mybd", Context.MODE_PRIVATE)
        val avataruri = sharedPrefs.getString("avatar", "")
        if(avataruri != "")
        {
            val uri = Uri.parse((avataruri))
            img_profile.setImageURI(uri)
        }

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
                txt_email.background = ContextCompat.getDrawable(applicationContext, nl.bezorgdirect.mijnbd.R.drawable.rounded_gray_section)
                txt_email.setTextColor(ContextCompat.getColor(applicationContext, nl.bezorgdirect.mijnbd.R.color.white))
                email = txt_email.text.toString()
                update()
                btn_edit_email.setImageResource(nl.bezorgdirect.mijnbd.R.drawable.ic_edit_y_24dp)
                txt_email.isEnabled = false
            }
            else
            {
                txt_email.background = ContextCompat.getDrawable(applicationContext,
                    nl.bezorgdirect.mijnbd.R.drawable.rounded_white_input)
                txt_email.setTextColor(ContextCompat.getColor(applicationContext, nl.bezorgdirect.mijnbd.R.color.colorPrimaryDark))
                btn_edit_email.setImageResource(nl.bezorgdirect.mijnbd.R.drawable.ic_save_black_24dp)
                txt_email.isEnabled = true
            }
        }

        btn_edit_phonenumber.setOnClickListener {
            if(txt_phonenumber.isEnabled)
            {
                txt_phonenumber.background = ContextCompat.getDrawable(applicationContext, nl.bezorgdirect.mijnbd.R.drawable.rounded_gray_section)
                txt_phonenumber.setTextColor(ContextCompat.getColor(applicationContext, nl.bezorgdirect.mijnbd.R.color.white))
                phonenumber = txt_phonenumber.text.toString()
                update()
                btn_edit_phonenumber.setImageResource(nl.bezorgdirect.mijnbd.R.drawable.ic_edit_y_24dp)
                txt_phonenumber.isEnabled = false
            }
            else
            {
                txt_phonenumber.background = ContextCompat.getDrawable(applicationContext,
                    nl.bezorgdirect.mijnbd.R.drawable.rounded_white_input)
                txt_phonenumber.setTextColor(ContextCompat.getColor(applicationContext, nl.bezorgdirect.mijnbd.R.color.colorPrimaryDark))
                btn_edit_phonenumber.setImageResource(nl.bezorgdirect.mijnbd.R.drawable.ic_save_black_24dp)
                txt_phonenumber.isEnabled = true
            }
        }

        btn_edit_avatar.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                }
                else{
                    //permission already granted
                    pickImageFromGallery();
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }
    }
    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
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
                        applicationContext, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    applicationContext, resources.getString(nl.bezorgdirect.mijnbd.R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

        })
    }
    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                val returnIntent = Intent()
                returnIntent.putExtra("email", email)
                returnIntent.putExtra("phonenumber", phonenumber)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size >0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            img_profile.setImageURI(data?.data)
            val sharedPref = this.getSharedPreferences("mybd", Context.MODE_PRIVATE)
            val editpref = sharedPref.edit()
            editpref.putString("avatar", data?.data.toString())
            editpref.commit()
        }
    }

}
