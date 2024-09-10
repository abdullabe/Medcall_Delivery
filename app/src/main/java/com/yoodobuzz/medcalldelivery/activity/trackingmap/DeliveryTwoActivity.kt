package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.GoogleViewmodel
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.addMarkerExt
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

class DeliveryTwoActivity : AppCompatActivity(), OnMapReadyCallback, SwipeRefreshLayout.OnRefreshListener {
    lateinit var cardPickUp: CardView
    lateinit var txtDestination: TextView
    lateinit var txtPhNo: TextView
    lateinit var txtWait: TextView
    lateinit var txtDeliverTime: TextView
    lateinit var viewmodel: ActivityViewmodel
    lateinit var dialog: SweetAlertDialog
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var mMap: GoogleMap
    private var deliveryPoint: LatLng? =null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_two)

        init()
        function()
    }

    fun init() {
        viewmodel = ViewModelProvider(this)[ActivityViewmodel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        dialog=SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        cardPickUp = findViewById(R.id.cardPickUp)
        txtWait = findViewById(R.id.txtWait)
        txtPhNo = findViewById(R.id.txtPhNo)
        txtDeliverTime = findViewById(R.id.txtDeliverTime)
        txtDestination = findViewById(R.id.txtDestination)
        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)

        val session= SessionManager(this)
        val user = session.getUserDetails()
        val email = user.get("email")

        println("### email : ${email}")
        val str_userId = user.get("user_id").toString()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        Helper.showDialog(dialog)
        viewmodel.getActivityData(str_userId)
        observeActivityViewmodel()
    }
    fun observeActivityViewmodel(){
        viewmodel.getActivityLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response :${response.data}")
                        if(response.data.cartItems.isNotEmpty()){
                            val activityList = response.data.cartItems.get(0)
                            txtDestination.setText(
                                activityList.userAdd!!.address_detail
                            )
                            val phno=response.data.cartItems.get(0).phoneNumber.toString()

                            txtPhNo.setText(phno)
                            if(response.data.cartItems.get(0).phoneNumber.toString().isNotEmpty()){
                                txtPhNo.setOnClickListener{
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phno")
                                    }
                                    startActivity(intent)
                                }
                            }
                            if(response.data.cartItems.get(0).status.equals("accept")){
                                Toast.makeText(this, "please wait store will approved shortly", Toast.LENGTH_SHORT).show()
                                txtWait.isVisible=true
                                cardPickUp.isVisible=false
                            }else{
                                txtWait.isVisible=false
                                cardPickUp.isVisible=true
                            }

                            val latLngParts = activityList.store_lat!!.split(",")

// Extract latitude and longitude
                            val latitude = latLngParts[0].toDouble()
                            val longitude = latLngParts[1].toDouble()
                            deliveryPoint= LatLng(latitude,longitude)
                            cardPickUp.setOnClickListener {
                                if(response.data.cartItems.get(0).status.equals("accept")){
                                    Toast.makeText(this, "please wait store will approved shortly", Toast.LENGTH_SHORT).show()
                                }else{
                                    val intent= Intent(this@DeliveryTwoActivity,DeliveredPickUpActivity::class.java)
                                    startActivity(intent)
                                }

                            }

                        }
                        Helper.hideDialog(dialog)
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    Helper.hideDialog(dialog)
                    val errorMessage = response.message ?: "An error occurred"
                }
            }
        })

    }

    fun function() {
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent= Intent(this@DeliveryTwoActivity, DashboardActivity::class.java)
                startActivity(intent)
            }
        })
    }

    override fun onRefresh() {
        loadData()
    }
    private fun loadData() {
        android.os.Handler().postDelayed({
            swipeRefreshLayout.isRefreshing = false
            function()
        }, 1000)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        CoroutineScope(Dispatchers.Main).launch {
            // Wait for deliveryPoint to be set
            withContext(Dispatchers.IO) {
                while (deliveryPoint == null) {
                    delay(100) // Wait for 100 ms
                }
            }

            if (deliveryPoint != null) {
                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.pin)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 140, 140, false)
                val deliveryIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap) // Correct method to use
                mMap.addMarker(MarkerOptions().position(deliveryPoint!!).title("Delivery Point"))!!.setIcon(deliveryIcon)

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(deliveryPoint!!, 14f))


            } else {
                Toast.makeText(this@DeliveryTwoActivity, "Delivery point is not available", Toast.LENGTH_SHORT).show()
            }

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            mMap.isMyLocationEnabled = false
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {

                        CoroutineScope(Dispatchers.Main).launch {
                            val deliveryPointAsync = async { waitForDeliveryPoint() }
                            val deliveryPointLatLng = deliveryPointAsync.await()

                            val currentLatLng = LatLng(it.latitude, it.longitude)
                            lifecycleScope.launch {
                                val travelTime =  GoogleViewmodel.fetchTravelTime(currentLatLng,deliveryPointLatLng!!)

                                if (travelTime != null) {
                                    txtDeliverTime.text = "$travelTime"
                                } else {
                                    txtDeliverTime.text = "time duration fetching.."
                                }
                        }

                    }
                }
        }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
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

