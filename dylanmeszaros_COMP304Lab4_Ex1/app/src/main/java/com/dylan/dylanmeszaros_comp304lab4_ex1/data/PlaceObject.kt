package com.dylan.dylanmeszaros_comp304lab4_ex1.data

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.Json

data class PlaceResult(
    @Json(name="result")
    val result: PlaceObject
)

data class PlaceObject (
    @Json(name = "geometry")
    var geometry: PlaceGeometry?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "place_id")
    var place_id: String?
)

data class PlaceGeometry (
    @Json(name = "location")
    var location: GeometryLocation?
)

data class GeometryLocation(
    @Json(name = "lat")
    val lat: Double,
    @Json(name = "lng")
    val lng: Double
)