package com.yoodobuzz.medcalldelivery.activity.welcome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.login.LoginActivity
import com.yoodobuzz.medcalldelivery.activity.signup.SignUpActivity

class WelcomeActivity : AppCompatActivity() {
    lateinit var txtSignUp: TextView
    lateinit var card_login: CardView
    lateinit var txtLogin: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        println("### WelcomeActivity")
        init()
        function()
    }
    fun init(){
        txtSignUp=findViewById(R.id.txtSignUp)
        card_login=findViewById(R.id.card_login)
        txtLogin=findViewById(R.id.txtLogin)
    }
    fun function(){
        card_login.setOnClickListener{
            val intent= Intent(this@WelcomeActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        txtLogin.setOnClickListener{
            val intent= Intent(this@WelcomeActivity,LoginActivity::class.java)
            startActivity(intent)
        }
        txtSignUp.setOnClickListener{
            val intent= Intent(this@WelcomeActivity, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}