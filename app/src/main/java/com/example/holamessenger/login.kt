package com.example.holamessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)
        val btn_login = findViewById<Button>(R.id.btn_login)
        val tv_register = findViewById<TextView>(R.id.tv_register)

        btn_login.setOnClickListener {
            performLogin()
        }
        tv_register.setOnClickListener {
            finish()
        }
    }


    private fun performLogin() {
        val edt_email = findViewById<EditText>(R.id.edt_email)
        val edt_textPassword = findViewById<EditText>(R.id.edt_textPassword)
        val email = edt_email.text.toString()
        val password = edt_textPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill All the details!", Toast.LENGTH_SHORT).show()
            return
        }

        val pb_loginActivity = findViewById<ProgressBar>(R.id.pb_loginActivity)
        pb_loginActivity.visibility = View.VISIBLE

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                pb_loginActivity.visibility = View.INVISIBLE
                Log.i("SKHST", "Successfully logged in: ${it.result?.user?.uid}")
                val intent = Intent(this,LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                pb_loginActivity.visibility = View.INVISIBLE
                Toast.makeText(this, "Failed to log in: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.i("SKHST", "Successfully logged in: ${it.message}")
            }
    }
}