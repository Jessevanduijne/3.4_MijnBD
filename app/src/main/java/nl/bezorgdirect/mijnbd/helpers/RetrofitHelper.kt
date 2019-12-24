package nl.bezorgdirect.mijnbd.helpers

import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.GoogleService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun getApiService(): ApiService{
    val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.178.14:7071/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(ApiService::class.java)
    return service
}

fun getGoogleService(): GoogleService {

    val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(GoogleService::class.java)
    return service
}


