package com.vocabdaily.ai.provider

interface AIProvider {
    fun generateExample(word: String): String
    fun simplifyMeaning(word: String): String
}

