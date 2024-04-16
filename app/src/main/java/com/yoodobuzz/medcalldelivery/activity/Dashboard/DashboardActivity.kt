package com.yoodobuzz.medcalldelivery.activity.Dashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.account.MyAccountActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.DeliveryActivity
import com.yoodobuzz.medcalldelivery.activity.help.HelpActivity
import com.yoodobuzz.medcalldelivery.activity.login.viewmodel.LoginViewmodel
import com.yoodobuzz.medcalldelivery.activity.welcome.WelcomeActivity
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper.toast
import com.yoodobuzz.medcalldelivery.utils.SessionManager

class DashboardActivity : AppCompatActivity() {
    var session: SessionManager? = null
    lateinit var loginViewmodel: LoginViewmodel
    lateinit var txtName:TextView
    lateinit var agentname:String
    private var back_pressed: Long = 0
    private var parent_view: View? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_new)
        init()
        function()
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
    }

    fun init() {
        txtName=findViewById(R.id.txtName)

        session= SessionManager(this)
        loginViewmodel = ViewModelProvider(this)[LoginViewmodel::class.java]

        val user = session!!.getUserDetails()
        agentname = user.get("agentname").toString()
        println("### agentname : ${agentname}")
        txtName.setText("Hello ${agentname},")

        if (agentname.isEmpty()) {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
    }

    fun function() {

        val lnr_my_deliveries = findViewById<LinearLayout>(R.id.lnr_my_deliveries) as LinearLayout
        val lnr_my_account = findViewById<LinearLayout>(R.id.lnr_my_account) as LinearLayout
        val lnr_help = findViewById<LinearLayout>(R.id.lnr_help) as LinearLayout
        val lnr_logout = findViewById<LinearLayout>(R.id.lnr_logout) as LinearLayout


        lnr_my_deliveries.setOnClickListener {
            val intent = Intent(this@DashboardActivity, DeliveryActivity::class.java)
            startActivity(intent)
        }
        lnr_my_account.setOnClickListener {
            val intent = Intent(this@DashboardActivity, MyAccountActivity::class.java)
            startActivity(intent)
        }
        lnr_help.setOnClickListener {
            val intent = Intent(this@DashboardActivity,HelpActivity ::class.java)
            startActivity(intent)
        }
        lnr_logout.setOnClickListener {
            val sessionManager = SessionManager(this)
            sessionManager.logoutUser()
        }

    }

}