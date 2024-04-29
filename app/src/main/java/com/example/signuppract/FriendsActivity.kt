package com.example.signuppract

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class FriendsActivity : AppCompatActivity() {
    var db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<User>
    private lateinit var myAdapter: MyAdapter2
    private val currentUser = FirebaseAuth.getInstance().currentUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)
        auth = FirebaseAuth.getInstance()

        //initialise recyclerview, and adapter
        recyclerView = findViewById(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        userArrayList = arrayListOf()
        myAdapter = MyAdapter2(userArrayList)

        recyclerView.adapter = myAdapter
        EventChangeListener()

        val seeRequestsButton = findViewById<Button>(R.id.seeRequestsButton)
        seeRequestsButton.setOnClickListener{
            val Intent = Intent(this,seeRequests::class.java)
            startActivity(Intent)
        }



    }

    fun deleteFriend(view: View){

        val position = view.tag as Int
        println("DeleteBtn clicked")
        if (position >= 0 && position < userArrayList.size){
            val user = userArrayList[position]
            //userArrayList.removeAt(position)


            val userIDRef = db.collection("users")
            userIDRef.whereEqualTo("email", user.email).get()
                .addOnSuccessListener{ documents ->
                    for (document in documents) {
                        // Get the ID of the document where the field is found
                        val userIDName = document.id
                        // Now that userIDName is retrieved, construct requesterRef
                        db.collection("requests").document(userIDName)
                            .collection("allFriends").document(currentUser?.email.toString()).delete()

                        db.collection("requests").document(currentUser?.uid.toString())
                            .collection("allFriends").document(user.email.toString()).delete()
                        userArrayList.removeAt(position)

                        myAdapter.setUserList(userArrayList)
                        Toast.makeText(this, "Friend Deleted", Toast.LENGTH_SHORT).show()
                    }

                    }

        }

    }

    private fun EventChangeListener() {
        //Fetches data from Firestore.
        //Listens for changes in the Firestore collection friendRequests under the current user's document.
        //Iterates through the documents, extracts email addresses, and adds them to the userArrayList.
        //Notifies the adapter that the data set has changed.
        val currentUserUid = currentUser?.uid

        //db = FirebaseFirestore.getInstance()
        val currentUserRequestsRef = currentUserUid?.let { db.collection("requests").document(it) }
        currentUserRequestsRef?.collection("allFriends")
            ?.addSnapshotListener { allFriendsSnapshot, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }


                userArrayList.clear()
                for (allFriendsDocument in allFriendsSnapshot?.documents ?: emptyList()) {

                    // Here, friendRequestDocument.id will give you the email address
                    val email = allFriendsDocument.id

                    if (email.contains("@")) {
                        // If the email contains "@", add it to the userArrayList
                        val user = User(email)
                        userArrayList.add(user)
                    }
                }

                myAdapter.notifyDataSetChanged()
            }
    }

    fun showBottomSheet(view: View) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheetfriends,null)
        val btnEnter = view.findViewById<Button>(R.id.sendRequest)
        val inputEmail = view.findViewById<EditText>(R.id.emailInput)

        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()


        btnEnter.setOnClickListener {
            val inputText = inputEmail.text.toString()
            db.collection("users")
                .whereEqualTo("email", inputText)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Email found in users collection
                        println("Email found in users collection: $inputText")
                        sendFriendRequest(inputText, auth.currentUser)
                        println("request to $inputText" +" sent")
                        Toast.makeText(this@FriendsActivity,"Request Sent", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()

                        // Send request or perform any other action here
                    } else {
                        // Email not found in users collection
                        println("Email not found in users collection: $inputText")
                        Toast.makeText(this@FriendsActivity,"User not found", Toast.LENGTH_SHORT).show()


                    }
                }
                .addOnFailureListener { e ->
                    // Failed to query Firestore
                    println("Error querying Firestore: $e")
                }


    }


}

    private fun sendFriendRequest(inputText: String, user: FirebaseUser?) {
        val currentUser = user?.uid

        val usersCollection = db.collection("users")


        //Code that adds request to ownRequest of current user
        val ownRequestsRef = db.collection("requests").document(currentUser.toString())
            .collection("ownRequests").document(inputText)

        val requestData = hashMapOf(
            "status" to "requested"
        )
        ownRequestsRef.set(requestData)
            .addOnSuccessListener {
                println("requested added to ownRequests of user "+ user?.email)
            }
            .addOnFailureListener {
                println("help")
            }

        //Code that adds a request to friendRequests of the user its sent to

        db.collection("users").whereEqualTo("email", inputText).get()
            .addOnSuccessListener {  querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Retrieve the first document (assuming email is unique)
                    val docSnapshot = querySnapshot.documents[0]
                    val docId = docSnapshot.id
                    val friendRequestsRef = db.collection("requests").document(docId).
                        collection("friendRequests").document(user?.email.toString())

                    val friendRequestData = hashMapOf(
                        "status" to "pending"
                    )

                    friendRequestsRef.set(friendRequestData).addOnSuccessListener {
                        println("request added to friend request of "+ inputText + docId)
                    }
                        .addOnFailureListener {
                            println("help aagen")
                        }




                    println("UID of the document holding the toUser email $inputText: $docId")
                } else {
                    println("No document found with the email $inputText")
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception") }









    }
}