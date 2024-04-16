package com.yoodobuzz.medcalldelivery.activity.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.login.LoginActivity
import com.yoodobuzz.medcalldelivery.activity.signup.viewmodel.SignUpViewmodel
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper.toast

class SignUpActivity : AppCompatActivity() {
    var edtFirstName: EditText? = null
    var edtLastName: EditText? = null
    var edt_email: EditText? = null
    var edtPhNumber: EditText? = null
    var edtPwd: EditText? = null
    var edtConfrmPwd: EditText? = null
    var cardSignup: CardView? = null
    var txtLogin: TextView? = null
    lateinit var viewmodel: SignUpViewmodel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        init()
        function()
    }
    fun init() {
        edtFirstName = findViewById(R.id.edtFirstName)
        edtLastName = findViewById(R.id.edtLastName)
        edt_email = findViewById(R.id.edt_email)
        edtPhNumber = findViewById(R.id.edtPhNumber)
        edtPwd = findViewById(R.id.edtPwd)
        edtConfrmPwd = findViewById(R.id.edtConfrmPwd)
        cardSignup = findViewById(R.id.cardSignup)
        txtLogin = findViewById(R.id.txtLogin)
    }

    fun function() {
        viewmodel = ViewModelProvider(this)[SignUpViewmodel::class.java]
        cardSignup?.setOnClickListener {
            val strFirstName = edtFirstName!!.text.toString()
            val strLastName = edtLastName!!.text.toString()
            val strEmail = edt_email!!.text.toString()
            val strPhNumber = edtPhNumber!!.text.toString()
            val strPwd = edtPwd!!.text.toString()
            val strConfrmPwd = edtConfrmPwd!!.text.toString()
            if (strFirstName.isEmpty()) {
                toast("First name is empty")

            } else if (strLastName.isEmpty()) {
                toast("Last name is empty")
            } else if (strEmail.isEmpty()) {
                toast("Email is empty")

            } else if (strPhNumber.isEmpty()) {
                toast("Phone number is empty")

            } else if (strPwd.isEmpty()) {
                toast("Password is empty")

            } else if (strConfrmPwd.isEmpty()) {
                toast("Confirm Password is empty")

            } else {
                if(strPwd.equals(strConfrmPwd)){
                    callSignUpApi(strFirstName,
                        strLastName,
                        strEmail,
                        strPhNumber,
                        strPwd)
                }else{
                    toast("Password both are not same")
                }
            }

        }
    }

    private fun callSignUpApi(
        strFirstName: String,
        strLastName: String,
        strEmail: String,
        strPhNumber: String,
        strPwd: String
    ) {
        val map = HashMap<String, String>()
        map["firstname"] = strFirstName
        map["lastname"] = strLastName
        map["email"] = strEmail
        map["password"] = strPwd
        map["phone_number"] = strPhNumber
        map["userType"] = "delivery"

        viewmodel.SignUpUserData(map)
        observeSignUpLiveData()
    }
    private fun observeSignUpLiveData() {
        viewmodel.signUpUserLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                        val intent= Intent(this@SignUpActivity, LoginActivity::class.java)
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
}