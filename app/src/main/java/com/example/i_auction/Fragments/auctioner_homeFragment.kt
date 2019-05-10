package com.example.i_auction.Fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.i_auction.Adapters.ItemsRVAdapter
import com.example.i_auction.Models.Items

import com.example.i_auction.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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
        val auctionerClicked: (views: View, position: Int) -> Unit = { view, position ->
            when (view.id) {
                R.id.re_bid_btn -> openOrCloseBidItem(false, position)
                R.id.close_bid_btn -> openOrCloseBidItem(true, position)
            }
        }

        adapter = ItemsRVAdapter(activity!!, itemList, auctionerClicked)
        recyler.layoutManager = LinearLayoutManager(activity!!)
        recyler.adapter = adapter
        getAllItems()
        return view
    }

    private fun openOrCloseBidItem(itemSold: Boolean, position: Int) {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Items").document(itemList[position].itemId)
            .update("soldOut", itemSold)
    }

    private fun getAllItems() {
        val dbRef = FirebaseFirestore.getInstance()
        val registeration = dbRef.collection("Items")
            .whereEqualTo("itemUploaderId",userId)
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
