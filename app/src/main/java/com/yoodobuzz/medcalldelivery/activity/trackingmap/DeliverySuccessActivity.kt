package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.login.viewmodel.LoginViewmodel
import com.yoodobuzz.medcalldelivery.utils.SessionManager

class DeliverySuccessActivity : AppCompatActivity() {
    lateinit var cardDelivered:CardView
    lateinit var txtName:TextView
    lateinit var txtOrder_id:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_success)
        init()
        function()


        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent= Intent(this@DeliverySuccessActivity,DashboardActivity::class.java)
                startActivity(intent)
            }
        })

        cardDelivered.setOnClickListener {
            val intent= Intent(this@DeliverySuccessActivity,DashboardActivity::class.java)
            startActivity(intent)
        }
    }
    fun init(){
        cardDelivered=findViewById(R.id.cardDelivered)
        txtName=findViewById(R.id.txtName)
        txtOrder_id=findViewById(R.id.txtOrderId)
    }

fun function(){
   val session= SessionManager(this)
    val user = session!!.getUserDetails()
   val agentname = user.get("agentname").toString()

    println("### agentname : ${agentname}")
    txtName.setText("Welldone ${agentname}!")
    txtOrder_id.setText("You just delivered order ${DeliveredActivity.strorder_id}")

}
}