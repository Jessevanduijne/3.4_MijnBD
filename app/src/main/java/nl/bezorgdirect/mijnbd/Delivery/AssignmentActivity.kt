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

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "nl.bezorgdirect.mijnbd"
    private val description = "description"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_assignment)

        custom_toolbar_title.setText(getString(string.title_assignment))
        setSupportActionBar(custom_toolbar)

        val noAssignmentFragment = NoAssignmentFragment()
        supportFragmentManager.beginTransaction().replace(id.delivery_fragment, noAssignmentFragment).commit()


        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        btn_tesT_notification.setOnClickListener{
            val intent = Intent(this,AssignmentActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)

//            val contentView = RemoteViews(packageName, R.layout.notification_layout)
//            contentView.setTextViewText(R.id.tv_title,"CodeAndroid")
//            contentView.setTextViewText(R.id.tv_content,"Text notification")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel = NotificationChannel(channelId,description,NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.enableVibration(false)
                notificationManager.createNotificationChannel(notificationChannel)

                builder = Notification.Builder(this,channelId)
                    .setContentTitle("Nieuwe opdracht")
                    .setContentText("Kom doen dan")
                    .setSmallIcon(R.drawable.ic_check)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.bezorg_direct_logo_trans))
                    .setContentIntent(pendingIntent)
            }else{

                builder = Notification.Builder(this)
                    .setContentTitle("Nieuwe opdracht")
                    .setContentText("Kom doen dan")
                    .setSmallIcon(R.drawable.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.drawable.ic_launcher))
                    .setContentIntent(pendingIntent)
            }
            notificationManager.notify(1234,builder.build())

        }
    }

}
