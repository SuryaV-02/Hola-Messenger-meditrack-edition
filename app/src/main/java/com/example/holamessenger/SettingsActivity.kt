package com.example.holamessenger

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.URL


class SettingsActivity : AppCompatActivity() {
    companion object{
        var USER_NAME = LatestMessagesActivity.currentUser?.username
        var USER_PROFILE_IMAGE_URL = LatestMessagesActivity.currentUser?.profileImageUrl
        var USER_ID = LatestMessagesActivity.currentUser?.uid
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("SETTINGS_TAG","$USER_PROFILE_IMAGE_URL")
        setContentView(R.layout.activity_settings)
        val profile_image = findViewById<ImageView>(R.id.iv_profile_image_settings)
        DownloadImageFromInternet(findViewById(R.id.iv_profile_image_settings)).execute("$USER_PROFILE_IMAGE_URL")
//        try {
//            val url = URL(USER_PROFILE_IMAGE_URL)
//            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
//            val image: Drawable = BitmapDrawable(this.resources, bitmap)
//            profile_image.background = image
//            Log.i("SETTINGS_TAG","success")
//        } catch (e: Exception) {
//            Log.i("SETTINGS_TAG","SRIVALA")
//            e.printStackTrace()
//        }
    }
    @SuppressLint("StaticFieldLeak")
    private inner class DownloadImageFromInternet(var imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        init {
            Toast.makeText(applicationContext, "Please wait, it may take a few minute...",     Toast.LENGTH_SHORT).show()
        }
        override fun doInBackground(vararg urls: String): Bitmap? {
            val imageURL = urls[0]
            var image: Bitmap? = null
            try {
                val `in` = java.net.URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)
            }
            catch (e: Exception) {
                Log.e("Error Message", e.message.toString())
                e.printStackTrace()
            }
            return image
        }
        override fun onPostExecute(result: Bitmap?) {
            imageView.setImageBitmap(result)
        }
    }


}
