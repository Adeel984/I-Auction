package com.example.i_auction.Fragments


import android.app.*
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.i_auction.Models.Items

import com.example.i_auction.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_post_item.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class post_itemFragment : Fragment() {
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var timeLast: String? = null
    var itemNameVal: String = ""
    var itemCatVal: String = ""
    var itemBrandVal: String = ""
    val defaultVal = "---"
    var GALLERY_IMAGE_REQ_CODE = 44
    var GALLERY_PERMISSION_CODE = 22
    var SELECTED_PHOTO_URI: Uri? = null
    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    lateinit var itemImage: ImageView
    lateinit var itemCategory: Spinner
    lateinit var itemBrand: Spinner
    lateinit var itemName: Spinner
    lateinit var itemDesc: EditText
    lateinit var itemMinBidAmount: EditText
    lateinit var timeOpener: ImageView
    lateinit var end_bid_time: TextView
    lateinit var submitBtn: Button
    lateinit var dialog: Dialog
    lateinit var timePicker: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dialog = Dialog(activity!!)
        dialog.setContentView(R.layout.dialog_r)
        val title = dialog.findViewById<TextView>(R.id.dialog_title)
        val message = dialog.findViewById<TextView>(R.id.dialog_message)
        title.setText("Uploading Item")
        message.setText("Please wait...")
        val view = inflater.inflate(R.layout.fragment_post_item, container, false)
        timePicker = view.findViewById(R.id.time_opener)
        itemCategory = view.findViewById(R.id.spinner_itemCategory)
        itemImage = view.findViewById(R.id.item_image_bidder_view)
        itemBrand = view.findViewById(R.id.spinner_itemBrand)
        itemName = view.findViewById(R.id.spinner_itemName)
        itemDesc = view.findViewById(R.id.item_description)
        itemMinBidAmount = view.findViewById(R.id.min_bid)
        timeOpener = view.findViewById(R.id.time_opener)
        end_bid_time = view.findViewById(R.id.time_last)
        submitBtn = view.findViewById(R.id.post_item_btn)
        timePicker.setOnClickListener {
            openDateDialogue()
        }
        addvaluesToSpinners()
        addListenersToDropDown()
        itemImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    activity!!,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, GALLERY_IMAGE_REQ_CODE)
            }
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_PERMISSION_CODE
            )
        }
        submitBtn.setOnClickListener {
            checkFields()
        }
        return view
    }


    private fun openDateDialogue() {
//        dialog.setContentView(R.layout.calender_view_layout)
//        dialog.window?.setLayout(1050, 1050)
        val now = Calendar.getInstance()
        val format = SimpleDateFormat("dd MMM yyyy", Locale.US)
        var datePicker: DatePickerDialog? = null
        datePicker = DatePickerDialog(activity!!, DatePickerDialog.OnDateSetListener { view, year, month, dayofMonth ->
            val dateInstance = Calendar.getInstance()
            dateInstance.set(Calendar.YEAR, year)
            dateInstance.set(Calendar.MONTH, month)
            dateInstance.set(Calendar.DAY_OF_MONTH, dayofMonth)
            selectedDate = format.format(dateInstance.time)
            datePicker?.dismiss()
            showTimePicker()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun showTimePicker() {
        var timePickerDialog: TimePickerDialog? = null
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val now = Calendar.getInstance()
        timePickerDialog = TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { view, hourofDay, minutes ->
            val timeInstance = Calendar.getInstance()
            timeInstance.set(Calendar.HOUR_OF_DAY, hourofDay)
            timeInstance.set(Calendar.MINUTE, minutes)
            selectedTime = timeFormat.format(timeInstance.time)
            timeLast = "$selectedDate $selectedTime"
            time_last?.text = timeLast
            //Toast.makeText(this@ToDoTasksActivity,"Date is: $selectedDate & Time is: $selectedTime",Toast.LENGTH_SHORT).show()
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false)
        timePickerDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            GALLERY_IMAGE_REQ_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageuri = data!!.getData()
//                    val input:InputStream? = this@RegisterActivity.contentResolver.openInputStream(imageuri)
                    //                  val bitmap = BitmapFactory.decodeStream(input)
                    val bitmap = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, imageuri)
                    val newb = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                    itemImage.setImageBitmap(newb)
//                    userImg?.alpha = 0f
                    SELECTED_PHOTO_URI = imageuri
                    //         userImg.visibility = View.INVISIBLE
                }

            }
        }
    }

    private fun addvaluesToSpinners() {
        ArrayAdapter.createFromResource(activity!!, R.array.category_items_array, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                itemCategory.adapter = adapter
            }
        ArrayAdapter.createFromResource(activity!!, R.array.item_brand_array, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                itemBrand.adapter = adapter
            }
        ArrayAdapter.createFromResource(activity!!, R.array.item_name_array, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                itemName.adapter = adapter
            }
    }

    private fun addListenersToDropDown() {
        itemName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {


            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                itemNameVal = parent!!.getItemAtPosition(position).toString()
            }
        }
        itemBrand.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                itemBrandVal = parent!!.getItemAtPosition(position).toString()
            }
        }
        itemCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                itemCatVal = parent!!.getItemAtPosition(position).toString()
            }
        }
    }

    private fun checkFields() {
        if (itemDesc.text.isNullOrEmpty()) {
            itemDesc.setError("Please provide brief justification")
            return
        }
        if (itemMinBidAmount.text.isNullOrEmpty()) {
            itemMinBidAmount.setError("Please provide min amout")
            return
        }
        if (itemBrandVal.equals(defaultVal)) {
            Toast.makeText(activity, "Please Select Item Brand", Toast.LENGTH_SHORT).show()
            return
        }
        if (itemNameVal.equals(defaultVal)) {
            Toast.makeText(activity, "Please Select Item Name", Toast.LENGTH_SHORT).show()
            return
        }
        if (itemCatVal.equals(defaultVal)) {
            Toast.makeText(activity, "Please Select Item Category", Toast.LENGTH_SHORT).show()
            return
        }
        if (timeLast == null) {
            time_last.setError("")
            time_last.text = "Please select End time"
            return
        }
        if (SELECTED_PHOTO_URI == null) {
            Toast.makeText(activity, "Please select image", Toast.LENGTH_LONG).show()
            return
        }
        dialog.show()
        showDialog()

//        if (spinnerIndexes[0].equals(0)) {
//            Toast.makeText(activity, "Please select Item Name", Toast.LENGTH_SHORT).show()
//            return
//        }
//        if (spinnerIndexes[1].equals(0)) {
//            Toast.makeText(activity, "Please select Item Brand", Toast.LENGTH_SHORT).show()
//            return
//        }
//        if (spinnerIndexes[2].equals(0)) {
//            Toast.makeText(activity, "Please select Item Category", Toast.LENGTH_SHORT).show()
//            return
//        }


        //itemCategory:Spinner
        //    lateinit var itemBrand:Spinner
        //    lateinit var itemName:
    }

    private fun showDialog() {
        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle("Start selling")
        dialog.setMessage("Do you want to start start selling your item right now?")
        dialog.setPositiveButton("Later", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                postItemToDb(true)
            }

        })
        dialog.setNegativeButton("Now", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
            postItemToDb(false)
            }
        })
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun uploadImageToStorage(dbRef: DocumentReference) {
        val fireStorageRef = FirebaseStorage.getInstance().getReference("itemImages/" + dbRef.id)
        fireStorageRef.putFile(SELECTED_PHOTO_URI!!)
            .addOnSuccessListener {
                fireStorageRef.downloadUrl.addOnSuccessListener {
                    dbRef.update("itemImageUri", it.toString())
                    clearFields()
                    dialog.dismiss()
                    Toast.makeText(activity, "Item added Successfully", Toast.LENGTH_SHORT).show()

                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun clearFields() {
        itemDesc.text.clear()
        itemMinBidAmount.setText("0")
        itemName.setSelection(0)
        itemBrand.setSelection(0)
        itemCategory.setSelection(0)
        SELECTED_PHOTO_URI = null
        activity!!.supportFragmentManager
            .beginTransaction()
            .replace(R.id.dashboard_container, auctioner_homeFragment())
            .commit()
    }

    private fun postItemToDb(upcoming: Boolean) {
        val dbRef = FirebaseFirestore.getInstance().collection("Items").document()
        val item = Items(
            dbRef.id,
            userId,
            itemNameVal,
            itemBrandVal,
            itemCatVal,
            itemDesc.text.trim().toString(),
            null,
            itemMinBidAmount.text.toString(),
            null,
            timeLast!!,
            false,
            null,
            false,
            upcoming,
            HashMap()
        )
        dbRef.set(item)
            .addOnSuccessListener {
                uploadImageToStorage(dbRef)
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}


