package com.yoodobuzz.medcalldelivery.activity.login

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.login.viewmodel.LoginViewmodel
import com.yoodobuzz.medcalldelivery.activity.signup.SignUpActivity
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper
import com.yoodobuzz.medcalldelivery.utils.Helper.toast
import com.yoodobuzz.medcalldelivery.utils.SessionManager

class LoginActivity : AppCompatActivity() {
    lateinit var edt_email: EditText
    lateinit var card_login: CardView
    lateinit var txtSignUp: TextView
    lateinit var img_back: ImageView
    lateinit var viewmodel: LoginViewmodel
    lateinit var dialog: SweetAlertDialog
    var session_manager: SessionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        println("### LoginActivity")
        init()
        function()
    }
    fun init(){
        viewmodel = ViewModelProvider(this)[LoginViewmodel::class.java]
        session_manager = SessionManager(this)
        edt_email= findViewById(R.id.edt_email)
        txtSignUp= findViewById(R.id.txtSignUp)
        img_back= findViewById(R.id.img_back)
        card_login= findViewById(R.id.card_login)
    }
    fun function(){
        dialog= SweetAlertDialog(this,SweetAlertDialog.PROGRESS_TYPE)
        card_login.setOnClickListener{
//            val intent=Intent(this@LoginActivity, DashboardActivity::class.java)
//            startActivity(intent)

            Helper.buttonLayout(card_login)
            val strEmail=edt_email.text.toString()
            if (strEmail.isNotEmpty()) {

                loginApi(strEmail)
            } else if (strEmail.isEmpty()) {
                toast("Enter email or phone number")
            }
        }
        txtSignUp.setOnClickListener{
            val intent= Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        img_back.setOnClickListener{
            onBackPressed()
        }
    }
    fun loginApi(strEmail: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (!task.isSuccessful) {
                Log.w(
                    ContentValues.TAG,
                    "Exception while registering FCM token with Braze.",
                    task.exception
                )
                return@addOnCompleteListener
            }
            val map = HashMap<String, String>()
            map["phoneno"] = strEmail
            map["fcmtoken"]=task.result.toString()

            println("### fcm token : ${task.result.toString()}")
            Helper.showDialog(dialog)
            viewmodel.LoginUserData(map)
            observeLoginLiveData()
        }

    }
    private fun observeLoginLiveData() {
        viewmodel.loginUserLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        session_manager?.createLogin(response.data)
                        println("### response ${response.data}")
                        Toast.makeText(this, "Submitted Successfully", Toast.LENGTH_SHORT).show()
                        val intent= Intent(this@LoginActivity, DashboardActivity::class.java)
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
        }
        )
    }
}