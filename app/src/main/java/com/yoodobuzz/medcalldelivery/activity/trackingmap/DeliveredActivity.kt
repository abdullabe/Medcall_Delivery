package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper
import com.yoodobuzz.medcalldelivery.utils.SessionManager
import okhttp3.internal.userAgent

class DeliveredActivity : AppCompatActivity() {
    lateinit var cardDelivered:CardView
    lateinit var txtDestination:TextView
    lateinit var viewmodel: ActivityViewmodel
    lateinit var dialog: SweetAlertDialog
    lateinit var orderId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivered)
        viewmodel = ViewModelProvider(this)[ActivityViewmodel::class.java]
        init()
        function()


    }
    fun init(){
        cardDelivered=findViewById(R.id.cardDelivered)
        txtDestination=findViewById(R.id.txtDestination)

    }
    fun function(){
        dialog= SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)

        val session= SessionManager(this)
        val user = session.getUserDetails()
        val str_userId = user.get("user_id").toString()

        viewmodel.getActivityData(str_userId)
        observeActivityViewmodel()
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
    fun observeActivityViewmodel(){
        viewmodel.getActivityLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response :${response.data}")
                        if(response.data.cartItems.isNotEmpty()){
                            val activityList = response.data.cartItems.get(0)
                            orderId=activityList.orderId.toString()
                            strorder_id=orderId.toString()
                            txtDestination.setText(
                                activityList.userAdd!!.building + "," + activityList.userAdd!!.area + "," +
                                        activityList.userAdd!!.landmark + "," + activityList.userAdd!!.district + "," +
                                        activityList.userAdd!!.state + "," +
                                        activityList.userAdd!!.country + "-" + activityList.userAdd!!.pincode
                            )

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
    companion object{
        var strorder_id =""
    }

}