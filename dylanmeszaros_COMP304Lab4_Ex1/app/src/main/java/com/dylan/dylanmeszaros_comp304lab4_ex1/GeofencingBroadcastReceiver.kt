package com.dylan.dylanmeszaros_comp304lab4_ex1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("Geofence_Receiver", "Broadcast received!")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                Log.e("GBR_Event", "Error: ${geofencingEvent.errorCode}")
                return;
            }
        }
        else{
            Log.e("GBR_Event", "geofencingEvent was found as null")
            return;
        }

        // Get geofence transition type
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Check which transition occurred
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.e("GBR_Transition", "Entered geofence")
                geofenceEntered.value = true;
                // Handle geofence entry
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.e("GBR_Transition", "Exited geofence")
                geofenceExited.value = true;
                // Handle geofence exit
            }
            else -> {
                Log.e("GBR_Transition", "Unknown geofence transition")
            }
        }
    }
}