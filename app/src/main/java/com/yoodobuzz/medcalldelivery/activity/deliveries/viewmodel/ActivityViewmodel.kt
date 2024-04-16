package com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yoodobuzz.medcalldelivery.activity.deliveries.model.ActivityAssignedModelResponse
import com.yoodobuzz.medcalldelivery.activity.deliveries.model.AgentHistoryModelResponse
import com.yoodobuzz.medcalldelivery.network.ModelResponse
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.network.RetrofitInstance
import com.yoodobuzz.medcalldelivery.network.SingleLiveEvent
import retrofit2.Call
import retrofit2.Response

class ActivityViewmodel : ViewModel(){
    val getActivityLiveData= MutableLiveData<Resource<ActivityAssignedModelResponse>>()
    val getAgentHistoryLiveData= MutableLiveData<Resource<AgentHistoryModelResponse>>()
    val acceptUserLiveData= SingleLiveEvent<Resource<ModelResponse>>()
    val cancelUserLiveData= SingleLiveEvent<Resource<ModelResponse>>()

    fun acceptUserData(map: HashMap<String, String>) {
        RetrofitInstance.ApiUser.agentsorderstatus(map).enqueue(object :
            retrofit2.Callback<ModelResponse> {
            override fun onResponse(call: Call<ModelResponse>, response: Response<ModelResponse>) {
                response.body()?.let { responseData ->

                    acceptUserLiveData.value = Resource.Loading()
                    if (response.isSuccessful) {
                        val status= response.body()!!.status
                        if(status.equals("true")){
                            acceptUserLiveData.value = Resource.Success(responseData)

                            println("### success :${responseData}")

                        }else if(status.equals("false")){
                            acceptUserLiveData.value = Resource.Error(responseData.message.toString())
                            println("### error :status :${response.body()!!.toString()}")

                        }

                    } else {
                        val message = response.message()
                        val code = response.code()
                        println("### error : code :${code} and message : ${message}")
                        acceptUserLiveData.value = Resource.Error(message = message)
                    }
                }
            }
            override fun onFailure(call: Call<ModelResponse>, t: Throwable) {
                Log.e("### on failure", t.message.toString())
                println("### on failure : ${t.message.toString()}")
            }
        })
    }
    fun cancelUserData(map: HashMap<String, String>) {
        RetrofitInstance.ApiUser.agentsorderstatus_cancel(map).enqueue(object :
            retrofit2.Callback<ModelResponse> {
            override fun onResponse(call: Call<ModelResponse>, response: Response<ModelResponse>) {
                response.body()?.let { responseData ->

                    cancelUserLiveData.value = Resource.Loading()
                    if (response.isSuccessful) {
                        val status= response.body()!!.status
                        if(status.equals("true")){
                            cancelUserLiveData.value = Resource.Success(responseData)

                            println("### success :${responseData}")

                        }else if(status.equals("false")){
                            cancelUserLiveData.value = Resource.Error(responseData.message.toString())
                            println("### error :status :${response.body()!!.toString()}")

                        }

                    } else {
                        val message = response.message()
                        val code = response.code()
                        println("### error : code :${code} and message : ${message}")
                        cancelUserLiveData.value = Resource.Error(message = message)
                    }
                }
            }
            override fun onFailure(call: Call<ModelResponse>, t: Throwable) {
                Log.e("### on failure", t.message.toString())
                println("### on failure : ${t.message.toString()}")
            }
        })
    }

    fun getActivityData(agent_id:String) {
        println("### api name : ${RetrofitInstance.ApiUser.getorderassign_del(agent_id).toString()}")
        RetrofitInstance.ApiUser.getorderassign_del(agent_id).enqueue(object :
            retrofit2.Callback<ActivityAssignedModelResponse> {
            override fun onResponse(call: Call<ActivityAssignedModelResponse>, response: Response<ActivityAssignedModelResponse>) {
                response.body()?.let { responseData ->

                    getActivityLiveData.value = Resource.Loading()
                    if (response.isSuccessful) {
                        val status= response.body()!!.status
                        if(status.equals("true")){
                            getActivityLiveData.value = Resource.Success(responseData)

                            println("### success :${responseData}")

                        }else if(status.equals("false")){
                            getActivityLiveData.value = Resource.Error(responseData.message.toString())
                            println("### error :status :${response.body()!!.toString()}")

                        }

                    } else {
                        val message = response.message()
                        val code = response.code()
                        println("### error : code :${code} and message : ${message}")

                        getActivityLiveData.value = Resource.Error(message = message)
                    }
                }
            }
            override fun onFailure(call: Call<ActivityAssignedModelResponse>, t: Throwable) {
                Log.e("### on failure", t.message.toString())
                println("### on failure : ${t.message.toString()}")
            }
        })
    }
    fun getagentorderhistory(agent_id:String) {
        RetrofitInstance.ApiUser.getagentorderhistory(agent_id).enqueue(object :
            retrofit2.Callback<AgentHistoryModelResponse> {
            override fun onResponse(call: Call<AgentHistoryModelResponse>, response: Response<AgentHistoryModelResponse>) {
                response.body()?.let { responseData ->

                    getAgentHistoryLiveData.value = Resource.Loading()
                    if (response.isSuccessful) {
                        val status= response.body()!!.status
                        if(status.equals("true")){
                            getAgentHistoryLiveData.value = Resource.Success(responseData)

                            println("### success :${responseData}")

                        }else if(status.equals("false")){
                            getAgentHistoryLiveData.value = Resource.Error(responseData.message.toString())
                            println("### error :status :${response.body()!!.toString()}")

                        }

                    } else {
                        val message = response.message()
                        val code = response.code()
                        println("### error : code :${code} and message : ${message}")
                        getAgentHistoryLiveData.value = Resource.Error(message = message)
                    }
                }
            }
            override fun onFailure(call: Call<AgentHistoryModelResponse>, t: Throwable) {
                Log.e("### on failure", t.message.toString())
                println("### on failure : ${t.message.toString()}")
            }
        })
    }
}