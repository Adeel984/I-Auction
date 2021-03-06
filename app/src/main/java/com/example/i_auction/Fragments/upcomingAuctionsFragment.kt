package com.example.i_auction.Fragments


import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.i_auction.Adapters.ItemsRVAdapter
import com.example.i_auction.Models.Items
import com.example.i_auction.Models.Users
import com.example.i_auction.Models.bidders_bid_data
import com.example.i_auction.NameEnums

import com.example.i_auction.R
import com.example.i_auction.mySharedPref
import com.example.i_auction.mySharedPref.Companion.appliedUsersList
import com.example.i_auction.mySharedPref.Companion.toId
import com.example.i_auction.mySharedPref.Companion.toUser
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class upcomingAuctionsFragment : Fragment() {

    lateinit var wait_dialog: Dialog
    private var maxVal: Int = 0
    private var value: Int = 0
    private var categorisedItems: Boolean = false
    private val categorisedList = ArrayList<Items>()
    lateinit var dialog: Dialog
    private lateinit var adapter: ItemsRVAdapter
    private val itemsList = ArrayList<Items>()
    lateinit var recyler: RecyclerView
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    lateinit var bidderClicked: (views: View, position: Int) -> Unit
    var assuranceText:EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_upcoming_auctions, container, false)
        recyler = view.findViewById<RecyclerView>(R.id.upcoming_auction_Rv)
        recyler.layoutManager = GridLayoutManager(activity,2)
        dialog = Dialog(activity!!)
        wait_dialog = Dialog(activity!!)
        bidderClicked = { views, position ->
            when (views.id) {
                R.id.bid_now_view -> showBidDialogue(position)
                R.id.withdraw_bid_view -> {
                    when (categorisedItems) {
                        true -> applyOrWithDrawFromBid(true, position, "0", categorisedList)
                        else -> applyOrWithDrawFromBid(true, position, "0", itemsList)
                    }
                }
                R.id.contact_auctioner -> {
                    if(categorisedItems) toId = categorisedList[position].itemUploaderId
                    else toId = itemsList[position].itemUploaderId
//                    Handler().postDelayed({
//                    },300000)
                    activity!!
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.dashboard_container, chatFragment())
                        .addToBackStack("nothing")
                        .commit()
                    // Toast.makeText(activity,bool.toString(),Toast.LENGTH_SHORT).show()

//                    DashboardActivity().title = "MyName"
                }
            }
        }
        val searchView = view.findViewById<EditText>(R.id.search_mobiles)
        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(searchView.text) && TextUtils.getTrimmedLength(searchView.text).equals(0)) {
                    categorisedItems = false
                    showCategorisedItems(null, categorisedItems)
                } else {
                    categorisedItems = true
                    showCategorisedItems(searchView.text.toString(), categorisedItems)
                }
            }
        })
        adapter = ItemsRVAdapter(activity!!, itemsList, bidderClicked)
        recyler.adapter = adapter
        if(itemsList.size==0) getAllItems()
        return view
    }

    private fun getAllItems() {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Items")
            .whereEqualTo("soldOut", false)
            .whereEqualTo("upcoming", true)
            .whereEqualTo("withDraw", false)
            .addSnapshotListener(EventListener<QuerySnapshot> { snapshots, e ->
                if (e != null) {
                    Log.d("Nodata", "No data")
                    return@EventListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val item = dc.document.toObject(Items::class.java)
                            itemsList.add(item)
//                            getUser(item.itemUploaderId)
                            Log.d("Item", item.toString())
                            adapter.notifyDataSetChanged()
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val item = dc.document.toObject(Items::class.java)
                            itemsList.forEachIndexed { index, items ->
                                if (items.itemId.equals(item.itemId)) {
                                    //categorisedList.add(index, itemData)
                                    itemsList[index] = item
                                    adapter.notifyItemChanged(index)
                                }
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            for (doc in snapshots.documents) {
                                val item = doc.toObject(Items::class.java)
                                itemsList.forEachIndexed { index, items ->
                                    if (items.itemId.equals(item?.itemId)) {
                                        itemsList.removeAt(index)
                                        adapter.notifyItemRemoved(index)
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun showBidDialogue(position: Int) {

        dialog.setContentView(R.layout.bidder_show_popup)
        val itemName = dialog.findViewById<TextView>(R.id.item_name_bidder_view)
        val plusBtn = dialog.findViewById<Button>(R.id.plus_controller)
        val minBtn = dialog.findViewById<Button>(R.id.minus_controller)
        val bidValue = dialog.findViewById<EditText>(R.id.bid_value_bidder_view)
        val itemImage = dialog.findViewById<ImageView>(R.id.item_image_bidder_view)
        val minBidAmount = dialog.findViewById<TextView>(R.id.min_bid_amount_bidderView)
        val maxBidAmount = dialog.findViewById<TextView>(R.id.max_bid_amount_bidderView)
        val applyBid = dialog.findViewById<Button>(R.id.apply_bid)
        assuranceText = dialog.findViewById(R.id.assurance_bidder)
        val item: Items
        when (categorisedItems) {
            true -> item = categorisedList[position]
            else -> item = itemsList[position]
        }
        appliedUsersList.putAll(item.bidded_users!!)
        value = item.min_bid_amount.toInt()
        try {
            bidValue.setText(value.toString())
            Picasso.get().load(item.itemImageUri).into(itemImage)
            minBidAmount.setText(item.min_bid_amount)
            maxBidAmount.setText(item.max_bid_amount)
            itemName.text = item.itemName
            item.bidded_users?.values?.forEach {
                if (it.bidAmount >= maxVal) {
                    maxVal = it.bidAmount
                }
            }
            maxBidAmount.setText(maxVal.toString())
        } catch (e: Exception) {
            e.message
        }
        plusBtn.setOnClickListener {
            value = bidValue.text.toString().toInt()
            value += 1
            bidValue.setText(value.toString())
        }
        minBtn.setOnClickListener {
            if (value > item.min_bid_amount.toInt()) {
                value -= 1
                bidValue.setText(value.toString())
            } else {
                Toast.makeText(activity!!, "Bid value can not be less than Min Bid value", Toast.LENGTH_LONG).show()
            }
        }
        applyBid.setOnClickListener {
            value = bidValue.text.toString().toInt()
            if(assuranceText!!.text.isNullOrEmpty()) {
                Toast.makeText(activity,"Please provide assurance",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(value >= item.min_bid_amount.toInt()) {
                wait_dialog.setContentView(R.layout.dialog_r)
                wait_dialog.setCancelable(false)
                val title = wait_dialog.findViewById<TextView>(R.id.dialog_title)
                val message = wait_dialog.findViewById<TextView>(R.id.dialog_message)
                title.setText("Applying Bid")
                message.setText("Please Wait")
                wait_dialog.show()
                when (categorisedItems) {
                    true -> applyOrWithDrawFromBid(false, position, bidValue.text.toString(), categorisedList)
                    else -> applyOrWithDrawFromBid(false, position, bidValue.text.toString(), itemsList)
                }
            } else Toast.makeText(activity!!, "Bid value can not be less than Min Bid value", Toast.LENGTH_LONG).show()
        }
        dialog.show()
    }

    private fun applyOrWithDrawFromBid(
        withDraw: Boolean,
        position: Int,
        bidValue: String,
        list: ArrayList<Items>
    ) {
        //Setting user in applied users list

        val dbRef = FirebaseFirestore.getInstance()
        when (withDraw) {
            true -> {
                appliedUsersList.remove(userId!!)
                dbRef.collection("Items").document(list[position].itemId)
                    .update("bidded_users", appliedUsersList)
                    .addOnSuccessListener {
                        appliedUsersList.clear()
                        wait_dialog.dismiss()
                        Toast.makeText(activity!!, "Success!", Toast.LENGTH_SHORT).show()
                        adapter.notifyItemChanged(position)
                    }
                    .addOnFailureListener {
                        wait_dialog.dismiss()
                        Toast.makeText(activity!!, it.message, Toast.LENGTH_LONG).show()
                    }
            }
            false -> {
//                if(appliedUsersList.size.equals(1) && appliedUsersList[0].equals("")) {
//                    val dbRef = FirebaseFirestore.getInstance()
//                    appliedUsersList[0]= userId!!
//                    dbRef.collection("Items").document(categorisedList[position].itemId).
//                        update("appliedUsers",appliedUsersList)
//                } else {
                val bidData = bidders_bid_data(userId!!,assuranceText?.text.toString(),bidValue.toInt(),list[position].itemId,false)
                appliedUsersList.put(userId, bidData)
                if (bidValue.toInt() > maxVal) {
                    dbRef.collection("Items").document(list[position].itemId)
                        .update("max_bid_amount", bidValue)
                }
                dbRef.collection("Items").document(list[position].itemId)
                    .update("bidded_users", appliedUsersList)
                    .addOnSuccessListener {
                        appliedUsersList.clear()
                        wait_dialog.dismiss()
                        dialog.dismiss()
                        Toast.makeText(activity!!, "Success!", Toast.LENGTH_SHORT).show()
                        adapter.notifyItemChanged(position)
                    }
                    .addOnFailureListener {
                        wait_dialog.dismiss()
                        Toast.makeText(activity!!, it.message, Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun showCategorisedItems(names: String?, categorised: Boolean) {
        when (categorised) {
            true -> {
                adapter = ItemsRVAdapter(activity!!, categorisedList, bidderClicked)
                recyler.adapter = adapter
                categorisedList.clear()
                itemsList.forEach { item ->
                    val itembr = item.itemBrand.toLowerCase()
                    val text = names!!.toLowerCase()
                    // searching in itemData brand
                    if (itembr.equals(text) || itembr.contains(text)) {
                        Log.d("dataxx", names)
//                        auctionerList.forEach {auctioner ->
//                            if(item.itemUploaderId.equals(auctioner.uid))
//                                tempAuctionerList.add(auctioner)
//                        }
                        categorisedList.add(item)
                        adapter.notifyDataSetChanged()
                       // adapter.notifyDataSetChanged()
                    }
                    //searching in itemData description
                    val itemDesc = item.itemDesc.toLowerCase()
                    if(itemDesc.equals(text) || itemDesc.contains(text)) {
                        categorisedList.add(item)
                        //adapter.notifyDataSetChanged()
                    }
                    removeDuplicates(adapter)
                }
            }
            else -> {
                adapter = ItemsRVAdapter(activity!!, itemsList, bidderClicked)
                recyler.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun removeDuplicates(adapter: ItemsRVAdapter) {
        val newList = ArrayList<Items>()
        categorisedList.forEach {
            if(!newList.contains(it)) {
                newList.add(it)
            }
        }
        categorisedList.clear()
        categorisedList.addAll(newList)
        adapter.notifyDataSetChanged()
    }
//    private fun getUser(auctionerId: String) {
//        val dbRef = FirebaseFirestore.getInstance()
//        dbRef.collection("Users").document("UserData")
//            .collection(NameEnums.Auctioner.names).document(auctionerId)
//            .get()
//            .addOnSuccessListener {
//                if (it.exists()) {
//                    val auctioner = it.toObject(Users::class.java)
//                    //auctionerList.add(auctioner!!)
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
//            }
//    }
}
