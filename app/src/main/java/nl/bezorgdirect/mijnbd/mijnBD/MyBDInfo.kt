package nl.bezorgdirect.mijnbd.mijnBD

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_my_bdinfo.*
import kotlinx.android.synthetic.main.toolbar.*
import nl.bezorgdirect.mijnbd.R
import nl.bezorgdirect.mijnbd.api.UpdateUserParams
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class MyBDInfo : AppCompatActivity() {

    var email = ""
    var dateofbirth = ""
    var range = 0
    var vehicle = 0
    var vehicledisplayname= ""
    var fare = 0.0f
    var phonenumber= ""
    var firstname= ""
    var lastname= ""
    var address=""
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
        if(avataruri != "" && checkStoragePriv())
        {
            val uri = Uri.parse((avataruri))
            try {
                img_profile.setImageURI(uri)
            }
            catch (e: Exception) {
                img_profile!!.setImageResource(R.drawable.ic_logo_y)
            }
        }
        if(img_profile!!.drawable == null)
        {
            img_profile!!.setImageResource(R.drawable.ic_logo_y)
        }

        txt_email.setText(email)
        txt_phonenumber.setText(phonenumber)
        lbl_dateofbirthvar.text = formatDate(dateofbirth)
        lbl_namevar.text = String.format("%s %s", firstname, lastname)
        lbl_addressvar.text = String.format("%s", address)

        setButtons()
    }
    fun checkStoragePriv() :Boolean
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
        return false
    }
    fun formatDate(input: String): String
    {
        try {
            val outputFormat = SimpleDateFormat("dd-MM-yyyy")
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val fullDate: Date = inputFormat.parse(input)
            return outputFormat.format(fullDate)
        }
        catch (e: Exception) {
            return input
        }

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
        address = intent.getStringExtra("address")
    }
    fun handleEditClick(textbox: EditText, button: ImageButton)
    {
        textbox.background = ContextCompat.getDrawable(applicationContext, nl.bezorgdirect.mijnbd.R.drawable.rounded_white_input)
        textbox.setTextColor(ContextCompat.getColor(applicationContext, nl.bezorgdirect.mijnbd.R.color.colorPrimaryDark))
        button.setImageResource(R.drawable.ic_save_black_24dp)
        textbox.isEnabled = true
        textbox.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(textbox, InputMethodManager.SHOW_IMPLICIT)
        textbox.setSelection(textbox.text.length)
    }
    fun handleSaveClick(textbox: EditText, button: ImageButton, type: String)
    {
        var valid = true
        if(type == "email")
        {
            valid = isEmailValid(textbox.text.toString())
        }
        else if(type == "phonenumber")
        {
            valid = isPhonenumberValid(textbox.text.toString())
        }
        if(valid) {
            textbox.background = ContextCompat.getDrawable(applicationContext, nl.bezorgdirect.mijnbd.R.drawable.rounded_gray_section)
            textbox.setTextColor(ContextCompat.getColor(applicationContext, nl.bezorgdirect.mijnbd.R.color.white))
            if(type == "email")
            {
                email = textbox.text.toString()
            }
            else if(type == "phonenumber")
            {
                phonenumber = textbox.text.toString()
            }
            update()
            button.setImageResource(nl.bezorgdirect.mijnbd.R.drawable.ic_edit_y_24dp)
            textbox.isEnabled = false
        }
        else
        {
            if(type == "email") {
                Toast.makeText(
                    applicationContext,
                    resources.getString(nl.bezorgdirect.mijnbd.R.string.wrong_email),
                    Toast.LENGTH_LONG
                ).show()
            }
            else if(type == "phonenumber")
            {
                Toast.makeText(
                    applicationContext,
                    resources.getString(nl.bezorgdirect.mijnbd.R.string.wrong_phone),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    fun setButtons()
    {
        btn_edit_email.setOnClickListener {
            if(txt_email.isEnabled)
            {
                handleSaveClick(txt_email, btn_edit_email, "email")
            }
            else
            {
                handleEditClick(txt_email, btn_edit_email)
            }
        }

        btn_edit_phonenumber.setOnClickListener {
            if(txt_phonenumber.isEnabled)
            {
                handleSaveClick(txt_phonenumber, btn_edit_phonenumber, "phonenumber")
            }
            else
            {
                handleEditClick(txt_phonenumber, btn_edit_phonenumber)
            }
        }

        btn_edit_avatar.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else{
                    //permission already granted
                    pickImageFromGallery()
                }
            }
            else{
                //system OS is < Marshmallow
                pickImageFromGallery()
            }
        }
    }
    companion object {
        //image pick code
        private const val IMAGE_PICK_CODE = 1000
        //Permission code
        private const val PERMISSION_CODE = 1001
    }
    fun update()
    {
        val decryptedToken = getDecryptedToken(applicationContext)
        val params = UpdateUserParams(email, phonenumber, dateofbirth, range, vehicle, fare, vehicledisplayname, firstname, lastname)
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

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    fun isPhonenumberValid(phone: String): Boolean {

        val pattern = Regex("^((\\+|00(\\s|\\s?\\-\\s?)?)31(\\s|\\s?\\-\\s?)?(\\(0\\)[\\-\\s]?)?|0)[1-9]((\\s|\\s?\\-\\s?)?[0-9])((\\s|\\s?-\\s?)?[0-9])((\\s|\\s?-\\s?)?[0-9])\\s?[0-9]\\s?[0-9]\\s?[0-9]\\s?[0-9]\\s?[0-9]\$")
        return pattern.matches(phone)
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
                    Toast.makeText(this, resources.getString(nl.bezorgdirect.mijnbd.R.string.no_permission), Toast.LENGTH_SHORT).show()
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
