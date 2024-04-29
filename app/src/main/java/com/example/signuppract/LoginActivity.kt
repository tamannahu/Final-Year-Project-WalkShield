package com.example.signuppract

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.signuppract.databinding.ActivityLogin2Binding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

//class LoginUpActivity extends AppCompatActivity
class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLogin2Binding

    private lateinit var auth: FirebaseAuth;
    private val TAG = "MyLoginActivity"




    override fun onCreate(savedInstanceState: Bundle?) {
        //creating the layout for the activity and setting it as the content view
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Firebase.firestore
        auth = Firebase.auth
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                println("user logged in: ${user.email}")

            } else {
                println("user logged out")
            }
        }
        //button listeners here
        binding.loginButton.setOnClickListener {
            val loginUsername = binding.loginUsername.text.toString()
            val loginPassword = binding.loginPassword.text.toString()

            if (loginUsername.isNotEmpty() && loginPassword.isNotEmpty()){
                loginUser(loginUsername,loginPassword)
            } else {
                Toast.makeText(this@LoginActivity,"All fields are mandatory",Toast.LENGTH_SHORT).show()
            }
        }

        binding.signupRedirect.setOnClickListener {
            startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
            finish()
        }
    }

    private fun loginUser(username:String,password:String){
        auth.signInWithEmailAndPassword(username,password)
            .addOnSuccessListener { authResult ->
                Toast.makeText(this@LoginActivity,"Login Successful",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                finish()
            } .addOnFailureListener { e ->
                Log.e(TAG, "User login failed: ${e.message}")
                // Handle the failure case if needed
                }




    }
}