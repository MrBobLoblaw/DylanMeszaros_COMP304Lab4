package com.dylan.dylanmeszaros_comp304lab4_ex1.data

import com.google.android.gms.location.Geofence
import com.google.android.gms.maps.model.LatLng

data class RouteObject(
    var origin: PlaceObject?,
    var destination: PlaceObject?,
    var routePoints: List<LatLng>,
    var geofenceManager: GeofenceManager?
)