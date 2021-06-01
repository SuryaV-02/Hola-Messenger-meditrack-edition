package com.example.holamessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import java.util.*

class MainActivity : AppCompatActivity() {
    var selectedPhotoUri : Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val btn_register = findViewById<Button>(R.id.btn_register)
        val tv_login = findViewById<TextView>(R.id.tv_login)
        val btn_uploadPhoto =findViewById<Button>(R.id.btn_uploadPhoto)

        btn_register.setOnClickListener {
            performRegister()
        }
        tv_login.setOnClickListener {
            val i = Intent(this,login::class.java)
            startActivity(i)
        }
        btn_uploadPhoto.setOnClickListener {
            Toast.makeText(this, "Select photo", Toast.LENGTH_SHORT).show()
            val i = Intent(Intent.ACTION_PICK)
            i.type= "image/*"
            startActivityForResult(i,0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode== Activity.RESULT_OK && data!=null){
            val btn_uploadPhoto =findViewById<Button>(R.id.btn_uploadPhoto)
            val profile_image =findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profile_image)
            Log.i("info","Photo Selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            //val bitmapDrawable = BitmapDrawable(bitmap)
            profile_image.setImageBitmap(bitmap)
            profile_image.visibility = View.VISIBLE
            btn_uploadPhoto.visibility = View.GONE
        }
    }

    private fun performRegister() {
        val edt_email = findViewById<EditText>(R.id.edt_email)
        val edt_textPassword = findViewById<EditText>(R.id.edt_textPassword)
        val email = edt_email.text.toString()
        val password = edt_textPassword.text.toString()
        if(email.isEmpty() && password.isEmpty()){
            Toast.makeText(this, "Enter All details", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{
                if(!it.isSuccessful){
                    Toast.makeText(this, "Registration not successful", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                Log.i("SKHST","Successfully created user with uid ${it.result?.user?.uid}")
                Toast.makeText(this, "Registration Success", Toast.LENGTH_SHORT).show()
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT).show()
                Log.i("SKHST","Failed to create user ${it.message}")
            }
    }
    fun uploadImageToFirebaseStorage(){
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.i("SKHST","SUCCESS Image Upload to Storage ${it.metadata?.path}")
                ref.downloadUrl
                    .addOnSuccessListener {
                        Log.i("SKHST","File Location : $it")
                        SaveUserToFirebaseDatabase(it.toString())
                    }
            }
            .addOnFailureListener{
                Log.i("SKHST","FAILED Image Upload to Storage ${it.message}")
            }

    }
    fun SaveUserToFirebaseDatabase(profileImageUrl : String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val username = findViewById<EditText>(R.id.edt_name).text.toString()
        val user = User(uid,username,profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.i("SKHST","SUCCESS Finally we saved data to Database SKHST!!!")
                val intent = Intent(this,LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.i("SKSHT","FAILED to upload to database ${it.message}")
            }


    }
}
@Parcelize
class User(val uid : String, val username : String, val profileImageUrl: String): Parcelable{
    constructor() : this("","","")
}