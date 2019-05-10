package com.example.i_auction

import android.app.Dialog
import android.content.Context
import com.example.i_auction.Models.Auctioners
import com.example.i_auction.Models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson

class mySharedPref {
    companion object {
        var userId:String? = null
        val appliedUsersList = HashMap<String,Int>()
    }
    fun setUserinsharedPref(ctx: Context, uid:String, user: Users) {
        val sharedPrefrences = ctx.getSharedPreferences("IAuction",0)
        val editor = sharedPrefrences.edit()
        val gson= Gson()
        val nowuser = gson.toJson(user)
        editor.putString(uid,nowuser)
        editor.apply()
        editor.commit()
    }
    fun getUserfromsharedPref(ctx: Context, uid:String): Users? {
        val sharedPrefrences = ctx.getSharedPreferences("IAuction",0)
        val usernow = sharedPrefrences.getString(uid,null)
        val gson = Gson()
        val saveduser = gson.fromJson<Users>(usernow,Users::class.java)
        return saveduser
    }

    fun setAuctionerDatainSPref(ctx: Context, uid:String, auctioner: Auctioners) {
        val sharedPrefrences = ctx.getSharedPreferences("Auctioners",0)
        val editor = sharedPrefrences.edit()
        val gson= Gson()
        val nowuser = gson.toJson(auctioner)
        editor.putString(uid,nowuser)
        editor.apply()
        editor.commit()
    }
    fun getAuctionerDatainSPref(ctx: Context, uid:String): Auctioners? {
        val sharedPrefrences = ctx.getSharedPreferences("Auctioners",0)
        val usernow = sharedPrefrences.getString(uid,null)
        val gson = Gson()
        val saveduser = gson.fromJson<Auctioners>(usernow,Auctioners::class.java)
        return saveduser
    }
}