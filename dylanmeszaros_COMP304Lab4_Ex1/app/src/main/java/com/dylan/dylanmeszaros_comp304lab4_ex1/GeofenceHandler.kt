package com.dylan.dylanmeszaros_comp304lab4_ex1

import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

fun createGeofence(location: LatLng, radius: Float): Geofence {
    return Geofence.Builder()
        .setRequestId("MyGeofence") // Unique ID for the geofence
        .setCircularRegion(location.latitude, location.longitude, radius) // Geofence coordinates
        .setExpirationDuration(Geofence.NEVER_EXPIRE) // Geofence lifetime
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
        .build()
}

fun createGeofencingRequest(geofence: Geofence): GeofencingRequest {
    return GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()
}

fun getGeofencePendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
    return PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
}

fun addGeofence(context: Context, geofencingRequest: GeofencingRequest, pendingIntent: PendingIntent) {
    val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    if (context.checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED){
        if (context.checkSelfPermission(ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED) {

            geofencingClient.addGeofences(geofencingRequest, pendingIntent).run {
                addOnSuccessListener {
                    // Geofence added successfully
                    Log.i("addGeofence", "Added Geofence successfully")
                }
                addOnFailureListener { err ->
                    // Failed to add geofence
                    Log.i("addGeofence", "Failed to add geofence: ${err.message}")
                }
            }
        }
        else{
            Log.i("addGeofence", "ACCESS_BACKGROUND_LOCATION Permission was not granted")
        }
    }
    else{
        Log.i("addGeofence", "ACCESS_FINE_LOCATION Permission was not granted")
    }

}