package com.example.i_auction.Fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.i_auction.Adapters.ItemsRVAdapter
import com.example.i_auction.Models.Items
import com.example.i_auction.R
import com.example.i_auction.mySharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore


class bidders_bidItems : Fragment() {
    private val categorisedList = ArrayList<Items>()
    private var categorisedItems: Boolean = false
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var spinner: Spinner
    private lateinit var adapter: ItemsRVAdapter
    private val itemList = ArrayList<Items>()
    private lateinit var viewClick: (view: View, position: Int) -> Unit
    lateinit var recyler: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bidders_bid_items, container, false)
        viewClick = { views, position ->
            when (views.id) {
                R.id.withdraw_bid_view -> {
                    if (categorisedItems) applyOrWithDrawFromBid(position, categorisedList)
                    else applyOrWithDrawFromBid(position, itemList)
                }
                R.id.contact_auctioner -> {
                    if (categorisedItems) {
                        mySharedPref.toId = categorisedList[position].itemUploaderId
                    } else {
                        mySharedPref.toId = itemList[position].itemUploaderId
                    }
                    activity!!
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.dashboard_container, chatFragment())
                        .addToBackStack("nothing")
                        .commit()
                }
            }
        }
        //Initialising views
        spinner = view.findViewById(R.id.myItem_Spinner)
        ArrayAdapter.createFromResource(activity!!, R.array.bidders_choice, android.R.layout.simple_spinner_item)
            .also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = it
            }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    categorisedItems = false
                    getSelectedData(position)
                } else {
                    categorisedItems = true
                    getSelectedData(position)
                }
            }
        }
        recyler = view.findViewById(R.id.biddersItemsRV)
        recyler.layoutManager = GridLayoutManager(activity!!, 2)
        adapter = ItemsRVAdapter(activity!!, itemList, viewClick)
        recyler.adapter = adapter
        if (itemList.size.equals(0)) getAllBiddersBidItesm()
        return view
    }

    private fun getSelectedData(position: Int) {
        categorisedList.clear()
        when (categorisedItems) {
            true -> {
                when (position) {
                    1 -> {
                        // Bidder's Purchased items
                        itemList.forEach { item ->
                            if (item.purchaserId != null && item.purchaserId.equals(userId)) {
                                categorisedList.add(item)
                                adapter = ItemsRVAdapter(activity!!, categorisedList, viewClick)
                                recyler.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                            else {
                                adapter = ItemsRVAdapter(activity!!, categorisedList, viewClick)
                                recyler.adapter = adapter
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                    2 -> {
                        // Bidder's Pending bid data which has not yet been accepted by auctioner
                        itemList.forEach { item ->
                            item.bidded_users!!.values.forEach {
                                if (it.bidder_id.equals(userId) && it.accepted_bid == false) {
                                    categorisedList.add(item)
                                    adapter = ItemsRVAdapter(activity!!, categorisedList, viewClick)
                                    recyler.adapter = adapter
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                    3 -> {
                        // Bidder's accepted bid data
                        itemList.forEach { item ->
                            item.bidded_users!!.values.forEach {
                                if (it.bidder_id.equals(userId) && it.accepted_bid == true) {
                                    categorisedList.add(item)
                                    adapter = ItemsRVAdapter(activity!!, categorisedList, viewClick)
                                    recyler.adapter = adapter
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                adapter = ItemsRVAdapter(activity!!, itemList, viewClick)
                recyler.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }

    }

    private fun getAllBiddersBidItesm() {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Items")
            .whereEqualTo("soldOut", false)
            .addSnapshotListener { snapShot, e ->
                if (e != null) {
                    Log.d("No Data", "No Data")
                    return@addSnapshotListener
                }
                for (dc in snapShot!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val item = dc.document.toObject(Items::class.java)
                            item.bidded_users!!.values.forEach { bidData ->
                                if (bidData.bidder_id.equals(userId)) {
                                    itemList.add(item)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val item = dc.document.toObject(Items::class.java)
                            itemList.forEachIndexed { index, items ->
                                if (item.itemId == items.itemId) {
                                    itemList.removeAt(index)
                                    adapter.notifyDataSetChanged()
                                    return@addSnapshotListener
                                }
                            }
                            if (categorisedItems) {
                                categorisedList.forEachIndexed { index, items ->
                                    if (item.itemId == items.itemId) {
                                        categorisedList.removeAt(index)
                                        adapter.notifyDataSetChanged()
                                        return@addSnapshotListener
                                    }
                                }

                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            for (doc in snapShot.documents) {
                                val item = doc.toObject(Items::class.java)
                                itemList.forEachIndexed { index, items ->
                                    if (items.itemId.equals(item?.itemId)) {
                                        itemList.removeAt(index)
                                        adapter.notifyItemRemoved(index)
                                    }
                                }
                                if (categorisedItems) {
                                    categorisedList.forEachIndexed { index, items ->
                                        categorisedList.removeAt(index)
                                        adapter.notifyItemRemoved(index)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun applyOrWithDrawFromBid(
        position: Int,
        list: ArrayList<Items>
    ) {
        //Setting user in applied users list
        val dbRef = FirebaseFirestore.getInstance()
        mySharedPref.appliedUsersList.remove(userId)
        dbRef.collection("Items").document(list[position].itemId)
            .update("bidded_users", mySharedPref.appliedUsersList)
            .addOnSuccessListener {
                mySharedPref.appliedUsersList.clear()
                Toast.makeText(activity!!, "Success!", Toast.LENGTH_SHORT).show()
                adapter.notifyItemChanged(position)
            }
            .addOnFailureListener {
                Toast.makeText(activity!!, it.message, Toast.LENGTH_LONG).show()
            }
    }
}