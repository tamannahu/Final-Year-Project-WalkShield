package com.example.signuppract

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore


class SupportActivity : AppCompatActivity() {
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)


        //get data
        db.collection("guides").get().addOnSuccessListener { snapshot ->
            setupGuides(snapshot.documents)

        }


        val backButton = findViewById<Button>(R.id.backButton2)
        backButton.setOnClickListener {
            val Intent = Intent(this, MainActivity::class.java)
            startActivity(Intent)
        }


    }

    private fun setupGuides(documents: List<DocumentSnapshot>) {

        for (doc in documents) {
            val guide = doc.data
            if (guide != null) {
                val titleTextView = TextView(this)
                val title = guide["title"] as? String // Accessing the title field


                titleTextView.text = title
                titleTextView.setTextColor(resources.getColor(R.color.pink))
                titleTextView.textSize = 22f
                titleTextView.setPadding(50, 30, 50, 30)


                val contentTextView = TextView(this)
                val content = guide["content"] as? String // Accessing the content field
                contentTextView.text = content
                contentTextView.setTextColor(resources.getColor(R.color.pink))
                contentTextView.textSize = 14f
                contentTextView.setPadding(50, 10, 50, 30)
                val container = findViewById<LinearLayout>(R.id.guidesContainer)






                container.addView(titleTextView)
                container.addView(contentTextView)

                for (i in 1..5) { // Change 5 to the maximum number of links per document
                    val linkName = guide["linkname$i"] as? String
                    val url = guide["url$i"] as? String

                    if (linkName != null && url != null) {
                        val linkContainer = LinearLayout(this) // Create a container for link & URL
                        linkContainer.orientation = LinearLayout.VERTICAL // Set vertical orientation

                        val linkNameTextView = TextView(this)
                        linkNameTextView.text = linkName
                        linkNameTextView.setTextColor(resources.getColor(R.color.pink)) // Optional: Customize text color
                        linkNameTextView.textSize = 18f
                        linkNameTextView.setPadding(40, 10, 10, 10)

                        val urlTextView = TextView(this)
                        urlTextView.text = url
                        urlTextView.setTextColor(resources.getColor(R.color.blue)) // Optional: Change link text color
                        urlTextView.textSize = 14f
                        urlTextView.setPadding(40, 0, 10, 30) // Adjust padding as needed

                        // Make URL TextView clickable
                        urlTextView.setLinkTextColor(resources.getColor(R.color.blue)) // Optional: Change link click color
                        urlTextView.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        }

                        linkContainer.addView(linkNameTextView)
                        linkContainer.addView(urlTextView)

                        container.addView(linkContainer)
                    }
                }
            }
        }

    }

}



