package com.example.holamessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val edt_email = findViewById<EditText>(R.id.edt_email)
        val edt_textPassword = findViewById<EditText>(R.id.edt_textPassword)
        val btn_login = findViewById<Button>(R.id.btn_login)
        val tv_register = findViewById<TextView>(R.id.tv_register)

        btn_login.setOnClickListener {
            val email = edt_email.text.toString()
            val password = edt_textPassword.text.toString()
            Toast.makeText(this, "Login to be implemented", Toast.LENGTH_SHORT).show()
        }
        tv_register.setOnClickListener {
            finish()
        }
    }
}