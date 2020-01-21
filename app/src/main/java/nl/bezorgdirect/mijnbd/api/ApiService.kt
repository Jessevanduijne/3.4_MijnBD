package nl.bezorgdirect.mijnbd.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    /************************auth******************************************/
    @POST("login") //Login as a Deliverer
    fun loginPost(@Body params: LoginParams): Call<String>

    @POST("logout") //Logout the authenticated Deliverer
    fun logoutPost(@Header("Authorization") auth: String): Call<Void>

    /************************availablities******************************************/
    @POST("availabilities") //Availabilities will be pushed to authenticated Deliverer's Availabilities array
    fun availablitiesPost(@Header("Authorization") auth: String, @Body availabilityPost: ArrayList<AddAvailabilityParams>): Call<ArrayList<Availability>>

    @GET("availabilities") //List Availabilities of authenticated Deliverer
    fun availablitiesGet(@Header("Authorization") auth: String): Call<ArrayList<Availability>>

    @PUT("availabilities") //Update Availabilities of authenticated Deliverer
    fun availablitiesPut(@Header("Authorization") auth: String, @Body availability: ArrayList<Availability>): Call<ArrayList<Availability>>

    @DELETE("availabilities/{id}") //Delete Availability with {Id} of authenticated Deliverer
    fun availablitiesDelete(@Path("id") id: String, @Header("Authorization") auth: String): Call<ResponseBody>

    /************************Deliverers******************************************/
    @GET("me") //Read authenticated Deliverer
    fun delivererGet(@Header("Authorization") auth: String): Call<User>

    @GET("me/earnings") //Read authenticated Deliverer

    fun delivererGetEarnings(@Header("Authorization") auth: String, @Query("timeframe") timeframe: String = "day" ): Call<String>//Get total earnings of authenticated Deliverer

    @PUT("me") //Update authenticated Deliverer
    fun delivererPut(@Header("Authorization") auth: String, @Body params: UpdateUserParams): Call<ResponseBody>

    /************************Notifications******************************************/
    @GET("notifications") //Read Notification of authenticated Deliverer
    fun notificationGet(@Header("Authorization") auth: String): Call<BDNotification>

    @PATCH("notifications/{id}") //Update Notification of authenticated Deliverer
    fun notificationPatch(@Header("Authorization") auth: String,
                          @Path("id") id: String,
                          @Body params: UpdateNotificationParams): Call<ResponseBody>

    /************************Locations******************************************/
    @GET("locations/warehouses") //List Warehouses
    fun locationGet(@Header("Authorization") auth: String): Call<ArrayList<Location>>

    /************************Deliveries******************************************/
    @GET("deliveries/current") //Read Delivery of authenticated Deliverer
    fun deliveryGetCurrent(@Header("Authorization") auth: String): Call<Delivery>

    @GET("deliveries/{id}") //Read Delivery of authenticated Deliverer
    fun deliveryGetById(@Header("Authorization") auth: String,
                        @Path("id") id: String): Call<Delivery>

    @PATCH("deliveries/location") //Update currentLocation in Delivery
    fun deliverylocationPatch(@Header("Authorization") auth: String,
                              @Body params: UpdateLocationParams): Call<Delivery>

    @PATCH("deliveries/{id}/status") //Update status in Delivery
    fun deliverystatusPatch(@Header("Authorization") auth: String,
                            @Path("id") id: String,
                            @Body params: UpdateStatusParams): Call<Void>

    @GET("deliveries") //List Deliveries of authenticated Deliverer
    fun deliveriesGet(@Header("Authorization") auth: String): Call<ArrayList<Delivery>>

}

