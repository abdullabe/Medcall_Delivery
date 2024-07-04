package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.adapter.AdapterActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper
import com.yoodobuzz.medcalldelivery.utils.SessionManager

class DeliveredPickUpActivity : AppCompatActivity() {

    lateinit var cardPickup: CardView
    lateinit var txtOrderId: TextView
    lateinit var txtItemName: TextView
    lateinit var txtQty: TextView
    lateinit var txtPrice: TextView
    lateinit var txtTotal: TextView
    lateinit var viewmodel: ActivityViewmodel
    lateinit var dialog: SweetAlertDialog
    var str_userId:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivered_pick_up)
        init()
        function()

    }

    fun init() {
        txtOrderId=findViewById(R.id.txtOrderId)
        txtQty=findViewById(R.id.txtQty)
        txtPrice=findViewById(R.id.txtPrice)
        txtTotal=findViewById(R.id.txtTotal)
        txtItemName=findViewById(R.id.txtItemName)
        cardPickup = findViewById(R.id.cardPickup)

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
                            txtQty.setText(response.data.cartItems.get(0).qty.toString())
                            txtPrice.setText("₹"+response.data.cartItems.get(0).totAmount)
                            txtTotal.setText("₹"+response.data.cartItems.get(0).totAmount)
                            txtItemName.setText(response.data.cartItems.get(0).productDetails!!.productName)


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
}