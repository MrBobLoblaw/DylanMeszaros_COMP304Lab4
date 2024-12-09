package com.dylan.dylanmeszaros_comp304lab4_ex1

import com.dylan.dylanmeszaros_comp304lab4_ex1.data.GooglePlacesAPI
import com.dylan.dylanmeszaros_comp304lab4_ex1.data.GoogleRoutesAPI
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object PlaceRetrofitClient {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/details/";
    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build();

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build();

    val placesAPI: GooglePlacesAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
            .create(GooglePlacesAPI::class.java)
    }

}