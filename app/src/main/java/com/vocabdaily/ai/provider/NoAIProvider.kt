package com.vocabdaily.ai.provider

class NoAIProvider : AIProvider {
    override fun generateExample(word: String): String = ""
    override fun simplifyMeaning(word: String): String = ""
}

