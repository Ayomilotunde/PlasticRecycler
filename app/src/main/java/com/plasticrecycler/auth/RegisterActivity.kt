package com.plasticrecycler.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.plasticrecycler.MainActivity
import com.plasticrecycler.R

class RegisterActivity : AppCompatActivity() {
    lateinit var mDatabase : DatabaseReference
    var mAuth = FirebaseAuth.getInstance()
    var user = FirebaseAuth.getInstance().currentUser
    private var mProgress: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mProgress =  ProgressDialog(this);

        mDatabase = FirebaseDatabase.getInstance().getReference("Users")

        val buttonSignUp = findViewById<Button>(R.id.btnSignUp)

        buttonSignUp.setOnClickListener {
            mProgress!!.setTitle("Registering");
            mProgress!!.setMessage("Please wait");
            mProgress!!.show();
            mProgress!!.setCanceledOnTouchOutside(false);
            registerUser()
        }
    }

    private fun registerUser () {
        val emailTxt = findViewById<View>(R.id.edtSignUpEmail) as EditText
        val passwordTxt = findViewById<View>(R.id.edtSignUpPassword) as EditText
        val nameTxt = findViewById<View>(R.id.edtSignUpFullName) as EditText

        var email = emailTxt.text.toString()
        var password = passwordTxt.text.toString()
        var name = nameTxt.text.toString()

        if (!email.isEmpty() && !password.isEmpty() && !name.isEmpty()) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this
            ) { task ->
                if (task.isSuccessful) {
                    mProgress!!.dismiss();
                    val user = mAuth.currentUser
                    val uid = user!!.uid
                    mDatabase.child(uid).child("fullname").setValue(name)
                    mDatabase.child(uid).child("uid").setValue(uid)
                    startActivity(Intent(this, MainActivity::class.java))
                    Toast.makeText(this, "Successfully registered :)", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        this,
                        "Error registering, try again later :(",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }else {
            Toast.makeText(this,"Please fill up the Credentials :|", Toast.LENGTH_LONG).show()
        }
    }
}