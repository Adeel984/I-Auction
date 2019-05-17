package com.example.i_auction.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.i_auction.Models.Users
import com.example.i_auction.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception

class BiddersAdapter(val ctx: Context, val userList: ArrayList<Users?>, val bidAmount: ArrayList<Int>) :
    RecyclerView.Adapter<BiddersAdapter.myViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        val view = LayoutInflater.from(ctx).inflate(R.layout.bidders_views_rows, null)
        return myViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val user = userList[position]
        holder.u_name.text = user?.userName
        holder.bidAmount.text = bidAmount[position].toString()
        try {
            Picasso.get().load(user?.imageUri).into(holder.u_image)

        } catch (e: Exception) {
            e.message
        }
    }

    inner class myViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val u_image = view.findViewById<CircleImageView>(R.id.bidder_image_row)
        val u_name = view.findViewById<TextView>(R.id.bidder_name_row)
        val bidAmount = view.findViewById<TextView>(R.id.bidder_bidAmount_row)
    }
}