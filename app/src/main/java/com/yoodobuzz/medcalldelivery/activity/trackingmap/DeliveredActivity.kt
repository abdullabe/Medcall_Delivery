package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.util.IOUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.clustering.ClusterManager
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.GoogleViewmodel
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.PlaceItem
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.addMarkerExt
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.haversine
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.setCustomMapStyle
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.setStartingZoomArea
import com.yoodobuzz.medcalldelivery.network.ApiRequest
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.network.RetrofitInstance
import com.yoodobuzz.medcalldelivery.utils.Helper
import com.yoodobuzz.medcalldelivery.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class DeliveredActivity : AppCompatActivity() , OnMapReadyCallback {
    lateinit var cardDelivered:CardView
    lateinit var txtDestination:TextView
    lateinit var txtDeliverTime:TextView
    lateinit var viewmodel: ActivityViewmodel
    lateinit var dialog: SweetAlertDialog
    lateinit var orderId:String

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var deliveryPoint:LatLng? =null
    private var circle: Circle? = null

    private var bikeMarker: Marker? = null

    var previousLocation: Location? = null
     lateinit var currentPolyline: Polyline

    private val polylinePoints = mutableListOf<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivered)
        viewmodel = ViewModelProvider(this)[ActivityViewmodel::class.java]
        init()
        function()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }
    fun init(){
        cardDelivered=findViewById(R.id.cardDelivered)
        txtDestination=findViewById(R.id.txtDestination)
        txtDeliverTime=findViewById(R.id.txtDeliverTime)

    }
    fun function(){
        dialog= SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)

        val session= SessionManager(this)
        val user = session.getUserDetails()
        val str_userId = user.get("user_id").toString()

        Helper.showDialog(dialog)
        viewmodel.getActivityData(str_userId)
        observeActivityViewmodel(dialog)
        cardDelivered.setOnClickListener {
            val map = HashMap<String, String>()
            map["order_id"] = orderId.toString()
            map["agent_id"] = str_userId.toString()
            map["status"] = "delivered"
            map["platform"] = "android"

            Helper.showDialog(dialog)
            viewmodel.acceptUserData(map)
            observeAcceptLiveData()

            val intent= Intent(this@DeliveredActivity,DeliverySuccessActivity::class.java)
            startActivity(intent)
        }
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent= Intent(this@DeliveredActivity,DashboardActivity::class.java)
                startActivity(intent)
            }
        })
    }
    fun observeActivityViewmodel(dialog: SweetAlertDialog){
        viewmodel.getActivityLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response delivery data:${response.data}")
                        if(response.data.cartItems.isNotEmpty()){
                            val activityList = response.data.cartItems.get(0)
                            orderId=activityList.orderId.toString()
                            strorder_id=orderId.toString()
                            txtDestination.setText(
                                activityList.userAdd!!.address_detail
                            )
                            val latLngParts = activityList.userAdd!!.lat!!.split(",")

// Extract latitude and longitude
                            val latitude = latLngParts[0].toDouble()
                            val longitude = latLngParts[1].toDouble()
                            deliveryPoint= LatLng(latitude,longitude)



                        }else{
                            println("### response data : ${response.data.message}")
                        }
                        Helper.hideDialog(dialog)
                    }
                }
                is Resource.Loading -> {
                    Helper.showDialog(dialog)
                }
                is Resource.Error -> {
                    Helper.hideDialog(dialog)
                    val errorMessage = response.message ?: "An error occurred"
                    println("### error message : ${errorMessage}")

                }
            }
        })

    }
    fun observeAcceptLiveData(){
        viewmodel.acceptUserLiveData.observe(this, Observer { response->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response ${response.data}")
                        Toast.makeText(this, response.data.message, Toast.LENGTH_SHORT).show()
                        val intent= Intent(this@DeliveredActivity,DeliverySuccessActivity::class.java)
                        startActivity(intent)
                    }
                    Helper.hideDialog(dialog)
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    Helper.hideDialog(dialog)
                    val errorMessage = response.message ?: "An error occurred"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

        })

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            mMap.isMyLocationEnabled = false
            startLocationUpdates()
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {

                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        val START_LOCATION = currentLatLng

                        CoroutineScope(Dispatchers.Main).launch {
                            val deliveryPointAsync = async { waitForDeliveryPoint() }
                            val deliveryPointLatLng = deliveryPointAsync.await()

                            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.pin)
                            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 140, 140, false)
                            val deliveryIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap) // Correct method to use
                            mMap.addMarker(MarkerOptions().position(deliveryPoint!!).title("Delivery Point"))!!.setIcon(deliveryIcon)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))


                            // Draw the route between current location and delivery point
