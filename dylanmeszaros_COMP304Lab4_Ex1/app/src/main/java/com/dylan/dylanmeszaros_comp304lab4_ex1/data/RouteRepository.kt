package com.dylan.dylanmeszaros_comp304lab4_ex1.data

interface RouteRepository {

    fun getSavedRouteObjects(): List<RouteObject>;
    fun addRouteObjectToSaved(newRouteObject: RouteObject): RouteObject;
    fun getSavedIndex(routeObject: RouteObject): Int?;
    fun removeSavedRoute(routeObject: RouteObject): Boolean;

}