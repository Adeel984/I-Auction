package com.example.i_auction.Fragments


import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.i_auction.DashboardActivity
import com.example.i_auction.Models.Auctioners

import com.example.i_auction.R
import com.example.i_auction.mySharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class auctionerDetailsFragment : Fragment() {
    lateinit var companyName: EditText
    lateinit var aboutCompany: EditText
    lateinit var designation: Spinner
    var designationText: String = ""
    lateinit var dialogBox: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_auctioner_details, container, false)
        companyName = view.findViewById(R.id.company_name)
        aboutCompany = view.findViewById(R.id.about_company)
        designation = view.findViewById(R.id.rank_auctioner)
        dialogBox = Dialog(activity!!)
        dialogBox.setCancelable(false)
        dialogBox.setContentView(R.layout.dialog_r)
        val title = dialogBox.findViewById<TextView>(R.id.dialog_title)
        val message = dialogBox.findViewById<TextView>(R.id.dialog_message)
        title.setText("Updating data")
        message.setText("Please wait...")
        val spinnerValues = arrayOf("CEO", "GM", "Managing Director", "Departmental Head")
        val arrayAdapter =
            ArrayAdapter<String>(activity!!, R.layout.support_simple_spinner_dropdown_item, spinnerValues)
        designation.adapter = arrayAdapter
        designation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                designationText = parent?.getItemAtPosition(position).toString()
                Log.d("Designation", designation.toString())
            }

        }
        val submitBtn = view.findViewById<Button>(R.id.submit_btn)
        submitBtn.setOnClickListener {
            checkFields()
        }
        return view
    }

    private fun checkFields() {
        if (companyName.text.isNullOrEmpty()) {
            companyName.setError("Invalid Field")
            return
        }
        if (aboutCompany.text.isNullOrEmpty()) {
            aboutCompany.setError("Invalid Field")
            return
        }
        addAuctionerData()
    }

    private fun addAuctionerData() {
        dialogBox.show()
        val uid = FirebaseAuth.getInstance().uid
        val user = mySharedPref().getUserfromsharedPref(activity!!, uid!!)
        val auctionerData = Auctioners(
            uid,
            user!!.userName, companyName.text.trim().toString(),
            designationText,
            user.email,
            user.imageUri,
            aboutCompany.text.trim().toString()
        )
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Users").document("AuctionerDetails")
            .collection("Auctioners").document(uid)
            .set(auctionerData)
            .addOnSuccessListener {
                dialogBox.dismiss()
                mySharedPref().setAuctionerDatainSPref(activity!!, uid, auctionerData)
                clearFields()
                Toast.makeText(activity, "Data added successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(activity!!,DashboardActivity::class.java))
                activity!!.finish()
            }
            .addOnFailureListener {
                dialogBox.dismiss()
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
            }

    }

    private fun clearFields() {
        companyName.text.clear()
        aboutCompany.text.clear()
        /// designation.set(0)
    }

}
