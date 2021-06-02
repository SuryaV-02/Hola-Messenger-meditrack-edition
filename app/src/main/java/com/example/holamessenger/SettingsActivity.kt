package com.example.holamessenger

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class SettingsActivity : AppCompatActivity() {
    var selectedPhotoUri : Uri? = null
    //val progressDialogBar = ProgressBar(this)
    //val dialog = ProgressDialog(this)
    companion object{
        var USER_NAME = LatestMessagesActivity.currentUser?.username
        var USER_PROFILE_IMAGE_URL = LatestMessagesActivity.currentUser?.profileImageUrl
        var USER_ID = LatestMessagesActivity.currentUser?.uid
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("SETTINGS_TAG","$USER_PROFILE_IMAGE_URL")
        setContentView(R.layout.activity_settings)
        val pb_settings = findViewById<ProgressBar>(R.id.pb_settings)
        pb_settings.visibility = View.VISIBLE

        // TODO: 02-Jun-21 check this out!
        DownloadImageFromInternet(findViewById(R.id.iv_profile_image_settings),findViewById(R.id.pb_settings)).execute("$USER_PROFILE_IMAGE_URL")
//        DownloadAndSetProfileImage(
//            "$USER_PROFILE_IMAGE_URL")

        val profile_image = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_profile_image_settings)
        val tv_userName_settings = findViewById<TextView>(R.id.tv_userName_settings)
        val btn_signout_settings = findViewById<Button>(R.id.btn_signout_settings)
        val btn_okay_settings = findViewById<Button>(R.id.btn_okay_settings)



        tv_userName_settings.text = USER_NAME
        btn_signout_settings.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this,login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        btn_okay_settings.setOnClickListener {
            pb_settings.visibility = View.VISIBLE
            uploadImageToFirebaseStorage()
        }
        profile_image.setOnClickListener{
            Toast.makeText(this, "Select photo", Toast.LENGTH_SHORT).show()
            val i = Intent(Intent.ACTION_PICK)
            i.type= "image/*"
            startActivityForResult(i,0)
        }
        pb_settings.visibility = View.GONE

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode== Activity.RESULT_OK && data!=null){
            val profile_image =findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_profile_image_settings)
            Log.i("SETTINGS_TAG","Photo Selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            //val bitmapDrawable = BitmapDrawable(bitmap)
            profile_image.setImageBitmap(bitmap)

        }
    }
    fun uploadImageToFirebaseStorage(){
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.i("SETTINGS_TAG","SUCCESS Image Upload to Storage ${it.metadata?.path}")
                ref.downloadUrl
                    .addOnSuccessListener {
                        Log.i("SKHST","File Location : $it")
                        SaveUserToFirebaseDatabase(it.toString())
                    }
            }
            .addOnFailureListener{
                Log.i("SETTINGS_TAG","FAILED Image Upload to Storage ${it.message}")
            }

    }
    fun SaveUserToFirebaseDatabase(profileImageUrl : String){
        val ref = FirebaseDatabase.getInstance().getReference("/users/${com.example.holamessenger.SettingsActivity.Companion.USER_ID}/profileImageUrl")
        ref.setValue(profileImageUrl)
            .addOnSuccessListener {
                val pb_settings = findViewById<ProgressBar>(R.id.pb_settings)
                Log.i("SETTINGS_TAG","SUCCESS Finally we saved data to Database SKHST!!!")
                pb_settings.visibility = View.INVISIBLE

            }
            .addOnFailureListener {
                Log.i("SETTINGS_TAG","FAILED to upload to database ${it.message}")
            }
    }
    fun DownloadAndSetProfileImage(url: String){
        val imageURL = url
        val imageView = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_profile_image_settings)
        var image: Bitmap? = null
        try {
            val `in` = java.net.URL(imageURL).openStream()
            image = BitmapFactory.decodeStream(`in`)
            Log.i("SETTINGS_TAG","Try success")
        }
        catch (e: Exception) {
            Log.i("SETTINGS_TAG", e.message.toString())
            e.printStackTrace()
        }
        Log.i("SETTINGS_TAG","SETTING Image")
        imageView.setImageBitmap(image)
        Log.i("SETTINGS_TAG","ALL SET SKHST!")
    }
}

@SuppressLint("StaticFieldLeak")
class DownloadImageNow(var imageView: de.hdodenhof.circleimageview.CircleImageView
,var progressBar : ProgressBar) : AsyncTask<String, Void, Bitmap?>() {
    override fun doInBackground(vararg urls: String): Bitmap? {
        val imageURL = urls[0]
        var image: Bitmap? = null
        progressBar.visibility = View.VISIBLE
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
