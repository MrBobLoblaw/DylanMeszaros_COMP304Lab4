package com.dylan.dylanmeszaros_comp304lab4_ex1.Work

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng

class SimulatedGeofence(
    private val center: LatLng,
    private val radius: Float // in meters
) {
    fun isInside(location: Location): Boolean {
        Log.e("WorkManager_Sim", "Checked?")
        val results = FloatArray(1)
        Location.distanceBetween(
            center.latitude, center.longitude,
            location.latitude, location.longitude,
            results
        )
        return results[0] <= radius
    }
}