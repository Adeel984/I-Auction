package com.example.i_auction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.widget.Toast
import com.example.i_auction.Fragments.*
import com.example.i_auction.Models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    var currentUser: Users? = null
    lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        toolbar = findViewById(R.id.toolbar)
        navView = findViewById(R.id.nav_view)
        setSupportActionBar(toolbar)
        val userid = FirebaseAuth.getInstance().currentUser?.uid
        currentUser = mySharedPref().getUserfromsharedPref(this, userid!!)
        checkCurrentUser(currentUser)
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
        drawerLayout = findViewById(R.id.drawer_layout)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
    }

    private fun checkCurrentUser(currentUser: Users?) {
        when (currentUser!!.accType) {
            enums.BIDDER.value -> {
                supportFragmentManager.beginTransaction()
                    .add(R.id.dashboard_container, bidder_HomeFragment())
                    .commit()
                navView.menu.setGroupVisible(R.id.bidder_menu, true)
            }
            enums.AUCTIONER.value -> {
                val dbRef = FirebaseFirestore.getInstance()
                dbRef.collection("Users").document("AuctionerDetails")
                    .collection("Auctioners")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener {
                        if(it !=null && it.exists()) {
                            supportFragmentManager.beginTransaction()
                                .add(R.id.dashboard_container, auctioner_homeFragment())
                                .commit()
                        } else supportFragmentManager.beginTransaction().replace(
                            R.id.dashboard_container,
                            auctionerDetailsFragment()
                        ).commit()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@DashboardActivity,it.message, Toast.LENGTH_LONG).show()
                    }
                navView.menu.setGroupVisible(R.id.auctioner_menu, true)
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar itemData clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.sign_out -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view itemData clicks here.
        // Handle navigation view itemData clicks here.
        when (item.itemId) {
            //Auctioner drawer listeners
            R.id.post_item -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.dashboard_container, post_itemFragment())
                    .commit()

            }
            R.id.auctioner_items -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.dashboard_container, auctioner_homeFragment())
                    .commit()
            }
            // Bidder drawer Listener

            R.id.upcoming_items -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.dashboard_container,upcomingAuctionsFragment())
                    .commit()
            }

            R.id.bidder_items -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.dashboard_container,bidder_HomeFragment())
                    .commit()
            }
            R.id.nav_share -> {

            }
            R.id.cell_phones -> {

            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}