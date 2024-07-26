package com.yoodobuzz.medcalldelivery.activity.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveredActivity
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveryOneActivity
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveryTwoActivity

class SplashActivity : AppCompatActivity() {
    private var back_pressed: Long = 0
    private var parent_view: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        parent_view = findViewById(android.R.id.content)
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (back_pressed + 2000 > System.currentTimeMillis()) {
                    finishAffinity()
                } else {
                    if (parent_view != null) {
                        Snackbar.make(parent_view!!, "Press once again to exit!", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
                back_pressed = System.currentTimeMillis()
            }
        })

        val bundle = intent.extras
        if (bundle != null && bundle["page"] != null) {

            val page = bundle["page"].toString()
            println("### data : ${page}")
            if(page.equals("1")){
                val i = Intent(this@SplashActivity, DeliveryOneActivity::class.java)
                startActivity(i)
            }else if(page.equals("2")){
                val i = Intent(this@SplashActivity, DeliveryTwoActivity::class.java)
                startActivity(i)
            }else if (page.equals("3")){
                val i = Intent(this@SplashActivity, DeliveryTwoActivity::class.java)
                startActivity(i)
            }else if (page.equals("4")){
                val i = Intent(this@SplashActivity, DeliveredActivity::class.java)
                startActivity(i)
            }
        }else{
            Handler().postDelayed({
                val intent= Intent(this@SplashActivity,DashboardActivity::class.java)
                startActivity(intent)
            },2000L)

        }
    }
}