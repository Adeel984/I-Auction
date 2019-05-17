package com.example.i_auction.Fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.i_auction.Adapters.BiddersAdapter
import com.example.i_auction.Adapters.ItemsRVAdapter
import com.example.i_auction.Models.Items
import com.example.i_auction.Models.Users
import com.example.i_auction.NameEnums
import com.example.i_auction.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.i_auction.mySharedPref
import com.example.i_auction.mySharedPref.Companion.appliedUsersList
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot

class auctioner_homeFragment : Fragment() {

    lateinit var adapter: ItemsRVAdapter
    var itemList: ArrayList<Items> = ArrayList()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auctioner_home, container, false)
        val recyler: RecyclerView = view.findViewById(R.id.auctioner_main_rv)
        val auctionerClicked: (views: View, position: Int) -> Unit = { views, position ->
            when (views.id) {
                R.id.re_bid_btn -> openOrCloseBidItem(false, position)
                R.id.close_bid_btn -> openOrCloseBidItem(true, position)
                R.id.item_cardView -> showBids(position)
            }
        }

        recyler.layoutManager = LinearLayoutManager(activity!!)
        adapter = ItemsRVAdapter(activity!!, itemList, auctionerClicked)
        recyler.adapter = adapter
        getAllItems()
        return view
    }

    private fun showBids(position: Int) {
        appliedUsersList.putAll(itemList[position].bidded_users!!)
        val userList = ArrayList<Users?>()
        var counter = 0
        appliedUsersList.keys.forEach { uid ->
            val user = mySharedPref().getUsersFromFirebasefireStore(uid, NameEnums.Bidder.names)
            if(user !=null) {
                userList.add(user)
                Toast.makeText(activity!!,user.toString(),Toast.LENGTH_SHORT).show()
            } else Toast.makeText(activity!!,"No user",Toast.LENGTH_SHORT).show()
            counter++
        }
        val dialog = Dialog(activity!!)
//        dialog.window.setLayout(400,400)
        dialog.setContentView(R.layout.bidders_view_layout)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.bids_RV)
        val bidCountView = dialog.findViewById<TextView>(R.id.bids_count)
        bidCountView.setText(counter.toString())
        val bidsArray = ArrayList<Int>()
        mySharedPref.appliedUsersList.values.forEach { bidAmount ->
            bidsArray.add(bidAmount)
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        val adapter = BiddersAdapter(activity!!, userList, bidsArray)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
        dialog.show()

    }

    private fun openOrCloseBidItem(itemSold: Boolean, position: Int) {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Items").document(itemList[position].itemId)
            .update("soldOut", itemSold)
    }

    fun getAllItems() {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Items")
            .whereEqualTo("itemUploaderId", userId)
            .addSnapshotListener(EventListener<QuerySnapshot> { snapshots, e ->
                if (e != null) {
                    Log.d("Nodata", "No data")
                    return@EventListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val item = dc.document.toObject(Items::class.java)
                            itemList.add(item)
                            adapter.notifyDataSetChanged()
                            Log.d("item", item.toString())
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val item = dc.document.toObject(Items::class.java)
                            itemList.forEachIndexed { index, items ->
                                if (items.itemId.equals(item.itemId)) {
                                    //itemList.add(index, item)
                                    itemList[index] = item
                                    adapter.notifyItemChanged(index)
                                }
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            for (doc in snapshots.documents) {
                                val item = doc.toObject(Items::class.java)
                                itemList.forEachIndexed { index, items ->
                                    if (items.itemId.equals(item?.itemId)) {
                                        itemList.removeAt(index)
                                        //        adapter.notifyItemRemoved(index)
                                        adapter.notifyItemRemoved(index)
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }
}
