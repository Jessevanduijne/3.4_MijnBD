package nl.bezorgdirect.mijnbd.helpers

import nl.bezorgdirect.mijnbd.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun getApiService(): ApiService{
    val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.178.18:7071/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ApiService::class.java)
    return service
}

