package com.dylan.dylanmeszaros_comp304lab4_ex1.data

var savedRouteObjects: MutableList<RouteObject> = mutableListOf();

class RouteRepositoryImpl: RouteRepository {

    override fun getSavedRouteObjects(): List<RouteObject> {
        return savedRouteObjects;
    }

    override fun addRouteObjectToSaved(newRouteObject: RouteObject): RouteObject {
        savedRouteObjects.add(0, newRouteObject);
        cleanDuplicates();
        return newRouteObject;
    }

    override fun getSavedIndex(routeObject: RouteObject): Int? {
        for (i in 0..savedRouteObjects.count()){
            if (savedRouteObjects[i].routePoints == routeObject.routePoints){
                return i;
            }
        }
        return null;
    }

    override fun removeSavedRoute(routeObject: RouteObject): Boolean {
        val index = getIndexFromPlaceIds(routeObject);
        if (index != -1){
            savedRouteObjects.removeAt(index);
            return true;
        }
        return false;
    }

}

fun cleanDuplicates() {
    val seenIds = mutableSetOf<Int>();
    savedRouteObjects = savedRouteObjects.filter { routeObject ->
        val index = getIndexFromPlaceIds(routeObject);
        if (index == -1){ return; } // Error
        val isNew = seenIds.add(getIndexFromPlaceIds(routeObject));
        isNew;
    }.toMutableList();
}

fun getIndexFromPlaceIds(routeObject: RouteObject): Int{
    for (i in 0..savedRouteObjects.count()){
        if (savedRouteObjects[i].origin!!.place_id == routeObject.origin!!.place_id
            && savedRouteObjects[i].destination!!.place_id == routeObject.destination!!.place_id){
            return i;
        }
    }
    return -1;
}