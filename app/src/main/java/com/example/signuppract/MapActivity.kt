package com.example.signuppract

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var db : FirebaseFirestore
    private var currentMarker: Marker? = null
    private val markerMap: MutableMap<String, Marker> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Initialize db property
        db = FirebaseFirestore.getInstance()

        //creates a coroutine
        GlobalScope.launch {
            val friendsList = fetchAllFriends(currentUser)
            val sharingFriends = findSharing(friendsList)//return who is sharing their location in locationData
            for (sharingFriend in sharingFriends){
                println(sharingFriend)
                updateMarkerPeriodically(sharingFriend)
            }
        }
        mapFragment = supportFragmentManager.findFragmentById(R.id.followMeMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener{
            val Intent = Intent(this,MainActivity::class.java)
            startActivity(Intent)
        }
    }

    private suspend fun updateMarkerPeriodically(sharingFriend: String) {
        GlobalScope.launch(Dispatchers.Main) {
            var checkSharing = isSharing(sharingFriend)
            while (checkSharing) {
                val location = retrieveLatAndLong(sharingFriend)
                val friendLatLng = LatLng(location.latitude, location.longitude)
                googleMap.addMarker(MarkerOptions().position(friendLatLng).title(sharingFriend))
                kotlinx.coroutines.delay(5000) // Delay for 5 seconds
                checkSharing = isSharing(sharingFriend)
                googleMap.clear()
            }
        }
    }

    private suspend fun isSharing(sharingFriend: String): Boolean {
        return suspendCoroutine { continuation ->
            val locationRef = db.collection("locationData").document(sharingFriend)
            locationRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val isSharing = document.getBoolean("isSharing")
                        if (isSharing != null) {
                            continuation.resume(isSharing)
                        } else {
                            println("isSharing field is null for document: $sharingFriend")
                            continuation.resume(false) // Default value if isSharing field is null
                        }
                    } else {
                        println("Document does not exist for: $sharingFriend")
                        continuation.resume(false) // Default value if document doesn't exist
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error retrieving document for $sharingFriend: $exception")
                    continuation.resume(false) // Default value if there's an error
                }
        }
    }

    suspend fun retrieveLatAndLong(sharingFriend: String) : LatLng {
        return suspendCoroutine { continuation ->
            val locationRef = db.collection("locationData").document(sharingFriend)
            locationRef.get()
                .addOnSuccessListener { document ->
                    // Check if the document exists and contains latitude and longitude fields
                    if (document.exists()) {
                        val latitude = document.getDouble("latitude")
                        val longitude = document.getDouble("longitude")
                        if (latitude != null && longitude != null) {
                            // Latitude and longitude retrieved successfully
                            println("Latitude: $latitude, Longitude: $longitude")
                            val location = LatLng(latitude, longitude)
                            continuation.resume(location)
                        } else {
                            println("Latitude or longitude is null for document: $sharingFriend")
                        }
                    } else {
                        println("Document does not exist for: $sharingFriend")
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error retrieving document for $sharingFriend: $exception")
                }
        }
    }

    suspend fun findSharing(friendsList: List<String>):List<String> {
        return suspendCoroutine { continuation ->
       val locationDataRef = db.collection("locationData")

            val isSharingList = mutableListOf<String>()
            locationDataRef
            .whereIn(FieldPath.documentId(), friendsList)
            .whereEqualTo("isSharing", true)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Add document IDs to the list
                    isSharingList.add(document.id)
                }
                continuation.resume(isSharingList)
            }
            .addOnFailureListener { exception ->
                println("Error finding sharing documents: $exception")
                continuation.resume(isSharingList)
            }
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        val london = LatLng(51.5241, -0.0404)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(london, 20f))

    }
    suspend fun fetchAllFriends(currentUser: FirebaseUser?):List<String> {
       return suspendCoroutine { continuation ->
           val allFriendsRef = db.collection("requests")
               .document(currentUser?.uid.toString())
               .collection("allFriends")
           val friendsList = mutableListOf<String>()
           allFriendsRef.get()
               .addOnSuccessListener { documents ->
                   for (document in documents) {
                       val email = document.getString("email")
                       if (email != null && email.contains("@")) {
                           friendsList.add(document.id.toString())
                       }
                   }
                   // Code inside this block is executed only when the Firestore operation is successful
                   continuation.resume(friendsList)
               }
               .addOnFailureListener { exception ->
                   println("Error finding allfriends: $exception")
                   // Call continuation.resumeWithException if you want to propagate the exception
                   continuation.resume(friendsList) // Return the list even in case of failure
               }
       }
    }
}