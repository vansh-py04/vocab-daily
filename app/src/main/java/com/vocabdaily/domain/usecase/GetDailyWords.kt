package com.vocabdaily.domain.usecase

import com.vocabdaily.domain.model.Word
import com.vocabdaily.domain.repository.WordRepository
import kotlin.random.Random

class GetDailyWords(
    private val repository: WordRepository,
) {
    suspend operator fun invoke(
        currentLevel: String,
        count: Int,
        seed: Int? = null,
    ): List<Word> {
        val allWords = repository.getAllWords()
        if (allWords.isEmpty() || count <= 0) return emptyList()

        val random = seed?.let { Random(it) } ?: Random.Default

        val current = allWords.filter { it.level.equals(currentLevel, ignoreCase = true) }
        val easier = easierLevels(currentLevel).flatMap { level ->
            allWords.filter { it.level.equals(level, ignoreCase = true) }
        }
        val harder = harderLevels(currentLevel).flatMap { level ->
            allWords.filter { it.level.equals(level, ignoreCase = true) }
        }

        val currentCount = (count * 0.7f).toInt().coerceAtLeast(0)
        val easierCount = (count * 0.2f).toInt().coerceAtLeast(0)
        val harderCount = (count - currentCount - easierCount).coerceAtLeast(0)

        val picked = mutableListOf<Word>()
        picked += current.shuffled(random).take(currentCount)
        picked += easier.shuffled(random).take(easierCount)
        picked += harder.shuffled(random).take(harderCount)

        if (picked.size < count) {
            val remaining = allWords
                .asSequence()
                .filterNot { w -> picked.any { it.text.equals(w.text, ignoreCase = true) } }
                .toList()
            picked += remaining.shuffled(random).take(count - picked.size)
        }

        return picked.shuffled(random)
    }

    private fun easierLevels(level: String): List<String> = when (normalize(level)) {
        "A1" -> emptyList()
        "A2" -> listOf("A1")
        "B1" -> listOf("A2", "A1")
        "B2" -> listOf("B1", "A2")
        "C1" -> listOf("B2", "B1")
        "C2" -> listOf("C1", "B2")
        else -> emptyList()
    }

    private fun harderLevels(level: String): List<String> = when (normalize(level)) {
        "A1" -> listOf("A2", "B1")
        "A2" -> listOf("B1", "B2")
        "B1" -> listOf("B2", "C1")
        "B2" -> listOf("C1", "C2")
        "C1" -> listOf("C2")
        "C2" -> emptyList()
        else -> emptyList()
    }

    private fun normalize(level: String): String = level.trim().uppercase()
}

