package com.yoodobuzz.medcalldelivery.activity.account

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.account.viewmodel.MyAccountViewmodel
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper.toast
import com.yoodobuzz.medcalldelivery.utils.SessionManager

class MyAccountActivity : AppCompatActivity() {
    lateinit var img_back: ImageView
    lateinit var txtName: TextView
    lateinit var txtEmail: TextView
    lateinit var txtAddress: TextView
    lateinit var txtPhNo: TextView
    lateinit var txtPincode: TextView

    lateinit var viewmodel: MyAccountViewmodel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)
        init()
        function()
    }

    fun init() {
        img_back = findViewById(R.id.img_back)
        txtEmail = findViewById(R.id.txtEmail)
        txtName = findViewById(R.id.txtName)
        txtAddress = findViewById(R.id.txtAddress)
        txtPhNo = findViewById(R.id.txtPhNo)
        txtPincode = findViewById(R.id.txtPincode)

    }

    fun function() {
        viewmodel = ViewModelProvider(this)[MyAccountViewmodel::class.java]

        img_back.setOnClickListener {
            onBackPressed()
        }
        val session= SessionManager(this)
        val user = session.getUserDetails()
        val email = user.get("email")

        println("### email : ${email}")
        val str_userId = user.get("user_id").toString()
        println("### userid : ${str_userId}")
        viewmodel.getProfileData(str_userId)
        observeGetProfileLiveData()
    }





    fun observeGetProfileLiveData() {
        viewmodel.getProfileDetailsLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response :${response.data}")
                        txtEmail.setText(response.data.profile?.email)
                        txtName.setText(response.data.profile?.agentName)
                        txtAddress.setText(response.data.profile?.address)
                        txtPhNo.setText(response.data.profile?.phoneno.toString())
                        txtPincode.setText(response.data.profile?.pincode)

                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    val errorMessage = response.message ?: "An error occurred"
                    toast(errorMessage)
                }
            }
        })
    }


}