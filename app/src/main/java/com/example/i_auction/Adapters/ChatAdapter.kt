package com.example.i_auction.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.i_auction.Models.ChatMessage
import com.example.i_auction.R
import com.example.i_auction.mySharedPref.Companion.fromUser
import com.example.i_auction.mySharedPref.Companion.toUser
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter (val ctx: Context, var chatData: ArrayList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.customView>(){
    var  fromId = FirebaseAuth.getInstance().currentUser!!.uid
    override fun getItemViewType(position: Int): Int {
        return when (chatData[position].fromId.equals(fromId)) {
            true -> 0
            false -> 1
        }
    }
    override fun onCreateViewHolder(p0: ViewGroup, position: Int): customView {
        val fromMsgView = LayoutInflater.from(ctx).inflate(R.layout.row_from_msg, null)
        val toMessageview = LayoutInflater.from(ctx).inflate(R.layout.row_too_msg, null)
        when (position) {
            0 -> return customView(fromMsgView)
            else -> return customView(toMessageview)
        }
    }

    override fun getItemCount(): Int {
        return chatData.size
    }

    override fun onBindViewHolder(holder: customView, position: Int) {
        // setting image & msgs which sent by logged in user
        holder.fromMsgTv?.text = chatData[position].msg
//        holder.fromMsgTv?.setOnClickListener {
//            viewClick(it,position)
//        }
        try {
            Picasso.get().load(fromUser?.imageUri).into(holder.fromUserImg!!)
        } catch (e:NullPointerException) {
            print(e.message)
        }

        // setting image & msgs which sent by other user
        holder.toMsgTv?.text = chatData[position].msg
        try {
            Picasso.get().load(toUser?.imageUri).into(holder.toUserImg!!)
        }
        catch (e:java.lang.NullPointerException) {
            println(e.message)
        }

        // var msgs = chatData[position].fromId +" \n" + chatData[position].msg
//      when(holder.itemViewType) {
//          0-> {
//              holder.fromMsgTv?.text = chatData[position].msg
//          } else -> {
//          holder.toMsgTv?.text = chatData[position].msg
//      }
//      }


//            chatData.forEach{
//                if(it.fromId == fromId) {
//
//                } else {
//
//                }
//            }


//        if(holder.itemViewType == R.layout.row_from_msg) {
//
//        } else {
//            holder.toMsgTv?.text = chatData[position].msg
//        }
    }

    inner class customView(view: View) : RecyclerView.ViewHolder(view) {
        //MessageTextView
        var fromMsgTv: TextView? = view.findViewById(R.id.from_Msg_Tv)
        val toMsgTv: TextView? = view.findViewById(R.id.to_msg_tv)
        //UsersImagesView
        val fromUserImg: CircleImageView?=view.findViewById(R.id.fromcircleImageView)
        val toUserImg: CircleImageView? = view.findViewById(R.id.toCircleImageView)
    }
}