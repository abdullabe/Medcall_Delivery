package com.yoodobuzz.medcalldelivery.activity.deliveries.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.deliveries.model.CartItems
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveredActivity
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveredPickUpActivity
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveryOneActivity
import com.yoodobuzz.medcalldelivery.activity.trackingmap.DeliveryTwoActivity
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterActivity(val context: Context) :
    RecyclerView.Adapter<AdapterActivity.MyViewHolder>() {
    var activeList = ArrayList<CartItems>()

    fun setActiveList(categoryList: List<CartItems>) {
        this.activeList = categoryList as ArrayList
        notifyDataSetChanged()
    }
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtUserName: TextView = view.findViewById(R.id.txtName)
        var txtDate: TextView = view.findViewById(R.id.txtDate)
        var txtOrderId: TextView = view.findViewById(R.id.txtOrderId)
        var txtShop: TextView = view.findViewById(R.id.txtShop)
        var txtAddress: TextView = view.findViewById(R.id.txtAddress)
        var img_activity: ImageView = view.findViewById(R.id.img_prod)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_list, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.txtUserName.setText(activeList[position].firstname +" "+activeList[position].lastname)
        holder.txtDate.setText("Required to be picked:"+convertDateFormat(activeList[position].date!!))
        holder.txtOrderId.setText((activeList[position].orderId!!))
        holder.txtShop.setText((activeList[position].storeName!!))
        Glide.with(holder.itemView.context)
            .load(activeList[position].prod_image)
            .into(holder.img_activity)

        holder.txtAddress.setText(activeList[position].userAdd!!.address_detail)
        holder.itemView.setOnClickListener(View.OnClickListener {

            if(activeList[position].status.equals("store_accept")){
                val intent= Intent(context, DeliveryOneActivity::class.java)
                context.startActivity(intent)
            }else if(activeList[position].status.equals("accept")){
                val intent= Intent(context, DeliveryTwoActivity::class.java)
                context.startActivity(intent)
            }else if(activeList[position].status.equals("pickup")){
                val intent= Intent(context, DeliveredActivity::class.java)
                context.startActivity(intent)
            }else if(activeList[position].status.equals("dispatched")){
                val intent= Intent(context, DeliveryTwoActivity::class.java)
                context.startActivity(intent)
            }
        })

    }
    override fun getItemCount(): Int {
        return activeList.size
    }
    fun convertDateFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

        val date = inputFormat.parse(inputDate)
        return outputFormat.format(date)
    }

}