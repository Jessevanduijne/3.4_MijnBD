package nl.bezorgdirect.mijnbd.api

data class LoginParams(val emailAddress: String, val password: String)

data class UpdateNotificationParams(val accepted: Boolean)

data class UpdateLocationParams(val latitude: Float, val longitude: Float)

data class UpdateStatusParams(val status: Int, val latitude: Double, val longitude: Double)

data class UpdateUserParams (val emailAddress: String?, val phoneNumber: String?, val dateOfBirth: String?, val range: Int?, val vehicle: Int?, val fare: Float?
                             , val firstName: String? , val lastName: String?)

data class AddAvailabilityParams (val date: String?, val startTime: String?, val endTime: String?)