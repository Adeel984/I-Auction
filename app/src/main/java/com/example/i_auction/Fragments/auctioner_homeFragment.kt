package com.example.i_auction.Fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    private val categorisedList = ArrayList<Items>()
    private var categorisedItems: Boolean = false
    private var madapter: BiddersAdapter? = null
    lateinit var auctionerClicked: (views: View, position: Int) -> Unit
    lateinit var recyler: RecyclerView
    private lateinit var adapter: ItemsRVAdapter
    var itemList: ArrayList<Items> = ArrayList()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    val userList = ArrayList<Users?>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auctioner_home, container, false)
        recyler = view.findViewById(R.id.auctioner_main_rv)
        val spinner: Spinner = view.findViewById(R.id.auctioner_item_spinner)
        ArrayAdapter.createFromResource(activity!!, R.array.item_name_array, android.R.layout.simple_spinner_item)
            .also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = it
            }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val item = parent?.getItemAtPosition(position).toString()
                if (position > 0) {
                    categorisedItems = true
                    showCategorisedItems(item, categorisedItems)
                } else {
                    categorisedItems = false
                    showCategorisedItems(item, categorisedItems)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }
        auctionerClicked = { views, position ->
            when (views.id) {
                R.id.re_bid_btn -> {
                    if (categorisedItems) openOrCloseBidItem(false, position, categorisedList)
                    else openOrCloseBidItem(false, position, itemList)
                }
                R.id.close_bid_btn -> {
                    if(categorisedItems) openOrCloseBidItem(true, position, categorisedList)
                    else openOrCloseBidItem(false, position, itemList)
                }


                R.id.item_cardView -> {
                    if(categorisedItems) showBids(position, categorisedList)
                    else showBids(position, itemList)
                }
            }
        }

        recyler.layoutManager = LinearLayoutManager(activity!!)
        adapter = ItemsRVAdapter(activity!!, itemList, auctionerClicked)
        recyler.adapter = adapter
        getAllItems()
        return view
    }

    private fun showCategorisedItems(name: String, categorisedItems: Boolean) {
        when (categorisedItems) {
            true -> {
                adapter = ItemsRVAdapter(activity!!, categorisedList, auctionerClicked)
                recyler.adapter = adapter
                categorisedList.clear()
                itemList.forEach { item ->
                    if (item.itemName.equals(name)) {
                        Log.d("Item Name", item.toString())
                        categorisedList.add(item)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            false -> {
                adapter = ItemsRVAdapter(activity!!, itemList, auctionerClicked)
                recyler.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showBids(position: Int, list:ArrayList<Items>) {
        appliedUsersList.clear()
        userList.clear()
        appliedUsersList.putAll(list[position].bidded_users!!)
//        Log.d("position",position.toString())
//        Log.d("itemCount", appliedUsersList.size.toString())

        var counter = 0
        appliedUsersList.keys.forEach { uid ->
            getUsersFromFirebasefireStore(uid, NameEnums.Bidder.names)
            // userList.add(user)

            // if(user.uid.isEmpty()) Toast.makeText(activity!!,"No user",Toast.LENGTH_SHORT).show()
            //   else {
            //   userList.add(user)
            //   //   madapter!!.notifyDataSetChanged()
            //      Toast.makeText(activity!!,user.toString(),Toast.LENGTH_SHORT).show()

            //(}
            counter++
        }
        val dialog = Dialog(activity!!)
//        dialog.window.setLayout(400,400)
        dialog.setContentView(R.layout.bidders_view_layout)
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.bids_RV)
        val bidCountView = dialog.findViewById<TextView>(R.id.bids_count)
        bidCountView.setText(counter.toString())
        val bidsArray = ArrayList<Int>()
        appliedUsersList.values.forEach { bidAmount ->
            bidsArray.add(bidAmount)
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)
        madapter = BiddersAdapter(activity!!, userList, bidsArray)
        recyclerView.adapter = madapter
        dialog.show()
    }

    private fun openOrCloseBidItem(itemSold: Boolean, position: Int, list: ArrayList<Items>) {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Items").document(list[position].itemId)
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
                                    //categorisedList.add(index, item)
                                    itemList[index] = item
                                    adapter.notifyItemChanged(index)
                                }
                            }
                            if (categorisedItems) {
                                categorisedList.forEachIndexed { indexs, catItem ->
                                    if (catItem.itemId.equals(item.itemId)) {
                                        categorisedList[indexs] = item
                                        adapter.notifyDataSetChanged()
                                    }
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
            })
    }

    fun getUsersFromFirebasefireStore(uid: String, nameString: String): Users {
        var user = Users()
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Users").document("UserData")
            .collection(nameString).document(uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    user = documentSnapshot.toObject(Users::class.java)!!
                    userList.add(user)
                    madapter!!.notifyDataSetChanged()
                    //Log.d("Users",user.toString())
                }
            }
        //if(user != null)
        //  return user
        //   else
        return user
    }
}
