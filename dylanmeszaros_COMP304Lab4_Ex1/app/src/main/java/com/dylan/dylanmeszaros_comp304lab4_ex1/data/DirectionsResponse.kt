package com.dylan.dylanmeszaros_comp304lab4_ex1.data

data class DirectionsResponse(
    val geocoded_waypoints: List<Waypoint>,
    val routes: List<Route>
)

data class Route(
    val overview_polyline: OverviewPolyline
)

data class OverviewPolyline(
    val points: String
)

data class Waypoint(
    val place_id: String
)
