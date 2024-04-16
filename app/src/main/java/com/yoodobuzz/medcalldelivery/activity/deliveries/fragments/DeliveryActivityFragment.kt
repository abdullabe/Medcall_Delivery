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
import com.yoodobuzz.medcalldelivery.activity.deliveries.adapter.AdapterActivity
import com.yoodobuzz.medcalldelivery.activity.deliveries.viewmodel.ActivityViewmodel
import com.yoodobuzz.medcalldelivery.network.Resource
import com.yoodobuzz.medcalldelivery.utils.Helper
import com.yoodobuzz.medcalldelivery.utils.SessionManager

class DeliveryActivityFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener  {

    lateinit var rec_activity: RecyclerView
    lateinit var lnrNoData: LinearLayout
    lateinit var viewmodel: ActivityViewmodel
    lateinit var adapterAct: AdapterActivity
    lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_delivery_activity, container, false)

        init(root)
        function()
        return root
    }

    fun init(root: View) {
        viewmodel = ViewModelProvider(this)[ActivityViewmodel::class.java]

        rec_activity = root.findViewById(R.id.rec_activity)
        lnrNoData = root.findViewById(R.id.lnrNoData)
        swipeRefreshLayout =root. findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)
        adapterAct = AdapterActivity(requireContext())
        prepareRecyclerView()

    }
    fun prepareRecyclerView(){
        rec_activity.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            adapter = adapterAct
        }
    }


    fun function(){
       val session= SessionManager(requireContext())
        val user = session.getUserDetails()
        val email = user.get("email")

        println("### email : ${email}")
        val str_userId = user.get("user_id").toString()
        println("### strUserid : ${str_userId}")

        viewmodel.getActivityData(str_userId)
        observeActivityViewmodel()

    }
    fun observeActivityViewmodel(){
        viewmodel.getActivityLiveData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    if (response.data != null) {
                        println("### response :${response.data}")
                        if(response.data.cartItems.isNotEmpty()){
                            adapterAct.setActiveList(response.data.cartItems)
                            lnrNoData.isVisible=false
                            rec_activity.isVisible=true

                        }else{
                            println("### response data : ${response.data.message}")
                            lnrNoData.isVisible=true
                            rec_activity.isVisible=false
                        }
                    }
                }
                is Resource.Loading -> {
                }
                is Resource.Error -> {
                    val errorMessage = response.message ?: "An error occurred"
                    println("### error message : ${errorMessage}")

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