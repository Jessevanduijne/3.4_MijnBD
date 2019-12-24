package nl.bezorgdirect.mijnbd

import android.app.Application
import android.content.Context

class MijnbdApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }


    companion object {
        // Global variables:
        lateinit  var appContext: Context
        var canReceiveNotification = true
    }
}