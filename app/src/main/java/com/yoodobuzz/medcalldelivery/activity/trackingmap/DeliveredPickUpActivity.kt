package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivered_pick_up)
        init()
        prepareRecyclerView()
        function()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

                            val latLngParts = response.data.cartItems.get(0).userAdd!!.lat!!.split(",")

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

            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        val START_LOCATION = currentLatLng
                        mMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))

                        // Use coroutines to wait for the deliveryPoint to be set
                        CoroutineScope(Dispatchers.Main).launch {
                            // Wait for deliveryPoint to be set
                            withContext(Dispatchers.IO) {
                                while (deliveryPoint == null) {
                                    delay(100) // Wait for 100 ms
                                }
                            }

                            if (deliveryPoint != null) {
                                mMap.addMarker(MarkerOptions().position(deliveryPoint!!).title("Delivery Point"))
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))

                                // Draw the route between current location and delivery point
                                drawRoute(currentLatLng, deliveryPoint!!)
                                googleMap.addMarkerExt(START_LOCATION)
                                addClusteredMarkers(mMap)

                                with(googleMap) {
                                    setCustomMapStyle(
                                        context = this@DeliveredPickUpActivity, styleResId = R.raw.map_style
                                    )
                                    setStartingZoomArea(
                                        startLatLng = START_LOCATION, deliveryPoint!!
                                    )
                                }
                            } else {
                                Toast.makeText(this@DeliveredPickUpActivity, "Delivery point is not available", Toast.LENGTH_SHORT).show()
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

                    val polylineOptions = PolylineOptions()
                        .addAll(decodedPath)
                        .width(10f)
                        .color(ContextCompat.getColor(this, R.color.purple))

                    mMap.addPolyline(polylineOptions)
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
}