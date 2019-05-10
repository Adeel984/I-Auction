package com.example.i_auction

import android.content.Context
import android.content.Intent
import android.hardware.input.InputManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.example.i_auction.Fragments.LoginFragment
import com.example.i_auction.Fragments.RegisterFragment
import com.example.i_auction.Fragments.auctionerDetailsFragment
import com.example.i_auction.Models.Users
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(this.intent != null)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container_main, LoginFragment()).commit()
         else
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container_main, RegisterFragment())
                .commit()

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setLogo(R.drawable.logo)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.title = "I-Auction"
        supportActionBar?.setDisplayShowTitleEnabled(true)
        //if user is logged in start dashboard activity
        when (userLoggedIn()) {
            true -> {
                val userId = FirebaseAuth.getInstance().currentUser!!.uid
                val user: Users? = mySharedPref().getUserfromsharedPref(this, userId)
                when (user!!.accType) {
                    enums.AUCTIONER.value -> {
                        if (mySharedPref().getAuctionerDatainSPref(this, userId) != null) {
                            startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                            finish()
                        } else supportFragmentManager.beginTransaction().replace(
                            R.id.container_main,
                            auctionerDetailsFragment()
                        ).commit()
                    }
                    enums.BIDDER.value -> {
                        startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }

    // checking wether user is logged in or not
    fun userLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser?.uid != null
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}
