package nl.bezorgdirect.mijnbd

import android.app.Application

class MijnbdApplication : Application(){
    companion object {
        // Global variables:
        var canReceiveNotification = true
    }
}