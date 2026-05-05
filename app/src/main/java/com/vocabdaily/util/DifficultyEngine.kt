package com.vocabdaily.util

object DifficultyEngine {
    fun updateLevel(score: Float, currentLevel: String): String {
        return when {
            score > 0.8f -> increaseLevel(currentLevel)
            score < 0.5f -> decreaseLevel(currentLevel)
            else -> currentLevel.trim().uppercase()
        }
    }

    private fun increaseLevel(level: String): String = when (level.trim().uppercase()) {
        "A1" -> "A2"
        "A2" -> "B1"
        "B1" -> "B2"
        "B2" -> "C1"
        "C1" -> "C2"
        "C2" -> "C2"
        else -> level.trim().uppercase()
    }

    private fun decreaseLevel(level: String): String = when (level.trim().uppercase()) {
        "C2" -> "C1"
        "C1" -> "B2"
        "B2" -> "B1"
        "B1" -> "A2"
        "A2" -> "A1"
        "A1" -> "A1"
        else -> level.trim().uppercase()
    }
}

