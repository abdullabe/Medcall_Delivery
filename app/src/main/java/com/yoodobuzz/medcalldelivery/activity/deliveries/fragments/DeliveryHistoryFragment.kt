package com.yoodobuzz.medcalldelivery.activity.deliveries.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.pedant.SweetAlert.SweetAlertDialog
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.Dashboard.DashboardActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.adapter.AdapterHistory
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper
import com.yoodobuzz.medcalldelivery.utils.SessionManager


class DeliveryHistoryFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    lateinit var rec_history: RecyclerView
    lateinit var lnrNoData: LinearLayout
    lateinit var viewmodel: ActivityViewmodel
    lateinit var adapterHistory: AdapterHistory
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root=inflater.inflate(R.layout.fragment_delivery_history, container, false)
        init(root)
        function()
        return root
    }
    fun init(root:View){
        viewmodel = ViewModelProvider(this)[ActivityViewmodel::class.java]

        rec_history = root.findViewById(R.id.rec_History)
        lnrNoData = root.findViewById(R.id.lnrNoData)
        swipeRefreshLayout =root. findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)
        adapterHistory = AdapterHistory()
        prepareRecyclerView()
    }
    fun prepareRecyclerView(){
        rec_history.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            adapter = adapterHistory
        }
    }
    fun function(){
        val session= SessionManager(requireContext())
        val user = session.getUserDetails()
        val email = user.get("email")

        println("### email : ${email}")
        val str_userId = user.get("user_id").toString()
        viewmodel.getagentorderhistory(str_userId)
        observeActivityViewmodel()
    }
    fun observeActivityViewmodel(){
        viewmodel.getAgentHistoryLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response :${response.data}")
                        if(response.data.cartItems.isNotEmpty()){
                            adapterHistory.setHistoryList(response.data.cartItems)

                            lnrNoData.isVisible=false
                            rec_history.isVisible=true
                        }else{
                            lnrNoData.isVisible=true
                            rec_history.isVisible=false
                        }

                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    val errorMessage = response.message ?: "An error occurred"
                }
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