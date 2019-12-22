package nl.bezorgdirect.mijnbd.api


import nl.bezorgdirect.mijnbd.R
import retrofit2.Call
import retrofit2.http.*


public interface GoogleService {

    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") key: String,
        @Query("travelmode") travelmode: String
    ): Call<GoogleDirections>

    @GET("maps/api/distancematrix/json")
    fun getDistance(
        @Query("origins") origin: String,
        @Query("destinations") destination: String,
        @Query("key") key: String,
        @Query("mode") travelmode: String
    ): Call<GoogleDistance>
}