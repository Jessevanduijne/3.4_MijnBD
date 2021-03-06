package nl.bezorgdirect.mijnbd.api

// Route result classes:
data class GoogleDirections (

    val geocoded_waypoints : List<Geocoded_waypoints>,
    val routes : List<Routes>,
    val status : String
)

data class Steps (

    val distance : Distance,
    val duration : Duration,
    val end_location : End_location,
    val html_instructions : String,
    val polyline : Polyline,
    val start_location : Start_location,
    val travel_mode : String
)

data class Bounds (

    val northeast : Northeast,
    val southwest : Southwest
)

data class Distance (

    val text : String,
    val value : Int
)

data class Duration (

    val text : String,
    val value : Int
)

data class End_location (

    val lat : Double,
    val lng : Double
)

data class Geocoded_waypoints (

    val geocoder_status : String,
    val place_id : String,
    val types : List<String>
)

data class Legs (

    val distance : Distance,
    val duration : Duration,
    val end_address : String,
    val end_location : End_location,
    val start_address : String,
    val start_location : Start_location,
    val steps : List<Steps>,
    val traffic_speed_entry : List<String>,
    val via_waypoint : List<String>
)

data class Northeast (

    val lat : Double,
    val lng : Double
)

data class Overview_polyline (

    val points : String
)

data class Polyline (

    val points : String
)

data class Routes (

    val bounds : Bounds,
    val copyrights : String,
    val legs : List<Legs>,
    val overview_polyline : Overview_polyline,
    val summary : String,
    val warnings : List<String>,
    val waypoint_order : List<String>
)

data class Southwest (

    val lat : Double,
    val lng : Double
)

data class Start_location (

    val lat : Double,
    val lng : Double
)

// Distance matrix result classes:
data class GoogleDistance (

    val destination_addresses : List<String>,
    val origin_addresses : List<String>,
    val rows : List<Rows>,
    val status : String
)

data class Rows (
    val elements : List<Elements>
)

data class Elements (

    val distance : Distance,
    val duration : Duration,
    val status : String
)