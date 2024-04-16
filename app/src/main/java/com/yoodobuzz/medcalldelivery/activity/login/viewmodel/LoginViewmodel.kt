package com.yoodobuzz.medcalldelivery.activity.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.yoodobuzz.medcalldelivery.activity.login.model.LoginModelResponse
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.network.RetrofitInstance
import com.yoodobuzz.medcalldelivery.network.SingleLiveEvent
import retrofit2.Call
import retrofit2.Response

class LoginViewmodel : ViewModel() {
    val loginUserLiveData= SingleLiveEvent<Resource<LoginModelResponse>>()

    fun LoginUserData(map: HashMap<String, String>) {
        RetrofitInstance.ApiUser.login(map).enqueue(object :
            retrofit2.Callback<LoginModelResponse> {
            override fun onResponse(call: Call<LoginModelResponse>, response: Response<LoginModelResponse>) {
                response.body()?.let { responseData ->

                    loginUserLiveData.value = Resource.Loading()
                    if (response.isSuccessful) {
                        val status= response.body()!!.status
                        if(status.equals("true")){
                            loginUserLiveData.value = Resource.Success(responseData)

                            println("### success dependent :${responseData}")

                        }else if(status.equals("false")){
                            loginUserLiveData.value = Resource.Error(responseData.message.toString())
                            println("### error :status :${response.body()!!.toString()}")

                        }

                    } else {
                        val message = response.message()
                        val code = response.code()
                        println("### error : code :${code} and message : ${message}")
                        loginUserLiveData.value = Resource.Error(message = message)
                    }
                }
            }
            override fun onFailure(call: Call<LoginModelResponse>, t: Throwable) {
                Log.e("### on failure", t.message.toString())
                println("### on failure : ${t.message.toString()}")
            }
        })
    }
}