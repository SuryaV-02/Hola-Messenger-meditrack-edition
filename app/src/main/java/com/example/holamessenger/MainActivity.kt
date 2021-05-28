package com.example.holamessenger

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
    var selectedPhotoUri : Uri? = null
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
        Toast.makeText(this, "Registration to be implemented", Toast.LENGTH_SHORT).show()
    }
}