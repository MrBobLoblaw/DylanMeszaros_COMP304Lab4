package com.dylan.dylanmeszaros_comp304lab4_ex1.views

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dylan.dylanmeszaros_comp304lab4_ex1.data.RouteObject
import com.dylan.dylanmeszaros_comp304lab4_ex1.viewmodel.RouteViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.koinViewModel

@Composable
fun RouteList(modifier: Modifier, context: Context, onSwitch: (RouteObject) -> Unit) {
    var routeViewModel: RouteViewModel = koinViewModel()
    LazyColumn(
        modifier = modifier
    ) {
        items(routeViewModel.getSavedRouteObjects()) { routeObject ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Display_WeatherCard(routeObject, context, onSwitch = { onSwitch(routeObject) });
            }
        }
    }
}

@Composable
fun Display_WeatherCard(routeObject: RouteObject, context: Context, onSwitch: () -> Unit) {
    var routeViewModel: RouteViewModel = koinViewModel()
    //var newWeather by remember { mutableStateOf(weather) };
    var isLoading by remember { mutableStateOf(true) };
    /*val uptodateWeatherObject = runBlocking {
        routeViewModel.get(LatLng(weatherObject.coord.latitude, weatherObject.coord.longitude));
    }*/
    //weatherObject = updatedWeatherObject;

    //weatherObject.id = uptodateWeatherObject.id;

    // Fetch weather data and wait till done loading
    if (isLoading){
        if (routeObject != null){
            isLoading = false;
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (isLoading) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = routeObject.origin!!.name!!, style = MaterialTheme.typography.bodyLarge);
                    IconButton(onClick = onSwitch) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "View"
                        );
                    }
                }
                Text(text = "-to-", style = MaterialTheme.typography.bodyMedium);
                Spacer(modifier = Modifier.height(4.dp));
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = routeObject.destination!!.name!!, style = MaterialTheme.typography.bodyLarge);
                    IconButton(onClick = {
                        Log.d("Delete Status: Successful?", routeViewModel.removeSavedRoute(routeObject).toString());
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove"
                        );
                    }
                }
            }
        }
    }
}