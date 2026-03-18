package com.smartspender.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.smartspender.app.UserDao
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailField = findViewById<EditText>(R.id.etRegEmail)
        val phoneField = findViewById<EditText>(R.id.etRegPhone)
        val passwordField = findViewById<EditText>(R.id.etRegPassword)
        val registerBtn = findViewById<Button>(R.id.btnRegisterAccount)

        registerBtn.setOnClickListener {
            val email = emailField.text.toString()
            val phone = phoneField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@RegisterActivity)

                // Create the user object
                val newUser = User(email = email, phone = phone, password = password)

                // Save to Room Database
                db.userDao().registerUser(newUser)

                Toast.makeText(this@RegisterActivity, "Account Created! Please Login", Toast.LENGTH_LONG).show()
                finish() // Goes back to Login screen
            }
        }
    }
}