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
import com.example.i_auction.Adapters.ItemsRVAdapter
import com.example.i_auction.Models.Items
import com.example.i_auction.mySharedPref.Companion.appliedUsersList
import com.example.i_auction.R
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
class bidder_HomeFragment : Fragment() {
    lateinit var adapter: ItemsRVAdapter
    var itemList: ArrayList<Items> = ArrayList()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var value = 0
    var maxVal = 0
    lateinit var wait_dialog: Dialog
    lateinit var dialog:Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bidder__home, container, false)
        val recyler: RecyclerView = view.findViewById(R.id.bidder_main_rv)
        recyler.layoutManager = LinearLayoutManager(activity!!)
        wait_dialog = Dialog(activity!!)
        dialog = Dialog(activity!!)
        val bidderClicked: (views: View, position: Int) -> Unit = { views, position ->
            when (views.id) {
                R.id.bid_now_view -> showBidDialogue(position)
                R.id.withdraw_bid_view -> applyOrWithDrawFromBid(true, position, "0")
            }
        }
        adapter = ItemsRVAdapter(activity!!, itemList, bidderClicked)
        recyler.layoutManager = LinearLayoutManager(activity!!)
        recyler.adapter = adapter
        getAllItems()
        return view
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
        val item = itemList[position]
        value = item.min_bid_amount.toInt()
        try {
            bidValue.setText(value.toString())
            Picasso.get().load(item.itemImageUri).into(itemImage)
            minBidAmount.setText(item.min_bid_amount)
            maxBidAmount.setText(item.max_bid_amount)
            itemName.text = item.itemName
            item.bidded_users?.values?.forEach {
            if(it >=maxVal) {
                maxVal = it
            }
            }
            maxBidAmount.setText(maxVal.toString())
        } catch (e: Exception) {
            e.message
        }
        plusBtn.setOnClickListener {
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
            wait_dialog.setContentView(R.layout.dialog_r)
            wait_dialog.setCancelable(false)
            val title = wait_dialog.findViewById<TextView>(R.id.dialog_title)
            val message = wait_dialog.findViewById<TextView>(R.id.dialog_message)
            title.setText("Applying Bid")
            message.setText("Please Wait")
            wait_dialog.show()
            applyOrWithDrawFromBid(false, position, bidValue.text.toString())
        }
        dialog.show()
    }


    private fun applyOrWithDrawFromBid(withDraw: Boolean, position: Int, bidValue: String) {
        //Setting user in applied users list
        appliedUsersList.putAll(itemList[position].bidded_users!!)
      //  Toast.makeText(activity,"Applied Users ${appliedUsersList.toString()}",Toast.LENGTH_LONG).show()
        val dbRef = FirebaseFirestore.getInstance()
        when (withDraw) {
            true -> {
                appliedUsersList.remove(userId!!)
                dbRef.collection("Items").document(itemList[position].itemId)
                    .update("bidded_users", appliedUsersList)
                    .addOnSuccessListener {
                        appliedUsersList.clear()
                        wait_dialog.dismiss()
                        Toast.makeText(activity!!, "Success!", Toast.LENGTH_SHORT).show()
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
//                    dbRef.collection("Items").document(itemList[position].itemId).
//                        update("appliedUsers",appliedUsersList)
//                } else {
                appliedUsersList.put(userId!!, value)
                if(bidValue.toInt() > maxVal) {
                    dbRef.collection("Items").document(itemList[position].itemId)
                        .update("max_bid_amount", bidValue)
                }
                dbRef.collection("Items").document(itemList[position].itemId).update("bidded_users", appliedUsersList)
                    .addOnSuccessListener {
                        appliedUsersList.clear()
                        wait_dialog.dismiss()
                        dialog.dismiss()
                        Toast.makeText(activity!!, "Success!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        wait_dialog.dismiss()
                        Toast.makeText(activity!!, it.message, Toast.LENGTH_LONG).show()
                    }
            }
        }
    }


    private fun getAllItems() {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Items")
            .whereEqualTo("soldOut", false)
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
