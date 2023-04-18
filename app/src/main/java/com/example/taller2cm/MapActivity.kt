package com.example.taller2cm

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.preference.PreferenceManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.taller2cm.databinding.ActivityMapBinding
import com.example.taller2cm.pojo.MyLocation
import com.google.android.gms.location.*
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import java.io.*
import java.util.*


class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding
    private  lateinit var map: MapView
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mCurrentLocation: Location
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var longPressedMarker: Marker
    private lateinit var uLocation: GeoPoint


    private val REQUEST_LOCATION_PERMISSION = 1

    val latitude = 4.702650
    val longitude = -74.048385
    val startPoint = GeoPoint(latitude, longitude)
    val bogota = GeoPoint(4.62, -74.07)

    private lateinit var roadManager:RoadManager

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        roadManager = OSRMRoadManager(baseContext, "ANDROID")



        val ctx = applicationContext
        setContentView(R.layout.activity_map)
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        map = findViewById<MapView>(R.id.osmMap)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        map.overlays.add(createOverlayEvents())
        // Get a reference to the SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Get a reference to the light sensor
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Register a listener for the light sensor
        sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)


        val myEditText = findViewById<EditText>(R.id.locationEditText)


        myEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val addressString = myEditText.text.toString()
                if (addressString.isNotEmpty()) {
                    try {
                        val addresses = Geocoder(this).getFromLocationName(addressString, 2)

                        if (addresses!!.size > 0) {
                            val address = addresses[0]
                            val lat = address.latitude
                            val lon = address.longitude
                            val marker = Marker(map)

                            marker.position = GeoPoint(lat, lon)
                            marker.title = address.getAddressLine(0)
                            map.overlays.add(marker)
                            map.controller.setCenter(GeoPoint(lat, lon))

                            drawRoute(GeoPoint(mCurrentLocation),marker.position)
                        } else {
                            Toast.makeText(applicationContext, "Address not found", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, "Error: " + e.message, Toast.LENGTH_LONG).show()
                    }
                }
                return@setOnEditorActionListener true
            } else {
                return@setOnEditorActionListener false
            }
        }

        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val userLocation = GeoPoint(location.latitude, location.longitude)
                    val userMarker = Marker(map)
                    uLocation =userLocation

                    userMarker.position = userLocation
                    userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    map.overlays.add(userMarker)
                    val mapController = map.controller
                    mapController.animateTo(userLocation)

                }
            }

        mLocationRequest = LocationRequest.create()
            .setInterval(100)
            .setFastestInterval(5000)
            .setSmallestDisplacement(30f)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mCurrentLocation = locationResult.lastLocation!!
                uLocation.setCoords(locationResult.lastLocation!!.latitude,locationResult.lastLocation!!.longitude)
                //Toast.makeText(applicationContext, mCurrentLocation.toString(), Toast.LENGTH_LONG).show()
                writeJSONObject()
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        val myButton = findViewById<Button>(R.id.button)

        myButton.setOnClickListener {
            readJSONAndDrawRoutes()

        }





    }
    private fun createOverlayEvents(): MapEventsOverlay {
        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }


            override fun longPressHelper(p: GeoPoint): Boolean {
                longPressOnMap(p)
                showDistanceToMarker()
                drawRoute(uLocation,p)
                return true
            }
        }
        return MapEventsOverlay(receiver)
    }



    private fun writeJSONObject() {
        val myLocation = MyLocation()
        myLocation.fecha = Date(System.currentTimeMillis()).toString()
        myLocation.latitud = mCurrentLocation.latitude.toString()
        myLocation.longitud = mCurrentLocation.longitude.toString()

        val filename = "locations.json"
        val file = File(getExternalFilesDir(null), filename)

        try {
            // Read the existing JSON array from the file, or create a new one if the file doesn't exist
            val json = if (file.exists()) {
                JSONObject(file.readText()).getJSONArray("locations")
            } else {
                JSONArray()
            }

            // Append the new location to the JSON array
            json.put(myLocation.toJSON())

            // Write the updated JSON array to the file
            file.writeText(JSONObject().put("locations", json).toString())

            Toast.makeText(applicationContext, "Location saved", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Log error
        }
    }

    private fun longPressOnMap(p: GeoPoint) {



        longPressedMarker = createMarkerFromGeoPoint(p)
        longPressedMarker.let {
            map.overlays.add(it)
        }




    }

    private fun readJSONAndDrawRoutes() {
        val filename = "locations.json"
        val file = File(getExternalFilesDir(null), filename)

        try {
            // Read the JSON array from the file
            val json = JSONObject(file.readText()).getJSONArray("locations")

            // Draw route for each pair of consecutive points in the JSON array, starting from uLocation
            var startPoint = uLocation
            for (i in 0 until json.length()) {
                val endPoint = GeoPoint(json.getJSONObject(i).getString("latitud").toDouble(), json.getJSONObject(i).getString("longitud").toDouble())
                drawRouteBlue(startPoint, endPoint)
                startPoint = endPoint
            }
        } catch (e: Exception) {
            // Log error
        }
    }





    private var routeOverlay: Polyline? = null

    private fun drawRoute(startPoint: GeoPoint, endPoint: GeoPoint) {
        // Remove existing route overlay, if any
        if (routeOverlay != null) {
            map.overlays.remove(routeOverlay)
        }

        // Get the road from the RoadManager
        val road = roadManager.getRoad(arrayListOf(startPoint, endPoint))

        // Create a new route overlay and add it to the map
        routeOverlay = RoadManager.buildRoadOverlay(road, Color.RED, 12f)
        map.overlays.add(routeOverlay)
        map.invalidate()
    }

    private fun drawRouteBlue(startPoint: GeoPoint, endPoint: GeoPoint) {
        // Remove existing route overlay, if any
//        if (routeOverlay != null) {
//            map.overlays.remove(routeOverlay)
//        }

        // Get the road from the RoadManager
        val road = roadManager.getRoad(arrayListOf(startPoint, endPoint))

        // Create a new route overlay and add it to the map
        routeOverlay = RoadManager.buildRoadOverlay(road, Color.BLUE, 12f)
        map.overlays.add(routeOverlay)
        map.invalidate()
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor








    override fun onResume() {
        super.onResume()
        val mapController = map.controller




        mapController.zoomTo(18.0)
        mapController.setCenter(startPoint)
        mapController.animateTo(bogota)
        getSystemService(Context.UI_MODE_SERVICE) as UiModeManager




    }



    override fun onPause() {
        super.onPause()
        map.onPause()
        stopLocationUpdates()
        sensorManager.unregisterListener(lightSensorListener)
    }

    private val lightSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Do nothing
        }

        override fun onSensorChanged(event: SensorEvent?) {
            // Check if the sensor type is the light sensor
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                // Get the luminance value from the sensor event
                val luminance = event.values[0]

                // Use the luminance value to adjust the map color filter
                if (luminance < 10) {
                    map.overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
                } else {
                    map.overlayManager.tilesOverlay.setColorFilter(null)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mFusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    private fun createMarkerFromGeoPoint(geopoint: GeoPoint): Marker {
        val geocoder = Geocoder(baseContext, Locale.getDefault())
        val addresses = geocoder.getFromLocation(geopoint.latitude, geopoint.longitude, 1)
        val address = addresses!![0]
        val marker = Marker(map)
        marker.position = geopoint
        marker.title = address.getAddressLine(0)
        marker.snippet = "Lat: ${geopoint.latitude}, Lon: ${geopoint.longitude}"
        return marker
    }
    private fun showDistanceToMarker() {
        // Get the last known location
        val lastLocation = mCurrentLocation

        // Get the marker position
        val markerPosition = longPressedMarker.position

        // Create a new location object for the marker position
        val markerLocation = Location("").apply {
            latitude = markerPosition.latitude
            longitude = markerPosition.longitude
        }

        // Calculate the distance between the last location and the marker location
        val distance = lastLocation.distanceTo(markerLocation)

        // Display the distance in a Toast message
        Toast.makeText(this, "Distance to marker: $distance meters", Toast.LENGTH_LONG).show()
    }



}




