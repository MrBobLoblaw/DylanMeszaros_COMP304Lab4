package com.dylan.dylanmeszaros_comp304lab4_ex1.data

import android.app.PendingIntent
import android.content.Context
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng

data class GeofenceManager (
    val geofenceLocation: LatLng?,
    val geofenceRequest: GeofencingRequest?,
    val pendingIntent: PendingIntent?,
    val context: Context
)