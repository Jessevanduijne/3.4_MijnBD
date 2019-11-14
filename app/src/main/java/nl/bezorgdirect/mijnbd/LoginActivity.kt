package nl.bezorgdirect.mijnbd

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener {

            val validated = validate(txt_username.text.toString(), txt_password.text.toString())
            if(validated)
            {
                val intent = Intent(this, MainActivity::class.java)
                finish()  //Kill the activity from which you will go to next activity
                startActivity(intent)
            }
        }

    }

    private fun validate(username: String?, password: String?): Boolean
    {
        if(username == "" || password == "")
        {
            sendToast(resources.getString(R.string.nocreds))
            return false
        }

        return true

    }

    private fun sendToast(message: String)
    {
        Toast.makeText(
            this, message,
            Toast.LENGTH_LONG
        ).show()

    }
}
