package com.example.i_auction.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.i_auction.Models.Items
import com.example.i_auction.Models.Users
import com.example.i_auction.R
import com.example.i_auction.enums
import com.example.i_auction.mySharedPref
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class ItemsRVAdapter(var ctx:Context,var itemsList:ArrayList<Items>, var viewClick: (view:View,position:Int) -> Unit) : RecyclerView.Adapter<ItemsRVAdapter.myViewHolder>() {
    var user:Users? = null
    val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        user = mySharedPref().getUserfromsharedPref(ctx,userId)
    val view = LayoutInflater.from(ctx).inflate(R.layout.items_view_layout,null)
        return myViewHolder(view)
    }

    override fun getItemCount(): Int {
    return itemsList.size
    }

    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val item = itemsList[position]
        holder.itemName.text = item.itemBrand +" "+ item.itemName
    //    holder.itemDesc.text = item.itemDesc
    //    holder.itemBrand.text = item.itemBrand
        if(item.max_bid_amount !=null) {
            holder.bidAmount.text = "Rs: "+ item.max_bid_amount
        } else {
            holder.bidAmount.text ="Rs: "+ item.min_bid_amount
        }
        Picasso.get().load(item.itemImageUri).into(holder.itemImage)
        when (user?.accType) {
            enums.AUCTIONER.value->  {
                holder.contactBtn.visibility = View.GONE
                holder.bidAmount.visibility = View.GONE
                when(item.withDraw) {
                    true -> {
                        holder.itemStatus.setText("Un-Available")
                        //holder.re_bid.visibility = View.VISIBLE
                        //holder.closeBid.visibility= View.GONE
                    }
                    false -> {
                        holder.itemStatus.setText("Available")
                        //holder.re_bid.visibility = View.GONE
                        //holder.closeBid.visibility = View.VISIBLE
                    }
                }
            }
            enums.BIDDER.value-> {
                holder.itemStatus.text = item.itemCategory
                holder.contactBtn.visibility = View.VISIBLE
                when(itemsList[position].bidded_users?.keys?.contains(userId)) {
                    true -> {
                        holder.applyForBid.visibility = View.GONE
                        holder.withDrawFrobBid.visibility = View.VISIBLE
                        itemsList[position].bidded_users?.values?.forEach {
                           if(it.accepted_bid == true)
                            holder.withDrawFrobBid.visibility= View.GONE
                        }

                        }
                        false -> {
                            holder.applyForBid.visibility = View.VISIBLE
                            holder.withDrawFrobBid.visibility = View.GONE
                        }
                }
            }
        }
        holder.contactBtn.setOnClickListener {
            viewClick(it,position)
        }
        holder.applyForBid.setOnClickListener {
            viewClick(it,position)
        }
        holder.withDrawFrobBid.setOnClickListener {
            viewClick(it,position)
        }

//        holder.closeBid.setOnClickListener {view ->
//            viewClick(view,position)
//        }
//        holder.re_bid.setOnClickListener {
//            viewClick(it,position)
//        }
        holder.cardView.setOnClickListener {
            viewClick(it,position)
        }
    }

//    private fun getAppliedUsers(position: Int) : Boolean {
//        var userApplied = false
//        val dbRef = FirebaseFirestore.getInstance()
//        dbRef.collection("Items").document(itemsList[position].itemId).get()
//            .addOnSuccessListener {documents ->
//                if(documents!=null && documents.exists()) {
//                    val userList = documents.toObject(HashMap::class.java)
//                    appliedUsersList.add(userList?.values.toString())
//                    if(appliedUsersList.contains(userId)) userApplied = true
//                     else userApplied =false
//
////                    when(userList) {
////                        userId -> {
////                        holder.applyForBid.visibility = View.GONE
////                            holder.withDrawFrobBid.visibility = View.VISIBLE
////                        }
////                        else -> {
////                            holder.applyForBid.visibility = View.VISIBLE
////                            holder.withDrawFrobBid.visibility = View.GONE
////                        }
// //                   }
//                }
//            }
//        return userApplied
//    }

    inner class myViewHolder(view:View) : RecyclerView.ViewHolder(view){
        val itemName:TextView = view.findViewById(R.id.item_name_view)
  //      val itemDesc:TextView = view.findViewById(R.id.item_desc_view)
//        val itemBrand:TextView = view.findViewById(R.id.item_brand_view)
        val itemImage:ImageView = view.findViewById(R.id.item_image_view)
        val bidAmount:TextView = view.findViewById(R.id.bid_amount_view)
        val itemStatus:TextView = view.findViewById(R.id.item_status_view)
        val cardView:CardView = view.findViewById(R.id.item_cardView)
        // Auctioner Buttons
//        val closeBid:Button = view.findViewById(R.id.close_bid_btn)
//        val re_bid:Button = view.findViewById(R.id.re_bid_btn)
        //Bidder Buttons
        val applyForBid:Button= view.findViewById(R.id.bid_now_view)
        val withDrawFrobBid:Button = view.findViewById(R.id.withdraw_bid_view)
        val contactBtn:Button = view.findViewById(R.id.contact_auctioner)
    }
}
