package com.example.signuppract

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class FollowMeActiv : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap
    private lateinit var db : FirebaseFirestore
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentMarker: Marker? = null
    private var lastKnownLocation: LatLng? = null
    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            interval = 5000 // Update location every 5 seconds
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_me)
        db = FirebaseFirestore.getInstance()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        mapFragment = supportFragmentManager.findFragmentById(R.id.followMeMap) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val sosButton = findViewById<SOSButton>(R.id.SOSButton)
        sosButton.setOnLongClickListener{
            val intent = Intent(Intent.ACTION_DIAL) // Use ACTION_DIAL for emergency dialer
            intent.data = Uri.parse("tel:999") // Use "tel:emergency" for emergency dialer
            startActivity(intent)

            true
        }

        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener{
            val Intent = Intent(this,MainActivity::class.java)
            startActivity(Intent)
         }

        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {

            setSharingStatus(true)
            startLocationUpdates()

            Toast.makeText(this@FollowMeActiv,"Sharing Location", Toast.LENGTH_SHORT).show()

        }

        val endButton = findViewById<Button>(R.id.endButton)
        endButton.setOnClickListener {

            lastKnownLocation = currentMarker?.position
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            setSharingStatus(false)

            Toast.makeText(this@FollowMeActiv,"Sharing Ended", Toast.LENGTH_SHORT).show()



        }


    }

    private fun startLocationUpdates() {

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),100)
            return

        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.lastLocation?.let { location ->
                val current = LatLng(location.latitude, location.longitude)
                currentMarker?.remove()
                currentMarker = googleMap.addMarker(MarkerOptions().position(current).title("Current Location"))
                println(location.latitude.toString() + "" + location.longitude.toString())
                setFirestoreLocation(location.latitude,location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 20f))
            }
        }
    }

    private fun getLocation() {
    //Check Location Permission
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),100)
            return

        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            it?.let { location ->
                val current = LatLng(location.latitude, location.longitude)
                currentMarker = googleMap.addMarker(MarkerOptions().position(current).title("Current Location"))
                setFirestoreLocation(location.latitude,location.longitude)


                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 20f))
            }
        }.addOnFailureListener {
            println("Failed to get location")
        }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
    }
    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates when the activity is destroyed
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)


        println("updates stopped")
    }

    private fun showLastKnownLocation() {
        // Check if lastKnownLocation is not null
        lastKnownLocation?.let { location ->
            // Show the map with the last known location
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("lastLocation", location) // Pass the last known location to the MapActivity
            startActivity(intent)
        }
    }

    private fun setFirestoreLocation(latitude: Double, longitude: Double) {
        val locationData = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude
        )
        val locationRef = db.collection("locationData").document(currentUser?.email.toString())

        locationRef.update(locationData as Map<String, Any>)
               .addOnSuccessListener {
               println("Data Added successfully")
               }
               .addOnFailureListener {
               println("Data Not added succesfully")
               }
    }
    private fun setSharingStatus(isSharing: Boolean) {
        val locationRef = db.collection("locationData").document(currentUser?.email.toString())
        locationRef
            .update("isSharing", isSharing)
            .addOnSuccessListener {
                println("isSharing updated successfully")
            }
            .addOnFailureListener { e ->
                println("Failed to update isSharing: ${e.message}")
            }
    }
}
