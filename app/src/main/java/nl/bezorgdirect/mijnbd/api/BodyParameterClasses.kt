package nl.bezorgdirect.mijnbd.api

data class LoginParams(val emailAddress: String, val password: String)

data class UpdateNotificationParams(val accepted: Boolean)

data class UpdateLocationParams(val latitude: Float, val longitude: Float)

data class UpdateStatusParams(val status: Int,val latitude: Float, val longitude: Float)

