package com.example.listingapp.util

import java.util.Locale

class Helper {
    fun palindrome(str: String): Boolean {
        // Remove all non-alphanumeric characters and convert to lowercase
        val cleanStr = str.replace("[^a-zA-Z0-9]".toRegex(), "").lowercase(Locale.getDefault())

        // Check if the cleaned string is a palindrome
        var left = 0
        var right = cleanStr.length - 1
        while (left < right) {
            if (cleanStr[left] != cleanStr[right]) {
                return false
            }
            left++
            right--
        }
        return true
    }
}