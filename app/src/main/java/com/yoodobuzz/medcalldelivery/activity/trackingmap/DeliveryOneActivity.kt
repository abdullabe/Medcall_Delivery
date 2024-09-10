package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.DeliveryActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.activity.trackingmap.adapter.AdapterProduct
import com.yoodobuzz.medcalldelivery.activity.trackingmap.googlemap.addMarkerExt
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
import java.text.SimpleDateFormat
import java.util.Locale

class DeliveryOneActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var cardAccept:CardView
    lateinit var card_decline:CardView
    lateinit var txtName:TextView
    lateinit var txtOrderID:TextView
    lateinit var txtStore:TextView
    lateinit var txtDate:TextView
    lateinit var txtDestination:TextView
    lateinit var txtTotal:TextView
    lateinit var txtPhNo:TextView
    lateinit var viewmodel: ActivityViewmodel
    lateinit var dialog: SweetAlertDialog
    lateinit var orderId:String
    lateinit var recProducts: RecyclerView
    lateinit var adapterProduct: AdapterProduct

    private lateinit var mMap: GoogleMap
    private var deliveryPoint:LatLng? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_one)
        init()
        prepareRecyclerView()
        function()
               
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
    fun init(){
        cardAccept=findViewById(R.id.cardAccept)
        card_decline=findViewById(R.id.card_decline)
        txtName=findViewById(R.id.txtName)
        txtOrderID=findViewById(R.id.txtOrderID)
        txtStore=findViewById(R.id.txtStore)
        txtDate=findViewById(R.id.txtDate)
        txtDestination=findViewById(R.id.txtDestination)
        txtTotal=findViewById(R.id.txtTotal)
        txtPhNo=findViewById(R.id.txtPhNo)

        recProducts=findViewById(R.id.recProducts)
        adapterProduct = AdapterProduct()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    fun function(){
        dialog= SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        viewmodel = ViewModelProvider(this)[ActivityViewmodel::class.java]

        val session= SessionManager(this)
        val user = session.getUserDetails()
        val str_userId = user.get("user_id").toString()
        viewmodel.getActivityData(str_userId)
        observeActivityViewmodel()
        cardAccept.setOnClickListener {
            val map = HashMap<String, String>()
            map["order_id"] = orderId.toString()
            map["agent_id"] = str_userId.toString()
            map["status"] = "accept"
            map["platform"] = "android"
            Helper.showDialog(dialog)
            viewmodel.acceptUserData(map)
            observeAcceptLiveData()

        }
        card_decline.setOnClickListener{
            val dialoge = Dialog(this@DeliveryOneActivity)
            dialoge.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialoge.setContentView(R.layout.dialog_app_version)
            dialoge.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialoge.setCancelable(true)
            val btnDecline = dialoge.findViewById<Button>(R.id.btnDecline)
            btnDecline.setOnClickListener {
                val map = HashMap<String, String>()
                map["order_id"] =orderId.toString()
                map["agent_id"] = str_userId.toString()
                map["status"] = "cancel"
                viewmodel.cancelUserData(map)
                observeCancelLiveData()
            }
            dialoge.show()


        }

    }
    fun observeActivityViewmodel(){
        viewmodel.getActivityLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response :${response.data}")
                        if(response.data.cartItems.isNotEmpty()){
                            val activityList=response.data.cartItems.get(0)
                            println("### activity list : ${activityList}")
                            orderId=activityList.orderId.toString()
                            txtName.setText(activityList.firstname+" "+activityList.lastname)
                            txtOrderID.setText(activityList.orderId)
                            txtStore.setText(activityList.storeName)
                            txtDate.setText(convertDateFormat(activityList.date!!))
                            txtDestination.setText(activityList.userAdd!!.address_detail)
                            txtTotal.setText("₹${activityList.totAmount?.toDoubleOrNull()?.toInt() ?: 0}")
                            txtPhNo.setText(activityList.phoneNumber.toString())
                            adapterProduct.setProductList(activityList.products)


                            val latLngParts = activityList.store_lat!!.split(",")

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
    fun observeCancelLiveData(){
        viewmodel.cancelUserLiveData.observe(this, Observer { response->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response ${response.data}")
                        Toast.makeText(this, response.data.message, Toast.LENGTH_SHORT).show()
                        val intent= Intent(this@DeliveryOneActivity,DashboardActivity::class.java)
                        startActivity(intent)
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    val errorMessage = response.message ?: "An error occurred"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
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
                        val intent= Intent(this@DeliveryOneActivity,DeliveryTwoActivity::class.java)
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
                Toast.makeText(this@DeliveryOneActivity, "Delivery point is not available", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun convertDateFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }
}