package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity

class DeliverySuccessActivity : AppCompatActivity() {
    lateinit var cardDelivered:CardView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_success)

        cardDelivered=findViewById(R.id.cardDelivered)

        cardDelivered.setOnClickListener {
            val intent= Intent(this@DeliverySuccessActivity,DashboardActivity::class.java)
            startActivity(intent)
        }
    }


}