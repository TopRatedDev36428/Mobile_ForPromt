package com.fortinge.prompter.utils

import android.graphics.Color

    fun Int.isColorDark() : Boolean{
        val darkness = 1-(0.299* Color.red(this) + 0.587* Color.green(this) + 0.114* Color.blue(this))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }