package com.plasticrecycler.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.plasticrecycler.MainActivity
import com.plasticrecycler.R

class LoginActivity : AppCompatActivity() {
    var mAuth = FirebaseAuth.getInstance()
    var user = FirebaseAuth.getInstance().currentUser
    private var mProgress: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mProgress =  ProgressDialog(this);

        val buttonSignIn = findViewById<Button>(R.id.btnSignIn)
        val register = findViewById<TextView>(R.id.textRegister)

        buttonSignIn.setOnClickListener {
            mProgress!!.setTitle("Logging In");
            mProgress!!.setMessage("Please wait");
            mProgress!!.show();
            mProgress!!.setCanceledOnTouchOutside(false);
            login()
        }
        register.setOnClickListener { sendToRegister() }

    }

    private fun sendToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun login () {
        val emailTxt = findViewById<View>(R.id.edtSignInEmail) as EditText
        var email = emailTxt.text.toString()
        val passwordTxt = findViewById<View>(R.id.edtSignInPassword) as EditText
        var password = passwordTxt.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            this.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener ( this
            ) { task ->
                if (task.isSuccessful) {
                    mProgress!!.dismiss();
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    Toast.makeText(this, "Successfully Logged in :)", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error Logging in :(", Toast.LENGTH_SHORT).show()
                }
            }

        }else {
            Toast.makeText(this, "Please fill up the Credentials :|", Toast.LENGTH_SHORT).show()
        }
    }
}