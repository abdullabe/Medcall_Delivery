package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.DisplayMetrics
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
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
import com.yoodobuzz.medcalldelivery.activity.deliveries.adapter.AdapterActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.activity.trackingmap.adapter.AdapterProduct
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.PlaceItem
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.addMarkerExt
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.haversine
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.setCustomMapStyle
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.setStartingZoomArea
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper
import com.yoodobuzz.medcalldelivery.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeliveredPickUpActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var cardPickup: CardView
    lateinit var txtOrderId: TextView
    lateinit var txtTotal: TextView
    lateinit var viewmodel: ActivityViewmodel
    lateinit var dialog: SweetAlertDialog
    var str_userId:String?=null
    lateinit var recProducts: RecyclerView
    lateinit var adapterProduct: AdapterProduct

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var deliveryPoint: LatLng? =null
    private var circle: Circle? = null
    private var bikeMarker: Marker? = null

    var previousLocation: Location? = null
    lateinit var currentPolyline: Polyline

    private val polylinePoints = mutableListOf<LatLng>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivered_pick_up)


        init()
        prepareRecyclerView()
        function()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }
    fun prepareRecyclerView(){
        recProducts.apply {
            val linearLayout =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recProducts.layoutManager = linearLayout
            recProducts.itemAnimator = DefaultItemAnimator()
            recProducts.adapter = adapterProduct
        }
    }

    fun init() {
        // Get the screen height in pixels
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels


        // Calculate 75% of the screen height
        val mapHeight = (screenHeight * 0.75).toInt()
        // Find the SupportMapFragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        // Set the height of the map to 75% of the screen
        val layoutParams = mapFragment.view?.layoutParams
        layoutParams?.height = mapHeight
        mapFragment.view?.layoutParams = layoutParams

        mapFragment.getMapAsync(this)


        txtOrderId=findViewById(R.id.txtOrderId)
        txtTotal=findViewById(R.id.txtTotal)
        cardPickup = findViewById(R.id.cardPickup)

        recProducts=findViewById(R.id.recProducts)
        adapterProduct = AdapterProduct()

    }

    fun function() {
        dialog= SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        viewmodel = ViewModelProvider(this)[ActivityViewmodel::class.java]


        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent= Intent(this@DeliveredPickUpActivity, DashboardActivity::class.java)
                startActivity(intent)
            }
        })
        val session= SessionManager(this)
        val user = session.getUserDetails()
        val email = user.get("email")
        str_userId = user.get("user_id").toString()

        viewmodel.getActivityData(str_userId!!)
        observeActivityViewmodel()

    }
    fun observeActivityViewmodel(){
        viewmodel.getActivityLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response :${response.data}")
                        if(response.data.cartItems.isNotEmpty()){
                            txtOrderId.setText(response.data.cartItems.get(0).orderId)
                            txtTotal.setText("₹"+response.data.cartItems.get(0).totAmount)

                            adapterProduct.setProductList(response.data.cartItems.get(0).products)
                            cardPickup.setOnClickListener {
                                val map = HashMap<String, String>()
                                map["order_id"] = response.data.cartItems.get(0).orderId.toString()
                                map["agent_id"] = str_userId.toString()
                                map["status"] = "pickup"
                                map["platform"] = "android"
                                Helper.showDialog(dialog)
                                viewmodel.acceptUserData(map)
                                observeAcceptLiveData()

                            }

                            val latLngParts = response.data.cartItems.get(0).store_lat!!.split(",")

// Extract latitude and longitude
                            val latitude = latLngParts[0].toDouble()
                            val longitude = latLngParts[1].toDouble()
                            deliveryPoint= LatLng(latitude,longitude)
                        }else{
                            println("### response data : ${response.data.message}")

                        }
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
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
                        val intent= Intent(this@DeliveredPickUpActivity,DeliveredActivity::class.java)
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

                        // Use coroutines to wait for the deliveryPoint to be set
                        CoroutineScope(Dispatchers.Main).launch {
                            val deliveryPointAsync = async { waitForDeliveryPoint() }
                            val deliveryPointLatLng = deliveryPointAsync.await()
                            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.pin)
                            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 140, 140, false)
                            val deliveryIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap) // Correct method to use
                            mMap.addMarker(MarkerOptions().position(deliveryPointLatLng!!).title("Delivery Point"))!!.setIcon(deliveryIcon)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))

                            // Draw the route between current location and delivery point
                            googleMap.addMarkerExt(START_LOCATION,this@DeliveredPickUpActivity)
                            addClusteredMarkers(mMap)


                            with(googleMap) {
                                setCustomMapStyle(
                                    context = this@DeliveredPickUpActivity, styleResId = R.raw.map_style
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
        // Declare previous location globally to track the user's movement

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        println("### location : $currentLatLng")

                        CoroutineScope(Dispatchers.Main).launch {
                            val deliveryPointAsync = async { waitForDeliveryPoint() }
                            val deliveryPointLatLng = deliveryPointAsync.await()
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