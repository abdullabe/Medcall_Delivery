package com.yoodobuzz.medcalldelivery.activity.trackingmap.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yoodobuzz.medcalldelivery.R
import com.yoodobuzz.medcalldelivery.activity.deliveries.model.Products

class AdapterProduct  :
    RecyclerView.Adapter<AdapterProduct.MyViewHolder>() {
    var prodList = ArrayList<Products>()
    fun setProductList(categoryList: List<Products>) {
        this.prodList = categoryList as ArrayList
        notifyDataSetChanged()
    }
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtItemName: TextView = view.findViewById(R.id.txtItemName)
        var txtQty: TextView = view.findViewById(R.id.txtQty)
        var txtPrice: TextView = view.findViewById(R.id.txtPrice)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txtItemName.setText(prodList[position].productName)
        holder.txtQty.setText(prodList[position].quantity.toString())
        holder.txtPrice.text = "₹${prodList[position].price?.toDoubleOrNull()?.toInt() ?: 0}"
    }
    override fun getItemCount(): Int {
        return prodList.size
    }
}