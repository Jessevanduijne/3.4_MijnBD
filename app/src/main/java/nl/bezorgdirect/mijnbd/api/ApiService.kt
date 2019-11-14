package nl.bezorgdirect.mijnbd.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    /************************auth******************************************/
    @POST("login") //Login as a Deliverer
    @FormUrlEncoded
    fun loginPost(@Field("emailAddress") email: String,
                  @Field("password") password: String): Call<User>

    @POST("logout") //Logout the authenticated Deliverer
    fun logoutPost(@Header("x-authtoken") auth: String): Call<Void>

    /************************availablities******************************************/
    @POST("availablities") //Availabilities will be pushed to authenticated Deliverer's Availabilities array
    fun availablitiesPost(@Header("x-authtoken") auth: String): Call<ResponseBody>

    @GET("availablities") //List Availabilities of authenticated Deliverer
    fun availablitiesGet(@Header("x-authtoken") auth: String): Call<ArrayList<Availability>>

    @PUT("availablities") //Update Availabilities of authenticated Deliverer
    fun availablitiesPut(@Header("x-authtoken") @Body availability: Availability): Call<Availability>

    @DELETE("availablities/{id}") //Delete Availability with {Id} of authenticated Deliverer
    fun availablitiesDelete(@Path("id") id: String, @Header("x-authtoken") auth: String): Call<ResponseBody>

    /************************Deliverers******************************************/
    @GET("me") //Read authenticated Deliverer
    fun deliverGet(@Header("x-authtoken") auth: String): Call<User>

    @PUT("me") //Update authenticated Deliverer
    fun deliverPut(@Header("x-authtoken") @Body user: User): Call<Availability>

    /************************Notifications******************************************/
    @GET("notifications") //Read Notification of authenticated Deliverer
    fun notificationGet(@Header("x-authtoken") auth: String): Call<BDNotification>

    @PATCH("notifications/{id}") //Update Notification of authenticated Deliverer
    @FormUrlEncoded
    fun notifacationPatch(@Header("x-authtoken") auth: String,
                          @Path("id") id: String,
                          @Field("accepted") accepted: Boolean): Call<ResponseBody>

    /************************Locations******************************************/
    @GET("locations/warehouses") //List Warehouses
    fun locationGet(@Header("x-authtoken") auth: String): Call<ArrayList<Location>>

    /************************Deliveries******************************************/
    @GET("deliverery") //Read Delivery of authenticated Deliverer
    fun delivereryGet(@Header("x-authtoken") auth: String): Call<Delivery>

    @PATCH("delivery/location") //Update currentLocation in Delivery
    @FormUrlEncoded
    fun deliverylocationPatch(@Header("x-authtoken") auth: String,
                              @Field("latitude") lat: Float,
                              @Field("longitude") lon: Float): Call<Delivery>

    @PATCH("delivery/{id}/status") //Update status in Delivery
    @FormUrlEncoded
    fun deliverystatusPatch(@Header("x-authtoken") auth: String,
                            @Path("id") id: String,
                            @Field("status") status: Int,
                            @Field("latitude") lat: Float,
                            @Field("longitude") lon: Float): Call<Delivery>

    @GET("delivereries") //List Deliveries of authenticated Deliverer
    fun delivereriesGet(@Header("x-authtoken") auth: String): Call<ArrayList<Delivery>>

}

