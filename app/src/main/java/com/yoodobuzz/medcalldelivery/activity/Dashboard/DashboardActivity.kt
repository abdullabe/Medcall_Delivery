package com.yoodobuzz.medcalldelivery.activity.Dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.account.MyAccountActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.DeliveryActivity
import com.yoodobuzz.medcalldelivery.activity.help.HelpActivity
import com.yoodobuzz.medcalldelivery.activity.login.viewmodel.LoginViewmodel
import com.yoodobuzz.medcalldelivery.activity.map.MapActivity
import com.yoodobuzz.medcalldelivery.activity.welcome.WelcomeActivity
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper.toast
import com.yoodobuzz.medcalldelivery.utils.SessionManager

class DashboardActivity : AppCompatActivity() {
    var session: SessionManager? = null
    lateinit var loginViewmodel: LoginViewmodel
    lateinit var txtName: TextView
    lateinit var map: TextView
    lateinit var agentname: String
    private var back_pressed: Long = 0
    private var parent_view: View? = null
    private val phoneNumber = "8667040195"

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_new)
        init()
        function()
        onBackPressedDispatcher.addCallback(
            this /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (back_pressed + 2000 > System.currentTimeMillis()) {
                        finishAffinity()
                    } else {
                        if (parent_view != null) {
                            Snackbar.make(
                                parent_view!!,
                                "Press once again to exit!",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                    back_pressed = System.currentTimeMillis()
                }
            })
    }

    fun init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        txtName = findViewById(R.id.txtName)
        map = findViewById(R.id.map)

        session = SessionManager(this)
        loginViewmodel = ViewModelProvider(this)[LoginViewmodel::class.java]

        val user = session!!.getUserDetails()
        agentname = user.get("agentname").toString()
        println("### agentname : ${agentname}")
        txtName.setText("Hello ${agentname},")

        if (agentname.isEmpty()) {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
        map.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
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
            showContactOptionsDialog()
        }
        lnr_logout.setOnClickListener {
            val sessionManager = SessionManager(this)
            sessionManager.logoutUser()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val currentLatLng = LatLng(it.latitude, it.longitude)
                        val START_LOCATION=currentLatLng
                        println("### latitude : ${it.latitude}")
                        println("### longitude: ${it.longitude}")
                    }
                }
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                // Re-call map setup here if needed
            }
        }
    }
    private fun showContactOptionsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_contact_options, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        val btnCallus=dialogView.findViewById<Button>(R.id.buttonCallUs)
        val buttonWhatsAppUs=dialogView.findViewById<Button>(R.id.buttonWhatsAppUs)
        buttonWhatsAppUs.setText("Chat")

        btnCallus.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
            dialog.dismiss()
        }

        buttonWhatsAppUs.setOnClickListener {
            val intent =Intent(this,HelpActivity::class.java)
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.show()
    }

}