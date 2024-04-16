package com.yoodobuzz.medcalldelivery.activity.signup.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.yoodobuzz.medcalldelivery.network.ModelResponse
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.network.RetrofitInstance
import com.yoodobuzz.medcalldelivery.network.SingleLiveEvent
import retrofit2.Call
import retrofit2.Response

class SignUpViewmodel : ViewModel() {
    val signUpUserLiveData= SingleLiveEvent<Resource<ModelResponse>>()

    fun SignUpUserData(map: HashMap<String, String>) {
        println("### parameters :${map.toString()}")
        RetrofitInstance.ApiUser.signup(map).enqueue(object :
            retrofit2.Callback<ModelResponse> {
            override fun onResponse(call: Call<ModelResponse>, response: Response<ModelResponse>) {
                response.body()?.let { responseData ->

                    signUpUserLiveData.value = Resource.Loading()
                    if (response.isSuccessful) {
                        val status= response.body()!!.status
                        if(status.equals("true")){
                            signUpUserLiveData.value = Resource.Success(responseData)

                            println("### success signup :${responseData}")

                        }else if(status.equals("false")){
                            signUpUserLiveData.value = Resource.Error(responseData.message.toString())
                            println("### error :status :${response.body()!!.toString()}")

                        }

                    } else {
                        val message = response.message()
                        val code = response.code()

                        println("### error : code :${code} and message : ${message}")
                        signUpUserLiveData.value = Resource.Error(message = message)
                    }
                }
            }
            override fun onFailure(call: Call<ModelResponse>, t: Throwable) {

                Log.e("### on failure", t.message.toString())
                println("### on failure : ${t.message.toString()}")
            }
        })
    }
}