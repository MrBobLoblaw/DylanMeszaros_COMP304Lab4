package com.dylan.dylanmeszaros_comp304lab4_ex1.data

import com.google.android.gms.maps.model.LatLng
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesAPI {
    @GET("json")
    suspend fun getPlaceFromAPI(
        @Query("place_id") place_id: String,
        @Query("key") key: String = "AIzaSyBedjsYyLbI91jaNr8WsqjELG00pvZZWro",
    ): PlaceResult;
}