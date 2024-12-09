package com.dylan.dylanmeszaros_comp304lab4_ex1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import com.android.volley.VolleyLog.TAG
import com.dylan.dylanmeszaros_comp304lab4_ex1.Work.scheduleGeofenceWorker
import com.dylan.dylanmeszaros_comp304lab4_ex1.data.GeofenceManager
import com.dylan.dylanmeszaros_comp304lab4_ex1.data.RouteObject
import com.dylan.dylanmeszaros_comp304lab4_ex1.di.appModules
import com.dylan.dylanmeszaros_comp304lab4_ex1.ui.theme.CoreTheme
import com.dylan.dylanmeszaros_comp304lab4_ex1.viewmodel.RouteViewModel
import com.dylan.dylanmeszaros_comp304lab4_ex1.views.RouteList
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

var onStartup = false;

lateinit var fusedLocationClient: FusedLocationProviderClient;
val defaultLocation = LatLng(43.6532, -79.3832); // Toronto as fallback

// Shared state for user location
var startLocationState = mutableStateOf<LatLng?>(null);
var endLocationState = mutableStateOf<LatLng?>(null);
var searchState = mutableStateOf<Boolean>(false);

// Geofence
//var geofencingClient = mutableStateOf<GeofencingClient?>(null);
var geofenceManager = mutableStateOf<GeofenceManager?>(null);
var geofenceEntered = mutableStateOf<Boolean>(false);
var geofenceExited = mutableStateOf<Boolean>(false);

var userLocationState = mutableStateOf(defaultLocation);

// Route
var routeGetDebounce = mutableStateOf<Boolean>(false);

class MainActivity : AppCompatActivity() {

    private lateinit var workRequest: OneTimeWorkRequest

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onStartup == false){
            onStartup = true;
            startKoin {
                modules(appModules);
            }
        }
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        //scheduleBackgroundTask();

        //monitorWorkerStatus();

        setContent {
            val routeViewModel: RouteViewModel = koinViewModel();

            CoreTheme {
                MainActivity_Main(onSwitch = { routeObject ->
                    startActivity(Intent(this@MainActivity, MapActivity::class.java).apply{
                        if (routeObject != null){
                            putExtra("routeID", routeViewModel.getSavedIndex(routeObject));
                        }
                    });
                    finish();
                }, this);
                //testWeatherData() // Test API
            }
        }


    }

    private fun scheduleBackgroundTask(){
        val workRequest = OneTimeWorkRequestBuilder<Worker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(workRequest);
    }

    private fun monitorWorkerStatus() {
        // Use the WorkManager to observe the worker status
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.getWorkInfoByIdLiveData(workRequest.id).observe(this, Observer { workInfo ->
            if (workInfo != null) {
                // Here, you can check the state of the worker
                when (workInfo.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        // Task was successful
                        Log.d("Worker", "Work succeeded")
                    }

                    WorkInfo.State.FAILED -> {
                        // Task failed
                        Log.d("Worker", "Work failed")
                    }

                    WorkInfo.State.RUNNING -> {
                        // Task is running
                        Log.d("Worker", "Work is running")
                    }

                    WorkInfo.State.ENQUEUED -> {
                        // Task is enqueued and waiting to run
                        Log.d("Worker", "Work is enqueued")
                    }

                    WorkInfo.State.BLOCKED -> TODO()
                    WorkInfo.State.CANCELLED -> TODO()
                }
            }
        })
    }
}

@ExperimentalCoroutinesApi
class MapActivity : AppCompatActivity() {

    private lateinit var startAutocomplete: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Register the permission launcher
        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchCurrentLocation { location ->
                    // Update the location state from the result
                    userLocationState.value = location
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        };

