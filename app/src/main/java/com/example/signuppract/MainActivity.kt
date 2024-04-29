package com.example.signuppract
//originally empty views activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import android.os.Handler
import android.os.HandlerThread
import android.view.MotionEvent
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MyMainActivity"
    private lateinit var auth: FirebaseAuth;
    private lateinit var sosButton: SOSButton

    private var startTime = 0L
    private val scope = CoroutineScope(Dispatchers.Main) // Use the Main dispatcher for UI interactions


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()


        sosButton = findViewById(R.id.SOSButton)
        sosButton.setOnLongClickListener{
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:999")
            startActivity(intent)

            true // Consumes the long click event

        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()

            // After signing out, navigate to the LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Finish the current activity to prevent the user from going back
            finish()
        }

        val followMeButton = findViewById<Button>(R.id.followMeButton)
        followMeButton.setOnClickListener{
            val Intent = Intent(this,FollowMeActiv::class.java)
            startActivity(Intent)
        }

        val supportButton = findViewById<Button>(R.id.support)
        supportButton.setOnClickListener {
            val Intent = Intent(this,SupportActivity::class.java)
            startActivity(Intent)
        }

        val friendsButton = findViewById<Button>(R.id.friendsButton)
        friendsButton.setOnClickListener {
            val Intent = Intent(this,FriendsActivity::class.java)
            startActivity(Intent)
        }

        val mapButton = findViewById<Button>(R.id.seeMapButton)
        mapButton.setOnClickListener {
            val Intent = Intent(this,MapActivity::class.java)
            startActivity(Intent)
        }

        val seeMessagesButton = findViewById<Button>(R.id.seeMessages)
        seeMessagesButton.setOnClickListener {
            val Intent = Intent(this,Messages::class.java)
            startActivity(Intent)

        }


    }


}