package com.example.signuppract

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.Toast

class SOSButton(context: Context, attrs: AttributeSet?) : androidx.appcompat.widget.AppCompatButton(context, attrs) {
    override fun performClick(): Boolean {
        super.performClick()
        println("Hold Down Longer")
        //Tells the user to hold down on button to initiate call


        return true
    }


}