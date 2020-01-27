package nl.bezorgdirect.mijnbd

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import nl.bezorgdirect.mijnbd.api.LoginParams
import nl.bezorgdirect.mijnbd.delivery.AssignmentActivity
import nl.bezorgdirect.mijnbd.encryption.CipherWrapper
import nl.bezorgdirect.mijnbd.encryption.KeyStoreWrapper
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.services.NotificationService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        println("hoi")
        setTheme(R.style.AppThemeNoBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        checkLoginNeeded()

        btn_login.setOnClickListener {
            validate(txt_username.text.toString(), txt_password.text.toString(), this)
        }

    }
    private fun checkLoginNeeded()
    {
        val keyStoreWrapper = KeyStoreWrapper(this, "mybd")
        val Key = keyStoreWrapper.getAndroidKeyStoreAsymmetricKeyPair("BD_KEY")

        val sharedPref: SharedPreferences = this.getSharedPreferences("mybd", Context.MODE_PRIVATE)

        if(Key != null)
        {
            val encryptedToken = sharedPref.getString("T", "")

            if(encryptedToken != "")
            {
                goToApp(this)
            }
        }
    }

    private fun validate(username: String = "", password: String = "", context: Context)
    {
        val sharedPref: SharedPreferences = this.getSharedPreferences("mybd", Context.MODE_PRIVATE)
        if(username == "" || password == "")
        {
            sendToast(resources.getString(R.string.nocreds))
            return
        }

        val service = getApiService()

        val editpref = sharedPref.edit()
        loadingSpinner.visibility = View.VISIBLE
        service.loginPost(LoginParams(username,password)).enqueue(object : Callback<String> {
            override fun onResponse(
                call: Call<String>,
                response: Response<String>
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
                    else if (response.code() == 400) {
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
                    println(values)
                    // Encrypt message with the key, using public key
                    val encryptedToken = cipherWrapper.encrypt(values.toString(), Key?.public)
                    editpref.putString("T", encryptedToken)
                    editpref.commit()

                    loadingSpinner.visibility = View.GONE
                    goToApp(context)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
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
        startNotificationService()
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

    private fun startNotificationService(){
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val notificationServiceClass = NotificationService::class.java
        val notificationIntent = Intent(applicationContext, notificationServiceClass)

        var notificationServiceIsRunning = false
        // Loop through running services
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (notificationServiceClass.name == service.service.className) {
                // If the service is running then return true
                notificationServiceIsRunning = true
            }
        }

        if(!notificationServiceIsRunning) {
            startService(notificationIntent)
        }
        else Log.e("NOTIFICATION", "Notification service already started (on login)")
    }
}
