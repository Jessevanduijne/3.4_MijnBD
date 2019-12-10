package nl.bezorgdirect.mijnbd.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import nl.bezorgdirect.mijnbd.Delivery.AssignmentActivity
import nl.bezorgdirect.mijnbd.api.BDNotification
import nl.bezorgdirect.mijnbd.helpers.getApiService
import nl.bezorgdirect.mijnbd.helpers.getDecryptedToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.UnsupportedOperationException
import nl.bezorgdirect.mijnbd.MijnbdApplication.Companion.canReceiveNotification
import nl.bezorgdirect.mijnbd.api.Delivery
import android.app.ActivityManager
import android.R
import android.annotation.SuppressLint
import android.content.ComponentName
import androidx.core.content.getSystemService
import nl.bezorgdirect.mijnbd.Delivery.NewAssignmentListener


class NotificationService: Service() {

    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "nl.bezorgdirect.mijnbd"
    private val apiService = getApiService()

    override fun onBind(intent: Intent?): IBinder? {
        // If the application needs to send data the service, this method will be used
        Log.e("", "")
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.e("NOTIFICATION", "STARTING" )

        mHandler = Handler()
        mRunnable = Runnable { checkForNotifications() }

        // Initial delay is mandatory: otherwise runnable won't get triggered
        mHandler.postDelayed(mRunnable, 10000)
        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("NOTIFICATION", "DESTROYING")
//        mHandler.removeCallbacks(mRunnable)
    }

    private fun checkForNotifications() {
        if(canReceiveNotification) {
            getNotification()

            // Check every 20 seconds
            mHandler.postDelayed(mRunnable, 20000)
        }
    }

    private fun getNotification() {
        val decryptedToken = getDecryptedToken(this.applicationContext!!)
        apiService.notificationGet(decryptedToken)
            .enqueue(object: Callback<BDNotification> {
                override fun onResponse(call: Call<BDNotification>, response: Response<BDNotification>) {
                    if(response.isSuccessful && response.body() != null) {
                        mHandler.postDelayed(mRunnable, 20000)
                        val notification: BDNotification = response.body()!!
                        getDeliveryInfoForNotification(notification.DeliveryId!!)

                        canReceiveNotification = false // Until response of user, receive no new notifications
                    }
                    else Log.e("NOTIFICATION", "No notification for you at the moment") // TODO: Remove in final version, or this will be spam
                }

                override fun onFailure(call: Call<BDNotification>, t: Throwable) {
                    Log.e("NOTIFICATION", "Something went wrong with the notification call (getNotification)")
                }
            })
    }

    private fun getDeliveryInfoForNotification(deliveryId: String){
        val decryptedToken = getDecryptedToken(this.applicationContext!!)
        apiService.deliveryGetById(decryptedToken, deliveryId)
            .enqueue(object: Callback<Delivery> {
                override fun onResponse(call: Call<Delivery>, response: Response<Delivery>) {
                    if(response.isSuccessful && response.body() != null) {
                        val delivery = response.body()!!
                        showNotification(delivery)
                    }
                    else Log.e("NOTIFICATION", "delivery call unsuccessful or body empty")
                }

                override fun onFailure(call: Call<Delivery>, t: Throwable) {
                    Log.e("NOTIFICATION", "Something went wrong with the delivery call (getDeliveryForNotification)")
                }
            })
    }

    private fun showNotification(deliveryInfo: Delivery){
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, AssignmentActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val contentView = RemoteViews(packageName, nl.bezorgdirect.mijnbd.R.layout.notification)
        contentView.setTextViewText(nl.bezorgdirect.mijnbd.R.id.notification_title, getString(nl.bezorgdirect.mijnbd.R.string.app_name))
        contentView.setTextViewText(nl.bezorgdirect.mijnbd.R.id.notification_header, getString(nl.bezorgdirect.mijnbd.R.string.lbl_new_assignment))
        contentView.setTextViewText(nl.bezorgdirect.mijnbd.R.id.notification_subtext1, getString(nl.bezorgdirect.mijnbd.R.string.lbl_earn))
        contentView.setTextViewText(nl.bezorgdirect.mijnbd.R.id.notification_subtext2, getString(nl.bezorgdirect.mijnbd.R.string.lbl_euro))
        contentView.setTextViewText(nl.bezorgdirect.mijnbd.R.id.notification_subtext3, deliveryInfo.Price!!.toBigDecimal().setScale(2).toString())


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, getString(nl.bezorgdirect.mijnbd.R.string.lbl_channel_name), NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.YELLOW
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setContent(contentView)
                .setSmallIcon(nl.bezorgdirect.mijnbd.R.drawable.ic_logo_b)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, nl.bezorgdirect.mijnbd.R.drawable.ic_logo_y))
                .setContentIntent(pendingIntent)
        }
        else {
            builder = Notification.Builder(this)
                .setContent(contentView)
                .setSmallIcon(nl.bezorgdirect.mijnbd.R.drawable.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, nl.bezorgdirect.mijnbd.R.drawable.ic_launcher))
                .setContentIntent(pendingIntent)
        }

        notificationManager.notify(0, builder.build()) // Todo: what does this id do?
    }
}