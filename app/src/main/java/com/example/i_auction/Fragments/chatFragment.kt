package com.example.i_auction.Fragments


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.i_auction.Adapters.ChatAdapter
import com.example.i_auction.Models.ChatMessage

import com.example.i_auction.R
import com.example.i_auction.mySharedPref.Companion.chatName
import com.example.i_auction.mySharedPref.Companion.toUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 *
 */
class chatFragment : Fragment() {
    lateinit var recyler: RecyclerView
    lateinit var adapter: ChatAdapter
    var msgsList = ArrayList<ChatMessage>()
    val fromId = FirebaseAuth.getInstance().currentUser?.uid
    var chatId:String? = null
    val dbRef = FirebaseFirestore.getInstance()
    lateinit var txtMsg:EditText
    lateinit var sendBtn:ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        chatId = fromId + "-" + toUser?.uid
        val list = chatId!!.split("-")
        val sorted = list.sorted()
        chatId = sorted[0] + "-" + sorted[1]
        Toast.makeText(activity, toUser?.userName, Toast.LENGTH_SHORT).show()
        recyler = view.findViewById(R.id.chat_RV)
        recyler.layoutManager = LinearLayoutManager(activity!!)
        adapter = ChatAdapter(activity!!, msgsList)
        recyler.adapter = adapter
        chatId = sorted[0] + "-" + sorted[1]
        txtMsg = view.findViewById(R.id.textMsg)
        sendBtn = view.findViewById(R.id.send_btn_chatLog)
        sendBtn.setOnClickListener {
            if(txtMsg.text.isNotEmpty())
            sendMsgtoDB()
        }
        getMsgData()

//        val nameView =view.findViewById<TextView>(R.id.name)
//        nameView.text = chatName

        return view
    }

    private fun sendMsgtoDB() {
        val msgID = System.currentTimeMillis().toString()
        val msgsData = ChatMessage(msgID,txtMsg.text.toString(),fromId!!,toUser?.uid!!,true)
        dbRef.collection("Messages").document(chatId!!)
            .collection("MsgData").document(msgID)
            .set(msgsData)
            .addOnSuccessListener {
                txtMsg.text.clear()
                recyler.scrollToPosition(adapter.itemCount -1)
            }
            .addOnFailureListener {
                Toast.makeText(activity,it.message,Toast.LENGTH_LONG).show()
            }
    }

    private fun getMsgData() {
        dbRef.collection("Messages").document(chatId!!)
            .collection("MsgData")
            .whereEqualTo("msgSent",true)
            .addSnapshotListener{snapShot, e ->
                if (e != null) {
                    Log.d("Nodata", "No data")
                    return@addSnapshotListener
                }
                for (dc in snapShot!!.documentChanges) {
                    when(dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val msgData = dc.document.toObject(ChatMessage::class.java)
                            msgsList.add(msgData)
                            adapter.notifyDataSetChanged()
                            recyler.scrollToPosition(adapter.itemCount -1)
                        }
                        DocumentChange.Type.MODIFIED -> {

                        }
                        DocumentChange.Type.REMOVED -> {

                        }
                    }
                }

            }
          //  .collection("MsgData").document()
//            .get()
//            .addOnSuccessListener {
//                if(it.exists() && it != null) {
//                    val msgData = it.toObject(ChatMessage::class.java)
//                    Toast.makeText(activity,msgData.toString(),Toast.LENGTH_SHORT).show()
//                    msgsList.add(msgData!!)
//                    adapter.notifyDataSetChanged()
//                }
//                else Toast.makeText(activity,"Data doesn't exitst",Toast.LENGTH_LONG).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(activity!!,it.message,Toast.LENGTH_LONG).show()
//            }
//            .addSnapshotListener(EventListener<DocumentSnapshot> { querySnapshot, e ->
//            if(e != null) {
//                Log.d("Error","No Data")
//                return@EventListener
//            }
//                for(dc in querySnapshot.documentChanges) {
//                    when(dc.type) {
//                        DocumentChange.Type.ADDED -> {
//
//                        }
//
//                    }
//                }
//
//            })
//
//
//    }


    }
}