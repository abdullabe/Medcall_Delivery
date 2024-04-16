package com.yoodobuzz.medcalldelivery.activity.account.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yoodobuzz.medcalldelivery.activity.account.model.GetProfileModelResponse
import com.yoodobuzz.medcalldelivery.activity.account.model.UploadProfileModelResponse
import com.yoodobuzz.medcalldelivery.activity.account.model.UploadProofModelResponse
import com.yoodobuzz.medcalldelivery.network.ModelResponse
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.network.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

class MyAccountViewmodel : ViewModel() {
    val getProfileDetailsLiveData=MutableLiveData<Resource<GetProfileModelResponse>>()




    fun getProfileData(prod_id:String) {
        RetrofitInstance.ApiUser.getProfile(prod_id).enqueue(object :
            retrofit2.Callback<GetProfileModelResponse> {
            override fun onResponse(call: Call<GetProfileModelResponse>, response: Response<GetProfileModelResponse>) {
                response.body()?.let { responseData ->

                    getProfileDetailsLiveData.value = Resource.Loading()
                    if (response.isSuccessful) {
                        val status= response.body()!!.status
                        if(status.equals("true")){
                            getProfileDetailsLiveData.value = Resource.Success(responseData)

                            println("### success :${responseData}")

                        }else if(status.equals("false")){
                            getProfileDetailsLiveData.value = Resource.Error(responseData.message.toString())
                            println("### error :status :${response.body()!!.toString()}")

                        }

                    } else {
                        val message = response.message()
                        val code = response.code()
                        println("### error : code :${code} and message : ${message}")
                        getProfileDetailsLiveData.value = Resource.Error(message = message)
                    }
                }
            }
            override fun onFailure(call: Call<GetProfileModelResponse>, t: Throwable) {
                Log.e("### on failure", t.message.toString())
                println("### on failure : ${t.message.toString()}")
            }
        })
    }


}