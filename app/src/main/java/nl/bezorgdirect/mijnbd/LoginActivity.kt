package nl.bezorgdirect.mijnbd

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import nl.bezorgdirect.mijnbd.Encryption.CipherWrapper
import nl.bezorgdirect.mijnbd.Encryption.KeyStoreWrapper
import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.LoginParams
import nl.bezorgdirect.mijnbd.api.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import nl.bezorgdirect.mijnbd.Delivery.AssignmentActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val keyStoreWrapper = KeyStoreWrapper(this, "mybd")
        val Key = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair("BD_KEY")

        val sharedPref: SharedPreferences = this.getSharedPreferences("mybd", Context.MODE_PRIVATE)

        if(Key != null)
        {
            val encryptedUsr = sharedPref.getString("U", "")
            val encryptedPass = sharedPref.getString("P", "")
            val encryptedToken = sharedPref.getString("T", "")

            if(encryptedUsr != "" && encryptedPass != "" && encryptedToken != "")
            {
                goToApp(this)
            }
        }

        btn_login.setOnClickListener {
            validate(txt_username.text.toString(), txt_password.text.toString(), sharedPref, this)
        }

    }

    private fun validate(username: String = "", password: String = "", sharedPref: SharedPreferences, context: Context)
    {

        if(username == "" || password == "")
        {
            sendToast(resources.getString(R.string.nocreds))
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:7071/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val editpref = sharedPref.edit()
        loadingSpinner.visibility = View.VISIBLE
        service.loginPost(LoginParams(username,password)).enqueue(object : Callback<User> {
            override fun onResponse(
                call: Call<User>,
                response: Response<User>
            ) {
                println(response)
                if (response.code() == 500) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.E500),
                        Toast.LENGTH_LONG
                    ).show()
                    loadingSpinner.visibility = View.GONE
                }
                    else if (response.code() == 401) {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrongcreds),
                        Toast.LENGTH_LONG
                    ).show()
                    loadingSpinner.visibility = View.GONE

                } else if (response.isSuccessful && response.body() != null) {
                    val values = response.body()!!
                    val keyStoreWrapper = KeyStoreWrapper(context, "mybd")
                    keyStoreWrapper.createAndroidKeyStoreAsymmetricKey("BD_KEY")
                    val Key = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair("BD_KEY")

                    val cipherWrapper = CipherWrapper("RSA/ECB/PKCS1Padding")

                    // Encrypt message with the key, using public key
                    val encryptedPass = cipherWrapper.encrypt(password, Key?.public)
                    val encryptedUsername = cipherWrapper.encrypt(username, Key?.public)
                    val encryptedToken = cipherWrapper.encrypt(values.token!!, Key?.public)

                    editpref.putString("U", encryptedUsername)
                    editpref.putString("P", encryptedPass)
                    editpref.putString("T", encryptedToken)
                    editpref.commit()

                    loadingSpinner.visibility = View.GONE
                    goToApp(context)
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("HTTP", "Could not fetch data", t)
                Toast.makeText(
                    context, resources.getString(R.string.E500),
                    Toast.LENGTH_LONG
                ).show()
                loadingSpinner.visibility = View.GONE
                return
            }

        })



    }
    private fun goToApp(context: Context)
    {
        val intent = Intent(context, AssignmentActivity::class.java)
        finish()  //Kill the activity from which you will go to next activity
        startActivity(intent)
    }



    private fun sendToast(message: String)
    {
        Toast.makeText(
            this, message,
            Toast.LENGTH_LONG
        ).show()

    }
}
