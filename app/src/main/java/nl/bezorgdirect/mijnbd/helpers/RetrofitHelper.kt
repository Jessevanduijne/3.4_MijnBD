package nl.bezorgdirect.mijnbd.helpers

import android.content.Context
import android.util.Log
import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.Delivery
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun getApiService(): ApiService{
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:7071/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ApiService::class.java)
    return service
}


