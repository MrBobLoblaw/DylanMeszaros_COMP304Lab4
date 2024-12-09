package com.dylan.dylanmeszaros_comp304lab4_ex1.data

import com.google.android.gms.maps.model.LatLng
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleRoutesAPI {
    @GET("json")
    suspend fun getRoutesFromAPI(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") key: String = "AIzaSyBedjsYyLbI91jaNr8WsqjELG00pvZZWro",
    ): Response<DirectionsResponse>;
}