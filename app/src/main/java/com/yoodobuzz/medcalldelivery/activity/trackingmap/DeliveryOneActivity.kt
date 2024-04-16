package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.DeliveryActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.adapter.AdapterActivity.Companion.activityListDetails
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
        val activityList=activityListDetails
        println("### activity list : ${activityList}")

        txtName.setText(activityList.firstname+" "+activityList.lastname)
        txtOrderID.setText(activityList.orderId)
        txtStore.setText(activityList.storeName)
        txtDate.setText(convertDateFormat(activityList.date!!))
        txtDestination.setText(activityList.userAdd!!.building+","+activityList.userAdd!!.area+","+
                activityList.userAdd!!.landmark+","+activityList.userAdd!!.district+","+
                activityList.userAdd!!.state+","+
                activityList.userAdd!!.country+"-"+activityList.userAdd!!.pincode)
        txtQty.setText(activityList.productDetails!!.quantity.toString())
        txtPrice.setText(activityList.productDetails!!.price)
        txtTotal.setText(activityList.totAmount)
        txtPhNo.setText(activityList.phoneNumber.toString())
        txtItemName.setText(activityList.productDetails!!.productName)

        val session= SessionManager(this)
        val user = session.getUserDetails()
        val str_userId = user.get("user_id").toString()
        cardAccept.setOnClickListener {
            val map = HashMap<String, String>()
            map["order_id"] = activityList.orderId.toString()
            map["agent_id"] = str_userId.toString()
            map["status"] = "accept"
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
                map["order_id"] = activityList.orderId.toString()
                map["agent_id"] = str_userId.toString()
                map["status"] = "cancel"
                viewmodel.cancelUserData(map)
                observeCancelLiveData()
            }
            dialoge.show()


        }

    }
    fun observeCancelLiveData(){
        viewmodel.cancelUserLiveData.observe(this, Observer { response->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response ${response.data}")
                        Toast.makeText(this, response.data.message, Toast.LENGTH_SHORT).show()
                        val intent= Intent(this@DeliveryOneActivity,DeliveryActivity::class.java)
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