//                            drawRoute(currentLatLng, deliveryPointLatLng!!)
                            googleMap.addMarkerExt(START_LOCATION,this@DeliveredActivity)
                            addClusteredMarkers(mMap)
                            checkProximityToDeliveryPoint(currentLatLng, deliveryPointLatLng!!)

                            with(googleMap) {
                                setCustomMapStyle(
                                    context = this@DeliveredActivity, styleResId = R.raw.map_style
                                )
                                setStartingZoomArea(
                                    startLatLng = START_LOCATION, deliveryPointLatLng!!
                                )
                            }
                        }
                    }
                }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }
    private fun checkProximityToDeliveryPoint(currentLatLng: LatLng, deliveryPoint: LatLng) {
        val results = FloatArray(1)
        Location.distanceBetween(
            currentLatLng.latitude, currentLatLng.longitude,
            deliveryPoint.latitude, deliveryPoint.longitude,
            results
        )
        val distanceInMeters = results[0]

        val proximityRadius = 50f // Example proximity threshold
        if (distanceInMeters <= proximityRadius) {
            // Delivery boy has reached the destination
            txtDeliverTime.text = "Arrived"  // Or some other meaningful text
            onDeliverySuccess()
        } else {
            // Keep updating delivery time if still on the way
            lifecycleScope.launch {
                val travelTime =  GoogleViewmodel.fetchTravelTime(currentLatLng,deliveryPoint)

                if (travelTime != null) {
                    txtDeliverTime.text = "$travelTime"
                } else {
                    txtDeliverTime.text = "time duration fetching.."
                }
            }
        }
    }


    private fun onDeliverySuccess() {
        // Show a success message
        Toast.makeText(this, "Delivery Successful!", Toast.LENGTH_LONG).show()

        // Navigate to a success screen
        val intent = Intent(this@DeliveredActivity, DeliverySuccessActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
                // Re-call map setup here if needed
            }
        }
    }

    private fun drawRoute(currentLatLng: LatLng, destinationLatLng: LatLng) {
        val origin = "${currentLatLng.latitude},${currentLatLng.longitude}"
        val destination = "${destinationLatLng.latitude},${destinationLatLng.longitude}"

        // Google Directions API URL
        val directionsUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&key=AIzaSyCkRSn-ZXBqRN5Qm2cDL3Wz1hz2xftOft4"

        // Make a network request to fetch the directions
        val requestQueue = Volley.newRequestQueue(this)
        val directionsRequest = JsonObjectRequest(
            Request.Method.GET,
            directionsUrl,
            null,
            { response ->
                val routes = response.getJSONArray("routes")
                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                    val decodedPath = PolyUtil.decode(points)

                    // Clear the existing polyline points and add the new ones
                    polylinePoints.clear()
                    polylinePoints.addAll(decodedPath)

                    // Check if polyline exists, if not create a new one
                    if (::currentPolyline.isInitialized) {
                        // Update the polyline's points
                        currentPolyline.points = polylinePoints
                    } else {
                        // Create the polyline for the first time
                        val polylineOptions = PolylineOptions()
                            .addAll(polylinePoints)
                            .width(20f)
                            .color(ContextCompat.getColor(this, R.color.purple))

                        currentPolyline = mMap.addPolyline(polylineOptions)
                    }
                }
            },
            { error ->
                // Handle the error
                error.printStackTrace()
            }
        )
        requestQueue.add(directionsRequest)
    }
    private fun addClusteredMarkers(googleMap: GoogleMap) {
        // Create the ClusterManager class and set the custom renderer
        val clusterManager = ClusterManager<PlaceItem>(this, googleMap)
        // Set custom info window adapter
        clusterManager.cluster()

        // Show polygon
        clusterManager.setOnClusterItemClickListener { item ->
            addCircle(googleMap, item)
            return@setOnClusterItemClickListener false
        }

        // When the camera starts moving, change the alpha value of the marker to translucent
        googleMap.setOnCameraMoveStartedListener {
            clusterManager.getMarkerCollection().markers.forEach { it.alpha = 0.3f }
            clusterManager.getClusterMarkerCollection().markers.forEach { it.alpha = 0.3f }
        }

        googleMap.setOnCameraIdleListener {
            // When the camera stops moving, change the alpha value back to opaque
            clusterManager.getMarkerCollection().markers.forEach { it.alpha = 1.0f }
            clusterManager.getClusterMarkerCollection().markers.forEach { it.alpha = 1.0f }

            // Call clusterManager.onCameraIdle() when the camera stops moving so that re-clustering
            // can be performed when the camera stops moving
            clusterManager.onCameraIdle()
        }
    }

    /**
     * Adds a [Circle] around the provided [item]
     */
    private fun addCircle(googleMap: GoogleMap, item: PlaceItem) {
        circle?.remove()
        circle = googleMap.addCircle(
            CircleOptions()
                .center(item.position)
                .radius(10000.0)
                .fillColor(ContextCompat.getColor(this, R.color.green_500))
                .strokeColor(ContextCompat.getColor(this, R.color.green_500))
        )
    }
    companion object{
        var strorder_id =""
    }
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // Update every 5 seconds
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        println("### location : ${currentLatLng}")

                        CoroutineScope(Dispatchers.Main).launch {
                            val deliveryPointAsync = async { waitForDeliveryPoint() }
                            val deliveryPointLatLng = deliveryPointAsync.await()
                            checkProximityToDeliveryPoint(currentLatLng, deliveryPointLatLng)
                            drawRoute(currentLatLng, deliveryPointLatLng)
                        }

                        // Custom bike icon for the current location
                        val bikeBitmap = BitmapFactory.decodeResource(resources, R.drawable.bike)
                        val resizedBike = Bitmap.createScaledBitmap(bikeBitmap, 100, 100, false)
                        val bikeIcon = BitmapDescriptorFactory.fromBitmap(resizedBike)

                        // Calculate the bearing if the previous location is available
                        var bearing = 0f
                        if (previousLocation != null) {
                            bearing = previousLocation!!.bearingTo(it)
                        }

                        // Update or create the bike marker
                        if (bikeMarker == null) {
                            // Add the marker for the first time
                            bikeMarker = mMap.addMarker(
                                MarkerOptions()
                                    .position(currentLatLng)
                                    .icon(bikeIcon)
                                    .rotation(bearing) // Apply rotation here
                                    .flat(true)        // Set flat so that the marker rotates smoothly
                                    .title("Current Location")
                            )
                        } else {
                            // Move the marker to the new location
                            bikeMarker?.position = currentLatLng
                            bikeMarker?.rotation = bearing // Update the rotation with the new bearing
                        }

                        // Update the previous location for the next bearing calculation
                        previousLocation = it

                    }
                }
            },
            Looper.getMainLooper()
        )
    }
    private suspend fun waitForDeliveryPoint(): LatLng {
        return withContext(Dispatchers.IO) {
            while (deliveryPoint == null) {
                delay(100) // Wait for 100 ms before checking again
            }
            deliveryPoint!! // Once set, return the deliveryPoint
        }
    }
}