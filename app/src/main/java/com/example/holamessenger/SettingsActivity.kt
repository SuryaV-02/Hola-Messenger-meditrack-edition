package com.example.holamessenger

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.net.URL


class SettingsActivity : AppCompatActivity() {
    companion object{
        var USER_NAME = LatestMessagesActivity.currentUser?.username
        var USER_PROFILE_IMAGE_URL = LatestMessagesActivity.currentUser?.profileImageUrl
        var USER_ID = LatestMessagesActivity.currentUser?.uid

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Log.i("SETTINGS_TAG","$USER_PROFILE_IMAGE_URL")
        setContentView(R.layout.activity_settings)

        DownloadImageFromInternet(findViewById(R.id.iv_profile_image_settings)).execute("$USER_PROFILE_IMAGE_URL")

        val profile_image = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_profile_image_settings)
        val tv_userName_settings = findViewById<TextView>(R.id.tv_userName_settings)
        val btn_signout_settings = findViewById<TextView>(R.id.btn_signout_settings)
        tv_userName_settings.text = USER_NAME
        btn_signout_settings.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this,login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }


    }
}