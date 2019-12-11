package nl.bezorgdirect.mijnbd

import android.app.Application
import android.view.View

class MijnbdApplication : Application(){
    companion object {
        // Global variables:
        var canReceiveNotification = true
    }
}