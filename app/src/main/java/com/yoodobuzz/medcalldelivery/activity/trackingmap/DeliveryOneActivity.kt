package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.app.Dialog
import android.content.Intent
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
import cn.pedant.SweetAlert.SweetAlertDialog
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.DeliveryActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper
import com.yoodobuzz.medcalldelivery.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Locale

class DeliveryOneActivity : AppCompatActivity() {
    lateinit var cardAccept:CardView
    lateinit var card_decline:CardView
    lateinit var txtName:TextView
    lateinit var txtOrderID:TextView
    lateinit var txtStore:TextView
    lateinit var txtDate:TextView
    lateinit var txtDestination:TextView
    lateinit var txtQty:TextView
    lateinit var txtPrice:TextView
    lateinit var txtTotal:TextView
    lateinit var txtItemName:TextView
    lateinit var txtPhNo:TextView
    lateinit var viewmodel: ActivityViewmodel
    lateinit var dialog: SweetAlertDialog
    lateinit var orderId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_one)
        init()
        function()
               
    }
    fun init(){
        cardAccept=findViewById(R.id.cardAccept)
        card_decline=findViewById(R.id.card_decline)
        txtName=findViewById(R.id.txtName)
        txtOrderID=findViewById(R.id.txtOrderID)
        txtStore=findViewById(R.id.txtStore)
        txtDate=findViewById(R.id.txtDate)
        txtDestination=findViewById(R.id.txtDestination)
        txtQty=findViewById(R.id.txtQty)
        txtPrice=findViewById(R.id.txtPrice)
        txtTotal=findViewById(R.id.txtTotal)
        txtPhNo=findViewById(R.id.txtPhNo)
        txtItemName=findViewById(R.id.txtItemName)

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
                            txtDestination.setText(activityList.userAdd!!.building+","+activityList.userAdd!!.area+","+
                                    activityList.userAdd!!.landmark+","+activityList.userAdd!!.district+","+
                                    activityList.userAdd!!.state+","+
                                    activityList.userAdd!!.country+"-"+activityList.userAdd!!.pincode)
                            txtQty.setText(activityList.qty.toString())
                            txtPrice.setText("₹${activityList.totAmount?.toDoubleOrNull()?.toInt() ?: 0}")
                            txtTotal.setText("₹${activityList.totAmount?.toDoubleOrNull()?.toInt() ?: 0}")
                            txtPhNo.setText(activityList.phoneNumber.toString())
                            txtItemName.setText(activityList.productDetails!!.productName)


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
    fun convertDateFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }
}