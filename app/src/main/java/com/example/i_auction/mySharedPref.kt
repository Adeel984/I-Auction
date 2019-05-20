package com.example.i_auction

import android.content.Context
import android.util.Log
import com.example.i_auction.Adapters.ItemsRVAdapter
import com.example.i_auction.Models.Auctioners
import com.example.i_auction.Models.Items
import com.example.i_auction.Models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson

class mySharedPref {
    companion object {
        var userId: String? = null
        val appliedUsersList = HashMap<String, Int>()
    }

    val userIdx = FirebaseAuth.getInstance().currentUser!!.uid

    fun setUserinsharedPref(ctx: Context, uid: String, user: Users) {
        val sharedPrefrences = ctx.getSharedPreferences("IAuction", 0)
        val editor = sharedPrefrences.edit()
        val gson = Gson()
        val nowuser = gson.toJson(user)
        editor.putString(uid, nowuser)
        editor.apply()
        editor.commit()
    }

    fun getUserfromsharedPref(ctx: Context, uid: String): Users? {
        val sharedPrefrences = ctx.getSharedPreferences("IAuction", 0)
        val usernow = sharedPrefrences.getString(uid, null)
        val gson = Gson()
        val saveduser = gson.fromJson<Users>(usernow, Users::class.java)
        return saveduser
    }

    fun setAuctionerDatainSPref(ctx: Context, uid: String, auctioner: Auctioners) {
        val sharedPrefrences = ctx.getSharedPreferences("Auctioners", 0)
        val editor = sharedPrefrences.edit()
        val gson = Gson()
        val nowuser = gson.toJson(auctioner)
        editor.putString(uid, nowuser)
        editor.apply()
        editor.commit()
    }

    fun getAuctionerDatainSPref(ctx: Context, uid: String): Auctioners? {
        val sharedPrefrences = ctx.getSharedPreferences("Auctioners", 0)
        val usernow = sharedPrefrences.getString(uid, null)
        val gson = Gson()
        val saveduser = gson.fromJson<Auctioners>(usernow, Auctioners::class.java)
        return saveduser
    }



    fun getAlluserFromDb(ctx: Context, UserTypeString: String): ArrayList<Users> {
        val userList = ArrayList<Users>()
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Users").document("UserData")
            .collection(UserTypeString)
            .addSnapshotListener { snapShots, e ->
                if (e != null) {
                    Log.d("NoData", "No data in bidders list")
                    return@addSnapshotListener
                }
                for (dc in snapShots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val bidder = dc.document.toObject(Users::class.java)
                            userList.add(bidder)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val bidder = dc.document.toObject(Users::class.java)
                            userList.forEachIndexed { index, users ->
                                if (bidder.uid.equals(users.uid))
                                    userList[index] = users
                            }

                        }
                        DocumentChange.Type.REMOVED -> {
                            val bidder = dc.document.toObject(Users::class.java)
                            userList.forEachIndexed { index, users ->
                                if (bidder.uid.equals(users.uid))
                                    userList.removeAt(index)
                            }
                        }
                    }

                }
            }
        return userList
    }

}



