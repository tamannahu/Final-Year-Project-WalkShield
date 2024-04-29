package com.example.signuppract

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.signuppract.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


//class SignUpActivity extends AppCompatActivity
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth;
    val db = Firebase.firestore
    private val TAG = "MyActivity"



    override fun onCreate(savedInstanceState: Bundle?) {
        //creating the layout for the activity and setting it as the content view
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
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



      //set button listeners, send user inputs to signup method
        binding.signupButton.setOnClickListener {
            val signupUsername = binding.signupUsername.text.toString()
            val signupPassword = binding.signupPassword.text.toString()

            if (signupUsername.isNotEmpty() && signupPassword.isNotEmpty()){
                signupUser(signupUsername,signupPassword)
            } else {
                Toast.makeText(this@SignUpActivity,"All fields are mandatory",Toast.LENGTH_SHORT).show()
            }
        }
        //set login redirect button listener
        binding.loginRedirect.setOnClickListener {
            startActivity(Intent(this@SignUpActivity,LoginActivity::class.java))
            finish()
        }

    }

    private fun signupUser(username : String, password: String){
        //auth code
        auth.createUserWithEmailAndPassword(username,password)
            .addOnSuccessListener { authResult ->
                // Successfully signed up, now write user data to Firestore
                val user = authResult.user // Get the FirebaseUser object

                // Create a HashMap to store user data

                val userData = hashMapOf(

                    "email" to (authResult.user?.email ?: "")
                    // You can add more user data here as needed
                )

                // Add user data to Firestore under 'users' collection with UID as document ID
                if (user != null){
                    db.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            // User data successfully written to Firestore
                            println("User data successfully written to Firestore.")
                        }
                        .addOnFailureListener { e ->
                            // Failed to write user data to Firestore
                            println("Error writing user data to Firestore: $e")
                        }

                // Create subcollections under 'requests' collection with UID as document ID
                val requestsRef = db.collection("requests").document(user.uid)

                requestsRef.collection("user_data").document("user_info")
                    .set(userData)
                    .addOnSuccessListener {
                        // User data successfully written to Firestore under user_data collection
                        println("User data successfully written to Firestore under user_data collection.")
                    }
                    .addOnFailureListener { e ->
                        // Failed to write user data to Firestore
                        println("Error writing user data to Firestore under user_data collection: $e")
                    }

                requestsRef.collection("ownRequests").document()
                    .set(hashMapOf<String, Any>()) // Empty document
                    .addOnSuccessListener {
                        // Empty document successfully written to Firestore under ownRequests collection
                        println("Empty document successfully written to Firestore under ownRequests collection.")
                    }
                    .addOnFailureListener { e ->
                        // Failed to write empty document to Firestore
                        println("Error writing empty document to Firestore under ownRequests collection: $e")
                    }

                requestsRef.collection("friendRequests").document()
                    .set(hashMapOf<String, Any>()) // Empty document
                    .addOnSuccessListener {
                        // Empty document successfully written to Firestore under friendRequests collection
                        println("Empty document successfully written to Firestore under friendRequests collection.")
                    }
                    .addOnFailureListener { e ->
                        // Failed to write empty document to Firestore
                        println("Error writing empty document to Firestore under friendRequests collection: $e")
                    }

                requestsRef.collection("allFriends").document()
                    .set(hashMapOf<String, Any>()) // Empty document
                    .addOnSuccessListener {
                        // Empty document successfully written to Firestore under allFriends collection
                        println("Empty document successfully written to Firestore under allFriends collection.")
                    }
                    .addOnFailureListener { e ->
                        // Failed to write empty document to Firestore
                        println("Error writing empty document to Firestore under allFriends collection: $e")
                    }


                    //set up data structure for locationData
                    val initialData = hashMapOf(
                        "isSharing" to false,
                        "latitude" to 0L,
                        "longitude" to 0L
                    )

                    val locationDataRef = db.collection("locationData").document(user.email.toString())
                    locationDataRef.set(initialData).addOnSuccessListener { println("Location data set up") }
                        .addOnFailureListener { println("Error setting up location data structure") }





                }







        Toast.makeText(this@SignUpActivity,"Signup Successful",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@SignUpActivity,MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                // Failed to sign up user
                println("Error signing up user: $e")
            }





    }
}