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
    var categorisedList = ArrayList<Items>()
    var itemsList = ArrayList<Items>()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var value = 0
    var maxVal = 0
    var categorisedItems: Boolean = false
    lateinit var wait_dialog: Dialog
    lateinit var dialog: Dialog
    lateinit var bidderClicked: (views: View, position: Int) -> Unit
    lateinit var recyler: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bidder__home, container, false)
        val categorySpinner = view.findViewById<Spinner>(R.id.bidder_home_spinner)

        ArrayAdapter.createFromResource(activity!!, R.array.category_items_array, android.R.layout.simple_spinner_item)
            .also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = it
            }
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

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

        }
        recyler = view.findViewById(R.id.bidder_main_rv)
        recyler.layoutManager = LinearLayoutManager(activity!!)
        wait_dialog = Dialog(activity!!)
        dialog = Dialog(activity!!)
        bidderClicked = { views, position ->
            when (views.id) {
                R.id.bid_now_view -> showBidDialogue(position)
                R.id.withdraw_bid_view -> {
                    if (categorisedItems) applyOrWithDrawFromBid(true, position, "0", categorisedList)
                    else applyOrWithDrawFromBid(true, position, "0", itemsList)
                }
            }
        }
        adapter = ItemsRVAdapter(activity!!, itemsList, bidderClicked)
        recyler.layoutManager = LinearLayoutManager(activity!!)
        recyler.adapter = adapter
        getAllItems()
        return view
    }

    private fun showCategorisedItems(names: String, categorised: Boolean) {
        when (categorised) {
            true -> {
                adapter = ItemsRVAdapter(activity!!, categorisedList, bidderClicked)
                recyler.adapter = adapter
                categorisedList.clear()
                itemsList.forEach { item ->
                    if (item.itemCategory.equals(names)) {
                        Log.d("Category", names)
                        categorisedList.add(item)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            else -> {
                adapter = ItemsRVAdapter(activity!!, itemsList, bidderClicked)
                recyler.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }


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
        var item = Items()
        when (categorisedItems) {
            true -> item = categorisedList[position]
            else -> item = itemsList[position]
        }
        value = item.min_bid_amount.toInt()
        try {
            bidValue.setText(value.toString())
            Picasso.get().load(item.itemImageUri).into(itemImage)
            minBidAmount.setText(item.min_bid_amount)
            maxBidAmount.setText(item.max_bid_amount)
            itemName.text = item.itemName
            item.bidded_users?.values?.forEach {
                if (it >= maxVal) {
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
            when (categorisedItems) {
                true -> applyOrWithDrawFromBid(false, position, bidValue.text.toString(), categorisedList)
                else -> applyOrWithDrawFromBid(false, position, bidValue.text.toString(), itemsList)
            }
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
                appliedUsersList.put(userId!!, value)
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


    private fun getAllItems() {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Items")
            .whereEqualTo("soldOut", false)
            .whereEqualTo("upcoming", false)
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
                            Log.d("Item", item.toString())
                            adapter.notifyDataSetChanged()
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val item = dc.document.toObject(Items::class.java)
                            itemsList.forEachIndexed { index, items ->
                                if (items.itemId.equals(item.itemId)) {
                                    //categorisedList.add(index, item)
                                    itemsList[index] = item
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
                                itemsList.forEachIndexed { index, items ->
                                    if (items.itemId.equals(item?.itemId)) {
                                        itemsList.removeAt(index)
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
}
