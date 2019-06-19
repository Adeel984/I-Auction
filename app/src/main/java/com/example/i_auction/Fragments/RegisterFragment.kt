package com.example.i_auction.Fragments


import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.i_auction.Models.Users
import com.example.i_auction.R
import com.example.i_auction.enums
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_register.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import id.zelory.compressor.Compressor
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class RegisterFragment : Fragment() {
    private var SELECTED_PHOTO_URI: Uri? = null
    private val GALLERY_PERMISSION_CODE: Int = 55
    lateinit var click_btn: Button
    lateinit var goto_Login: TextView
    lateinit var image: CircleImageView
    lateinit var userName: EditText
    lateinit var userEmail: EditText
    lateinit var userPass: EditText
    lateinit var confirmPass: EditText
    lateinit var radioGrp: RadioGroup
    val GALLERY_IMAGE_REQ_CODE = 22
    var accType: Int? = null
    lateinit var dialogBox:Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        dialogBox = Dialog(activity!!)
        dialogBox.setContentView(R.layout.dialog_r)
        dialogBox.setCancelable(false)
        val title = dialogBox.findViewById<TextView>(R.id.dialog_title)
        val message = dialogBox.findViewById<TextView>(R.id.dialog_message)
        message.setText("Please wait...\nThis may take a while")
        title.setText("Creating Account")
        click_btn = view.findViewById(R.id.register_btn)
        userName = view.findViewById(R.id.register_name)
        userEmail = view.findViewById(R.id.register_email)
        userPass = view.findViewById(R.id.password)
        confirmPass = view.findViewById(R.id.confirm_password)
        radioGrp = view.findViewById(R.id.register_radio)
        goto_Login = view.findViewById(R.id.goto_login)
        image = view.findViewById(R.id.register_img)
        image.setOnClickListener {
            getImage()
        }
        goto_Login.setOnClickListener {
            activity!!.supportFragmentManager.beginTransaction().replace(R.id.container_main, LoginFragment()).commit()
        }
        click_btn.setOnClickListener {
            checkFields()
        }
        return view
    }

    private fun checkFields() {
        val userEmailText = userEmail.text.toString()
        val userPassText = userPass.text.toString()
        if (userName.text.isNullOrEmpty()) {
            userName.setError("Please input your name")
            return
        }
        if (userEmail.text.isNullOrEmpty()) {
            userEmail.setError("Please input your email")
            return
        }
        if (userPass.text.isNullOrEmpty()) {
            userPass.setError("Please input password")
            return
        }
        if (userPass.text.length <= 5) {
            userPass.setError("Please input password in 6 or more characters")
            return
        }
        if (!userPass.text.toString().equals(confirmPass.text.toString())) {
            confirmPass.setError("Password do not match")
            return
        }
        if (radioGrp.checkedRadioButtonId.equals(radio_bidder.id)) {
            accType = enums.BIDDER.value
            dialogBox.show()
            registerToAuth(userEmailText, userPassText)
        } else if (radioGrp.checkedRadioButtonId.equals(radio_auctioner.id)) {
            accType = enums.AUCTIONER.value
            dialogBox.show()
            registerToAuth(userEmailText, userPassText)
        } else {
            Toast.makeText(activity, "Please select account type", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun registerToAuth(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val fileName = FirebaseAuth.getInstance().uid
                val storageRef = FirebaseStorage.getInstance().getReference("/images/$fileName")
                if (SELECTED_PHOTO_URI != null) {
                    val bmp = MediaStore.Images.Media.getBitmap(activity!!.getContentResolver(), SELECTED_PHOTO_URI);
                    val baos = ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                    val data = baos.toByteArray();
                    //uploading the image
                    storageRef.putBytes(data)
                        .addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener {
                                saveUserTodb(it.toString())
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                            dialogBox.dismiss()
                            return@addOnFailureListener
                        }
                } else {
                    saveUserTodb(null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                dialogBox.dismiss()
                return@addOnFailureListener
            }

    }

    private fun saveUserTodb(imageUri: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val user = Users(userId!!, userName.text.toString(), userEmail.text.toString(), accType, imageUri)
        val dbRef = FirebaseFirestore.getInstance()
        if(accType!!.equals(enums.AUCTIONER.value)) {
            dbRef.collection("Users").document("UserData")
                .collection("Auctioner").document(userId)
                .set(user)
                .addOnSuccessListener {
                      Toast.makeText(activity,"Registered Successfully!",Toast.LENGTH_SHORT).show()
                    clearFields()
                    dialogBox.dismiss()
                    FirebaseAuth.getInstance().signOut()
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.container_main, LoginFragment()).commit()
                }
                .addOnFailureListener {
                    Toast.makeText(activity,it.message,Toast.LENGTH_SHORT).show()
                    dialogBox.dismiss()
                    return@addOnFailureListener
                }
        } else {
            dbRef.collection("Users").document("UserData")
                .collection("Bidder").document(userId)
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(activity,"Registered Successfully!",Toast.LENGTH_SHORT).show()
                    clearFields()
                    dialogBox.dismiss()
                    FirebaseAuth.getInstance().signOut()
                    activity!!.supportFragmentManager.beginTransaction().replace(R.id.container_main, LoginFragment()).commit()
                }
                .addOnFailureListener {
                    Toast.makeText(activity,it.message,Toast.LENGTH_SHORT).show()
                    dialogBox.dismiss()
                    return@addOnFailureListener
                }
        }
    }

    private fun clearFields() {
        userEmail.text.clear()
        userName.text.clear()
        userPass.text.clear()
        confirmPass.text.clear()
        SELECTED_PHOTO_URI = null
        image.setImageBitmap(null)
        userName.requestFocus()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GALLERY_IMAGE_REQ_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val imageuri = data!!.getData()
                   // val bitFill = File(imageuri?.toString())
                    val bytes = ByteArrayOutputStream();
                    val bitmap = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, imageuri)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bytes);
                   // val path = MediaStore.Images.Media.insertImage(activity!!.getContentResolver(),bitmap, imageuri?.toString(),null)


                    //CompressImage
//                    val bmp = BitmapFactory.decodeFile(data.dataString)
//                    val bos = ByteArrayOutputStream();
//                    bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
//                    val inp:InputStream = ByteArrayInputStream(bos.toByteArray());
                   // ContentBody foto = new InputStreamBody(inp, "image/jpeg", "filename");
//                    val input:InputStream? = this@RegisterActivity.contentResolver.openInputStream(imageuri)
                    //                  val bitmap = BitmapFactory.decodeStream(input)

                    //val newb = Bitmap.createScaledBitmap(bitmap, 100, 100, true);
                    image.setImageBitmap(bitmap)
//                    userImg?.alpha = 0f
                    //SELECTED_PHOTO_URI = path.toUri()
                    SELECTED_PHOTO_URI = imageuri
                }
            }
        }
    }

    private fun getImage() {
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
}