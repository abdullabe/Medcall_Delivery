package com.yoodobuzz.medcalldelivery.activity.trackingmap

import android.app.Dialog
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
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.pedant.SweetAlert.SweetAlertDialog
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper
import com.yoodobuzz.medcalldelivery.utils.SessionManager

class DeliveryTwoActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    lateinit var cardPickUp: CardView
    lateinit var txtDestination: TextView
    lateinit var txtPhNo: TextView
    lateinit var txtWait: TextView
    lateinit var viewmodel: ActivityViewmodel
    lateinit var dialog: SweetAlertDialog
    lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_two)

        init()
        function()
    }

    fun init() {
        viewmodel = ViewModelProvider(this)[ActivityViewmodel::class.java]
        dialog=SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        cardPickUp = findViewById(R.id.cardPickUp)
        txtWait = findViewById(R.id.txtWait)
        txtPhNo = findViewById(R.id.txtPhNo)
        txtDestination = findViewById(R.id.txtDestination)
        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)

        val session= SessionManager(this)
        val user = session.getUserDetails()
        val email = user.get("email")

        println("### email : ${email}")
        val str_userId = user.get("user_id").toString()

        Helper.showDialog(dialog)
        viewmodel.getActivityData(str_userId)
        observeActivityViewmodel()
    }
    fun observeActivityViewmodel(){
        viewmodel.getActivityLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response :${response.data}")
                        if(response.data.cartItems.isNotEmpty()){
                            val activityList = response.data.cartItems.get(0)
                            txtDestination.setText(
                                activityList.userAdd!!.address_detail
                            )
                            val phno=response.data.cartItems.get(0).phoneNumber.toString()

                            txtPhNo.setText(phno)
                            if(response.data.cartItems.get(0).phoneNumber.toString().isNotEmpty()){
                                txtPhNo.setOnClickListener{
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phno")
                                    }
                                    startActivity(intent)
                                }
                            }
                            if(response.data.cartItems.get(0).status.equals("accept")){
                                Toast.makeText(this, "please wait store will approved shortly", Toast.LENGTH_SHORT).show()
                                txtWait.isVisible=true
                                cardPickUp.isVisible=false
                            }else{
                                txtWait.isVisible=false
                                cardPickUp.isVisible=true
                            }


                            cardPickUp.setOnClickListener {
                                if(response.data.cartItems.get(0).status.equals("accept")){
                                    Toast.makeText(this, "please wait store will approved shortly", Toast.LENGTH_SHORT).show()
                                }else{
                                    val intent= Intent(this@DeliveryTwoActivity,DeliveredPickUpActivity::class.java)
                                    startActivity(intent)
                                }

                            }

                        }
                        Helper.hideDialog(dialog)
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    Helper.hideDialog(dialog)
                    val errorMessage = response.message ?: "An error occurred"
                }
            }
        })

    }

    fun function() {
        onBackPressedDispatcher.addCallback(this /* lifecycle owner */, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent= Intent(this@DeliveryTwoActivity, DashboardActivity::class.java)
                startActivity(intent)
            }
        })
    }

    override fun onRefresh() {
        loadData()
    }
    private fun loadData() {
        android.os.Handler().postDelayed({
            swipeRefreshLayout.isRefreshing = false
            function()
        }, 1000)
    }
}

