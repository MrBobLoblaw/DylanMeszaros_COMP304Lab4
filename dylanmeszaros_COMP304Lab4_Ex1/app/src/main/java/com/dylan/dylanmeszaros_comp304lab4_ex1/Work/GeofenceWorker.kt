package com.dylan.dylanmeszaros_comp304lab4_ex1.Work

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class GeofenceWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.e("WorkManager", "Started doing work")

        // Extract geofence location from inputData
        val latitude = inputData.getDouble("latitude", 0.0)
        val longitude = inputData.getDouble("longitude", 0.0)
        val geofenceLocation = LatLng(latitude, longitude)

        val geofence = SimulatedGeofence(geofenceLocation, 100f) // Replace with your geofence values
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        var isInsideGeofence = false
        val latch = CountDownLatch(1)

        Log.e("WorkManager", "Checking Permissions")
        // Check permissions
        if (ActivityCompat.checkSelfPermission(applicationContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(applicationContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("WorkManager", "Permissions Failed")
            return Result.failure()
        }

        Log.e("WorkManager", "Fetching last location listeners")
        // Get the current location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                isInsideGeofence = geofence.isInside(location)
            }
            latch.countDown()
        }.addOnFailureListener {
            latch.countDown()
        }

        Log.e("WorkManager", "Waiting for location result.")
        latch.await() // Wait for the location result
        return if (isInsideGeofence) {
            Log.e("WorkManager", "Successfully Inside Geofence")
            Result.success()
        } else {
            Log.e("WorkManager", "Successfully Exited Geofence")
            Result.retry()
        }
    }
}

fun scheduleGeofenceWorker(context: Context, geofenceLocation: LatLng) {
    val inputData = Data.Builder()
        .putDouble("latitude", geofenceLocation.latitude)
        .putDouble("longitude", geofenceLocation.longitude)
        .build()

    val geofenceWorkRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
        .setInputData(inputData)
        .setInitialDelay(15, TimeUnit.MINUTES) // Example delay
        .build()

    WorkManager.getInstance(context).enqueue(geofenceWorkRequest)
}