package com.example.holamessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            Toast.makeText(this, "Please fill out email/pw.", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show()
                Log.i("SKHST", "Successfully logged in: ${it.result?.user?.uid}")
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to log in: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.i("SKHST", "Successfully logged in: ${it.message}")
            }
    }
}