package com.example.i_auction.Fragments


import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.i_auction.DashboardActivity
import com.example.i_auction.Models.Users

import com.example.i_auction.R
import com.example.i_auction.mySharedPref
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_login.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class LoginFragment : Fragment() {
    private var userid: String? = null
    // lateinit var clickBtn:Button
    lateinit var gotoRegister: TextView
    lateinit var loginBtn: Button
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var dialogBox: Dialog
    lateinit var loginRadio: RadioGroup
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        loginBtn = view.findViewById(R.id.signin_btn)
        email = view.findViewById(R.id.login_email)
        password = view.findViewById(R.id.login_password)
        loginRadio = view.findViewById(R.id.login_radio)
        dialogBox = Dialog(activity!!)
        dialogBox.setContentView(R.layout.dialog_r)
        dialogBox.setCancelable(false)
        val title = dialogBox.findViewById<TextView>(R.id.dialog_title)
        val message = dialogBox.findViewById<TextView>(R.id.dialog_message)
        message.setText("Please wait...")
        title.setText("Logging In")
        //clickBtn = view.findViewById(R.id.goto_register)
        gotoRegister = view.findViewById(R.id.goto_register)
        gotoRegister.setOnClickListener {
            replaceLoginToRegister()
        }
        loginBtn.setOnClickListener {
            checkFields()
        }
        return view
    }
    // end of onCreate

    // replacing login fragment to Register Fragment
    private fun replaceLoginToRegister() {
        activity!!.supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_main, RegisterFragment())
            .commit()
    }

    // checking if fields are empty
    private fun checkFields() {
        if (email.text.isNullOrEmpty()) {
            email.setError("Please input email")
            return
        }
        if (password.text.isNullOrEmpty()) {
            password.setError("Please input password")
            return
        }
        if (loginRadio.checkedRadioButtonId.equals(radio_auctioner_login.id)) {
            dialogBox.show()
            checkUserCredentials(email.text.toString(), password.text.toString())

        } else if (loginRadio.checkedRadioButtonId.equals(radio_bidder_login.id)) {
            dialogBox.show()
            checkUserCredentials(email.text.toString(), password.text.toString())
        } else {
            Toast.makeText(activity, "Please select account type", Toast.LENGTH_SHORT).show()
            return
        }
    }

    // checking user in firebaseFireStore
    private fun checkUserCredentials(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                mySharedPref.userId = it.user.uid
                userid = it.user.uid
                when (loginRadio.checkedRadioButtonId) {
                    radio_bidder_login.id -> checkUserInBidderData()
                    radio_auctioner_login.id -> checkUserInAuctionerData()
                    // null -> Toast.makeText(activity,"Please select account type",Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                dialogBox.dismiss()
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                return@addOnFailureListener
            }
    }

    private fun checkUserInBidderData() {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Users")
            .document("UserData")
            .collection("Bidder")
            .document(mySharedPref.userId!!)
            .get()
            .addOnSuccessListener {
                if (it != null && it.exists()) sendUsertoDashboard(it)
                else postErrorUserNotFound()
            }
            .addOnFailureListener {
                dialogBox.dismiss()
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserInAuctionerData() {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Users")
            .document("UserData")
            .collection("Auctioner")
            .document(mySharedPref.userId!!)
            .get()
            .addOnSuccessListener {
                if (it != null && it.exists()) {
                    val user = it.toObject(Users::class.java)
                    mySharedPref().setUserinsharedPref(activity!!,user!!.uid, user)
                    checkAuctionerDetail(it)
                }
                else postErrorUserNotFound()
            }
            .addOnFailureListener {
                dialogBox.dismiss()
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun checkAuctionerDetail(snapShot: DocumentSnapshot) {
        val dbRef = FirebaseFirestore.getInstance()
        dbRef.collection("Users").document("AuctionerDetails")
            .collection("Auctioners").document(userid!!)
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    dialogBox.dismiss()
                    sendUsertoDashboard(snapShot)
                } else {
                    dialogBox.dismiss()
                    showAuctionerDetailFragment()
                }
            }
            .addOnFailureListener {
                dialogBox.dismiss()
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
            }
    }

    fun showAuctionerDetailFragment() {
        activity!!.supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_main, auctionerDetailsFragment())
            .commit()
    }

    fun sendUsertoDashboard(it: DocumentSnapshot) {
        val user: Users = it.toObject(Users::class.java)!!
        mySharedPref().setUserinsharedPref(activity!!, user.uid, user)
        dialogBox.dismiss()
        Toast.makeText(activity, "Login Success", Toast.LENGTH_SHORT).show()
        startActivity(Intent(activity, DashboardActivity::class.java))
        activity?.finish()
    }

    fun postErrorUserNotFound() {
        dialogBox.dismiss()
        Toast.makeText(activity, "Account doesn't exist!", Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
    }
}