package nl.bezorgdirect.mijnbd.helpers

import com.google.gson.GsonBuilder
import nl.bezorgdirect.mijnbd.api.ApiService
import nl.bezorgdirect.mijnbd.api.GoogleService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



fun getApiService(): ApiService{

    val gson = GsonBuilder()
        .setLenient()
        .create()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://bezorgdirect-bezorgersapplicatie-api.azurewebsites.net/api/")
        .addConverterFactory(GsonConverterFactory.create(gson))
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


