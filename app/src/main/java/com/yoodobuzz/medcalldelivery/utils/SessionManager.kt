package com.yoodobuzz.medcalldelivery.utils

import android.content.Context
import android.content.Intent
import com.yoodobuzz.medcalldelivery.MainActivity
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.login.model.LoginModelResponse

class SessionManager (val context: Context) {
    var PRIVATE_MODE = 0
    val PREF_NAME = "AndroidHivePref"
    var pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    var editor = pref.edit()
    private val IS_LOGIN = "IsLoggedIn"

    val KEY_USER_ID = "user_id"
    val KEY_EMAIL="email"
    val KEY_AGENT_NAME="agentname"

    fun createLogin(users: LoginModelResponse){
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_AGENT_NAME,users.agentname)
        editor.putString(KEY_USER_ID,users.userId)
        editor.putString(KEY_EMAIL,users.email)
        editor.apply()
    }

    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[KEY_USER_ID] = pref.getString(KEY_USER_ID, "")
        user[KEY_AGENT_NAME] = pref.getString(KEY_AGENT_NAME, "")
        user[KEY_EMAIL] = pref.getString(KEY_EMAIL, "")
        // return user
        return user
    }

    fun logoutUser() {
        editor.clear()
        editor.commit()
        val i = Intent(context, DashboardActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }
}