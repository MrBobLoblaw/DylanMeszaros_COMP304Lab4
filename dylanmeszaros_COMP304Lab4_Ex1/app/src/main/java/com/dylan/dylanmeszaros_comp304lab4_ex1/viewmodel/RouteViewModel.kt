package com.dylan.dylanmeszaros_comp304lab4_ex1.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.dylan.dylanmeszaros_comp304lab4_ex1.PlaceRetrofitClient
import com.dylan.dylanmeszaros_comp304lab4_ex1.RouteRetrofitClient
import com.dylan.dylanmeszaros_comp304lab4_ex1.data.PlaceObject
import com.dylan.dylanmeszaros_comp304lab4_ex1.data.RouteObject
import com.dylan.dylanmeszaros_comp304lab4_ex1.data.RouteRepository
import com.dylan.dylanmeszaros_comp304lab4_ex1.endLocationState
import com.dylan.dylanmeszaros_comp304lab4_ex1.startLocationState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.runBlocking

class RouteViewModel (
    private var routeRepository: RouteRepository
    // No Repository yet
): ViewModel() {

    suspend fun getPlaceFromAPI(place_id: String): PlaceObject{
        val foundPlace = PlaceRetrofitClient.placesAPI.getPlaceFromAPI(
            place_id, "AIzaSyBedjsYyLbI91jaNr8WsqjELG00pvZZWro"
        )

        return foundPlace.result;
    }

    suspend fun getRouteFromAPI(startLocation: LatLng, endLocation: LatLng): RouteObject? {
        val origin = "${startLocation.latitude},${startLocation.longitude}"
        val destination = "${endLocation.latitude},${endLocation.longitude}"
        try {
            val newRouteObject = RouteObject(
                PlaceObject(null, "", ""),
                PlaceObject(null, "",""),
                emptyList(),
                null
            );
            val response = RouteRetrofitClient.routesAPI.getRoutesFromAPI(
                origin, destination, "AIzaSyBedjsYyLbI91jaNr8WsqjELG00pvZZWro"
            );
            val body = response.body();

            if (body != null && body.routes.isNotEmpty()) {
                val polyline = body.routes[0].overview_polyline.points;
                newRouteObject.routePoints = decodePolyline(polyline); // Decode the polyline
                Log.d("Route Fetched", "Route Found")
            } else {
                Log.e("Route Fetch Failed", "No routes found")
                return null;
            }

            if (body.geocoded_waypoints.isNotEmpty() && body.geocoded_waypoints.count() >= 2){
                val newOrigin = runBlocking { getPlaceFromAPI(body.geocoded_waypoints[0].place_id); }
                val newDestination = runBlocking { getPlaceFromAPI(body.geocoded_waypoints[1].place_id); }

                newRouteObject.origin = newOrigin;
                newRouteObject.destination = newDestination;
                Log.d("Places Fetched", "Both Places found [Origin=${newRouteObject.origin!!.place_id}], [Destination=${newRouteObject.destination!!.place_id}]")
                return newRouteObject;
            }
            else{
                Log.e("Places Fetch Failed", "Missing one or more places")
                return null;
            }
        } catch (e: Exception) {
            Log.e("Route Fetch", "Error: ${e.message}")
            return null;
        }
    }

    fun addRouteObjectToSaved(newRouteObject: RouteObject): RouteObject{
        routeRepository.addRouteObjectToSaved(newRouteObject);
        return newRouteObject;
    }

    fun getSavedRouteObjects(): List<RouteObject>{
        return routeRepository.getSavedRouteObjects();
    }

    fun getSavedIndex(routeObject: RouteObject): Int?{
        return routeRepository.getSavedIndex(routeObject);
    }

    fun removeSavedRoute(routeObject: RouteObject): Boolean{
        return routeRepository.removeSavedRoute(routeObject);
    }
}

// Utility function to decode a polyline string into a list of LatLng
fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat
        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng
        val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
        poly.add(latLng)
    }
    return poly
}