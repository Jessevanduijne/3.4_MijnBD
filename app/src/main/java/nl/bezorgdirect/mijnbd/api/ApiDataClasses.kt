package nl.bezorgdirect.mijnbd.api

data class Feed (val Id: Int?, val Name: String?)

data class Location (val id: String?, val latitude: Double?, val longitude: Double?, val address: String?, val postalCode: String?,
                     val place: String?, val isWarehouse: Boolean?)

data class User (val id: String?, var emailAddress: String?, val firstName: String?, val lastName: String?, var phoneNumber: String?,
                          val home: Location, val dateOfBirth: String?, var range: Int?, var vehicle: Int?, val vehicleDisplayName: String?, val fare: Float?, val totalEarnings: Float?)

data class Availability (val id: String?, val delivererId: String?, var date:  String?, var startTime: String?, var endTime: String?)

data class BDNotification (val Id: String?, val DelivererId: String?, val DeliveryId: String?,val CreatedAt: String?, val AcceptedAt: String?,
                           val RefusedAt: String?, val ExpiredAt: String?, val Status: Int?, val StatusDisplayName: String?)


data class Delivery (val Id: String?, val DelivererId: String?, val CustomerPhoneNumber: String?, val DueDate: String?, val Vehicle: Int?, val StartedAtId: String?,
                     val WarehouseDistanceInKilometers: Float?, val WarehouseETA: String?, val WarehousePickUpAt:  String?, val CustomerDistanceInKilometers: Float?,
                     val CustomerETA: String?, val CustomerId: String?, val CurrentId: String?, val DeliveredAt: String?, val Price: Float?, val tip: Float?,
                     val PaymentMethod: Int?, val PaymenMethodDisplayName: String?, val Status: Int?, val StatusDisplayName: String?, val Warehouse: Location,
                     val Customer: Location, val Current: Location, val VehicleDisplayName: String)