        // Register PlacesAPI
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyBedjsYyLbI91jaNr8WsqjELG00pvZZWro")
        }

        // Searchbar activity
        startAutocomplete = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            //Log.i(TAG, "Check for resultCode")
            if (result.resultCode == Activity.RESULT_OK) {
                //Log.i(TAG, "resultCode is ok")
                val intent = result.data
                if (intent != null) {
                    //Log.i(TAG, "intent exists")
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    //Log.i(TAG, "place recieved")
                    if (place.location != null) {
                        userLocationState.value = place.location!!;
                        searchState.value = true;
                    }
                    else{
                        //Log.e(TAG, "Location is null");
                    }

                    //Log.i( TAG, "Place: ${place.displayName}, ${place.id}" )
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                //Log.i(TAG, "User canceled autocomplete")
            }
        }

        // Setup new Geofence
        //geofencingClient.value = LocationServices.getGeofencingClient(this);

        // Update pending
        /*val geofencePendingIntent: PendingIntent by lazy{
            val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }*/

        setContent {

            val routeViewModel: RouteViewModel = koinViewModel();

            var locationPermissionGranted by remember { mutableStateOf(false) };
            var backgroundLocationPermissionGranted by remember { mutableStateOf(false) };

            LaunchedEffect(Unit) {
                // Check location permissions
                if (ContextCompat.checkSelfPermission(this@MapActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                    locationPermissionGranted = true
                    fetchCurrentLocation { location ->
                        userLocationState.value = location;
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }

                if (ContextCompat.checkSelfPermission(this@MapActivity,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
                    backgroundLocationPermissionGranted = true
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                }
            }

            val routeID = intent.getIntExtra("routeID", -1);
            val foundRouteObject: RouteObject?
            if (routeID == -1){
                foundRouteObject = null;
            }
            else{
                foundRouteObject = routeViewModel.getSavedRouteObjects()[routeID];
            }

            // Display Google Map with the current location
            MapActivity_Main(this, onSwitch = {
                startActivity(Intent(this@MapActivity, MainActivity::class.java));
                finish();
            }, locationPermissionGranted, onSearch = {
                // Set Fields to read
                val fields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION)

                // Start the autocomplete intent.
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this)
                startAutocomplete.launch(intent);
            }, foundRouteObject);
        }


    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume called")
    }

    // Fetch current location using FusedLocationProviderClient
    private fun fetchCurrentLocation(onLocationFetched: (LatLng) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    onLocationFetched(latLng)
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                };
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivity_Main(onSwitch: (RouteObject?) -> Unit, context: Context) {


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Saved Travel Routes", fontWeight = FontWeight.Bold)
                },
                colors =  TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier,
                onClick = { onSwitch(null) }
            ) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Pick a new route");
            }
        },
        content =  { paddingValues ->
            RouteList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                context = context,
                onSwitch
            )
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapActivity_Main(context: Context, onSwitch: () -> Unit, isLocationEnabled: Boolean, onSearch: () -> Unit, routeObject: RouteObject?) {
    // GET VIEW MODEL IF THERE IS ONE
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Google Map", fontWeight = FontWeight.Bold)
                },
                colors =  TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        content =  {

            // Google Maps Code Started

            MapScreen(context, onSwitch, isLocationEnabled, onSearch, routeObject);//, onSearch());

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(context: Context, onSwitch: () -> Unit, isLocationEnabled: Boolean, onSearch: () -> Unit, lastRouteObject: RouteObject?) {
    var screenInitialize by remember { mutableStateOf(true) }
    if (screenInitialize){
        if (lastRouteObject != null){
            if (lastRouteObject.origin!!.geometry!!.location != null && lastRouteObject.destination!!.geometry!!.location != null){
                //Log.d("MapScreenInitialization", "lastRouteObject Location Found as not Null");
                //Log.d("mpInit", "p1[Org_id=${lastRouteObject.origin!!.place_id}], p2[Des_id=${lastRouteObject.destination!!.place_id}]");
                //Log.d("mpInit", "p1[Org=${lastRouteObject.origin!!.geometry!!.location}], p2[Des=${lastRouteObject.destination!!.geometry!!.location}]");
                startLocationState.value = LatLng(lastRouteObject.origin!!.geometry!!.location!!.lat, lastRouteObject.origin!!.geometry!!.location!!.lng);
                endLocationState.value = LatLng(lastRouteObject.destination!!.geometry!!.location!!.lat, lastRouteObject.destination!!.geometry!!.location!!.lng);

                val middlePoint = LatLng((lastRouteObject.origin!!.geometry!!.location!!.lat + lastRouteObject.destination!!.geometry!!.location!!.lat) / 2.0,
                    (lastRouteObject.origin!!.geometry!!.location!!.lng + lastRouteObject.destination!!.geometry!!.location!!.lng) / 2.0
                );
                userLocationState.value = middlePoint;
            }
        }
        screenInitialize = false;
    }

    Log.d("MapScreen", "Reference RouteViewModel");
    val routeViewModel: RouteViewModel = koinViewModel();

    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocationState.value, 14f);
    };

    // Marker Info
    var markerClicked by remember { mutableStateOf(false) }
    var clickedLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Route Info
    Log.d("MapScreen", "Initialize Route List");
    var routeObject by remember { mutableStateOf<RouteObject?>(null)}
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
            //: MutableList<LatLng> = mutableListOf();

    // Fetch the route
    Log.d("MapScreen", "Fetch Route");
    if (startLocationState.value != null && endLocationState.value != null){
        Log.d("MapScreen", "Route Fetched");

        if (!routeGetDebounce.value){
            routeGetDebounce.value = true;

            routeObject = runBlocking {
                routeViewModel.getRouteFromAPI(startLocationState.value!!, endLocationState.value!!);
            };
            routeObject!!.geofenceManager = geofenceManager.value;
        }
    }
    else{
        routeObject = null;
        routeGetDebounce.value = false;
    }

    // Fetch the geofence
    if (endLocationState.value != null && geofenceManager.value == null){
        Log.d("MS_Geofence", "Geofence started");

        if (geofenceManager.value == null){
            // Updated Geofence Map element
            val newGeofence = createGeofence(endLocationState.value!!, 100f);

            geofenceManager.value = GeofenceManager(endLocationState.value!!, createGeofencingRequest(newGeofence), getGeofencePendingIntent(context), context);

            addGeofence(geofenceManager.value!!.context, geofenceManager.value!!.geofenceRequest!!, geofenceManager.value!!.pendingIntent!!);

            scheduleGeofenceWorker(context, endLocationState.value!!);
        }
        Log.d("MS_Geofence", "Geofence returned no errors.")
    }

    // Google Map Start
    GoogleMap(
        modifier = Modifier
            .absolutePadding(
                top = 65.dp
            )
            .fillMaxWidth(1f)
            .fillMaxHeight(1f),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = isLocationEnabled,
            isIndoorEnabled = true
        ),
        onMapLongClick = { latLng ->
            markerClicked = true;
            Log.d("MapScreen", "Map long-clicked at: $latLng");
            clickedLatLng = latLng; // Capture clicked location
            userLocationState.value = latLng
        }
    ) {

        // Draw Start Location Marker
        if (startLocationState.value != null){
            // Add a marker at searched location
            Marker(
                state = MarkerState(position = startLocationState.value!!),
                title = "End",
                snippet = "Start Location",
                onClick = {
                    markerClicked = true;
                    clickedLatLng = it.position;
                    Log.d("MapScreen", "Marker clicked: ${it.title}")
                    true
                },

            );
        }

        // Draw Destination Marker
        if (endLocationState.value != null){
            // Add a marker at searched location
            Marker(
                state = MarkerState(position = endLocationState.value!!),
                title = "Start",
                snippet = "Destination",
                onClick = {
                    markerClicked = true;
                    clickedLatLng = it.position;
                    Log.d("MapScreen", "Marker clicked: ${it.title}")
                    true
                }
            );
        }

        // Draw Polyline for route between Start Location and Destination
        if (startLocationState.value != null && endLocationState.value != null && routeObject != null){
            //Polyline(points = routePoints, color = Color.Blue, width = 8f)
            if (routeObject!!.routePoints.isNotEmpty()) {
                Polyline(points = routeObject!!.routePoints, color = Color.Blue, width = 16f)
            }

            if (geofenceManager.value != null){
                com.google.maps.android.compose.Circle(
                    center = endLocationState.value!!,
                    radius = 160.0,
                    strokeColor = Color.Cyan
                )
            }
        }


        // Zoom in on the user's current location
        cameraPositionState.move(update = CameraUpdateFactory.newLatLngZoom(userLocationState.value, 14f));

    }

    // End Google Map

    // Go Back Button Start
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top-left Floating Action Button
        FloatingActionButton(
            onClick = {
                onSwitch();
            },
            modifier = Modifier
                .absolutePadding(
                    left = 10.dp,
                    top = 80.dp
                )
                .align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Go back");
        }
    }
    // End Go Back Button

    // Search Bar Start
    Box( // Background Box
        modifier = Modifier
            .offset(
                x = 70.dp,
                y = 80.dp
            )
            .size(
                width = 280.dp,
                height = 55.dp
            )
            .background(Color.White, RoundedCornerShape(16.dp)),
    ) {
        Text(
            modifier = Modifier
                .offset(
                    x = 25.dp,
                    y = 15.dp,
                )
                .fillMaxSize(),
            text = "Search for Location",
            style = TextStyle(
                fontWeight = FontWeight.Bold,  // Makes the text bold
                fontSize = 22.sp             // Set the font size (adjust to your preference)
            ),
            textAlign = TextAlign.Center
        )
        FloatingActionButton(
            onClick = {
                onSearch();
            },
            modifier = Modifier
                .offset(x = 25.dp, y = 15.dp,)
                .fillMaxSize()
                .alpha(0f),
            containerColor = Color.Transparent
        ) { }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top-left Floating Action Button
        FloatingActionButton(
            onClick = {
                onSearch();
            },
            modifier = Modifier
                .absolutePadding(
                    left = 70.dp,
                    top = 80.dp
                )
                .align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search for a Destination");
        }
    }
    // End Search Bar

    // Save Button Start
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top-left Floating Action Button
        FloatingActionButton(
            onClick = {
                // VIEW MODEL CODE HERE
                if (startLocationState.value != null && endLocationState.value != null
                    && geofenceManager.value != null && routeObject != null){
                    routeViewModel.addRouteObjectToSaved(routeObject!!);
                }

            },
            modifier = Modifier
                .absolutePadding(
                    left = 10.dp,
                    top = 140.dp
                )
                .align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Save Current Route");
        }
    }
    // End Save Button

    // Marker Interaction Start
    if (markerClicked || searchState.value) {
        if (searchState.value){
            clickedLatLng = userLocationState.value;
        }

        // Background Deselect
        Box( // Background Box
            modifier = Modifier
                .offset(
                    x = 0.dp,
                    y = 0.dp
                )
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.5f)) // Transparent Color
        ) {
            FloatingActionButton(
                onClick = {
                    markerClicked = false;
                },
                modifier = Modifier
                    .absolutePadding(
                        left = 0.dp,
                        top = 0.dp
                    )
                    .fillMaxSize()
                    .align(Alignment.TopStart),
                containerColor = Color(0x00FFFFFF),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                )
            ) {

            }
        }

        Box( // Background Box
            modifier = Modifier
                .offset(
                    x = 60.dp,
                    y = 200.dp
                )
                .size(
                    width = 290.dp,
                    height = 180.dp
                )
                .background(Color.White, RoundedCornerShape(16.dp)),

            ) {

            // Choose as the Start Location
            Text(
                modifier = Modifier
                    .offset(
                        x = 60.dp,
                        y = 20.dp,
                    )
                    .fillMaxSize(),
                text = "Set as Start Location",
                style = TextStyle(
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left
            )
            FloatingActionButton(
                onClick = {
                    startLocationState.value = clickedLatLng;
                    userLocationState.value = startLocationState.value!!; // This should update the position of the user's location
                    markerClicked = false;
                    searchState.value = false;

                    routeGetDebounce.value = false;
                },
                modifier = Modifier
                    .absolutePadding(
                        left = 10.dp,
                        top = 10.dp
                    )
                    .size(
                        width = 40.dp,
                        height = 40.dp
                    )
                    .align(Alignment.TopStart)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Choose as Start");
            }

            // Choose as the End Location
            Text(
                modifier = Modifier
                    .offset(
                        x = 60.dp,
                        y = 70.dp,
                    )
                    .fillMaxSize(),
                text = "Set as Destination",
                style = TextStyle(
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left
            )
            FloatingActionButton(
                onClick = {
                    endLocationState.value = clickedLatLng;
                    userLocationState.value = endLocationState.value!!; // This should update the position of the user's location
                    markerClicked = false;
                    searchState.value = false;

                    routeGetDebounce.value = false;

                    geofenceManager.value = null;
                },
                modifier = Modifier
                    .absolutePadding(
                        left = 10.dp,
                        top = 60.dp
                    )
                    .size(
                        width = 40.dp,
                        height = 40.dp
                    )
                    .align(Alignment.TopStart)
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Choose as Start");
            }

            // Delete Marker
            Text(
                modifier = Modifier
                    .offset(
                        x = 60.dp,
                        y = 120.dp,
                    )
                    .fillMaxSize(),
                text = "Remove Marker",
                style = TextStyle(
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left
            )
            FloatingActionButton(
                onClick = {
                    if (clickedLatLng == startLocationState.value) {
                        startLocationState.value = null;
                    }
                    else if (clickedLatLng == endLocationState.value) {
                        endLocationState.value = null;
                    }

                    markerClicked = false;
                    searchState.value = false;

                    routeGetDebounce.value = false;
                    geofenceManager.value = null;
                },
                modifier = Modifier
                    .absolutePadding(
                        left = 10.dp,
                        top = 110.dp
                    )
                    .size(
                        width = 40.dp,
                        height = 40.dp
                    )
                    .align(Alignment.TopStart),
                containerColor = Color(0xffd45959)

            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                );
            }
        }
    }
    // End Marker Interaction

    // Geofence Enter Notification
    if (geofenceEntered.value) {
        geofenceExited.value = false;

        // Background Deselect
        Box( // Background Box
            modifier = Modifier
                .offset(
                    x = 0.dp,
                    y = 0.dp
                )
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.5f)) // Transparent Color
        ) {
            FloatingActionButton(
                onClick = {
                    geofenceEntered.value = false;
                },
                modifier = Modifier
                    .absolutePadding(
                        left = 0.dp,
                        top = 0.dp
                    )
                    .fillMaxSize()
                    .align(Alignment.TopStart),
                containerColor = Color(0x00FFFFFF),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                )
            ) {

            }
        }

        Box( // Background Box
            modifier = Modifier
                .offset(
                    x = 40.dp,
                    y = 100.dp
                )
                .size(
                    width = 330.dp,
                    height = 60.dp
                )
                .background(Color.White, RoundedCornerShape(16.dp)),

            ) {

            // Acknowledge Button
            Text(
                modifier = Modifier
                    .offset(
                        x = 60.dp,
                        y = 20.dp,
                    )
                    .fillMaxSize(),
                text = "You've reached the destination!",
                style = TextStyle(
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left
            )
            FloatingActionButton(
                onClick = {
                    geofenceEntered.value = false;
                },
                modifier = Modifier
                    .absolutePadding(
                        left = 10.dp,
                        top = 10.dp
                    )
                    .size(
                        width = 40.dp,
                        height = 40.dp
                    )
                    .align(Alignment.TopStart)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Entered Geofence Destination");
            }
        }
    }
    // End Geofence Enter Notification

    // Geofence Enter Notification
    if (geofenceExited.value) {
        geofenceEntered.value = false;

        // Background Deselect
        Box( // Background Box
            modifier = Modifier
                .offset(
                    x = 0.dp,
                    y = 0.dp
                )
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.5f)) // Transparent Color
        ) {
            FloatingActionButton(
                onClick = {
                    geofenceExited.value = false;
                },
                modifier = Modifier
                    .absolutePadding(
                        left = 0.dp,
                        top = 0.dp
                    )
                    .fillMaxSize()
                    .align(Alignment.TopStart),
                containerColor = Color(0x00FFFFFF),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                )
            ) {

            }
        }

        Box( // Background Box
            modifier = Modifier
                .offset(
                    x = 50.dp,
                    y = 100.dp
                )
                .size(
                    width = 310.dp,
                    height = 60.dp
                )
                .background(Color.White, RoundedCornerShape(16.dp)),

            ) {

            // Acknowledge Button
            Text(
                modifier = Modifier
                    .offset(
                        x = 60.dp,
                        y = 20.dp,
                    )
                    .fillMaxSize(),
                text = "You've left the destination!",
                style = TextStyle(
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Left
            )
            FloatingActionButton(
                onClick = {
                    geofenceExited.value = false;
                },
                modifier = Modifier
                    .absolutePadding(
                        left = 10.dp,
                        top = 10.dp
                    )
                    .size(
                        width = 40.dp,
                        height = 40.dp
                    )
                    .align(Alignment.TopStart)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Left Geofence Destination");
            }
        }
    }
    // End Geofence Enter Notification

}