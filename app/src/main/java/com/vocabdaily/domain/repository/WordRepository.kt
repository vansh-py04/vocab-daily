package com.vocabdaily.domain.repository

import com.vocabdaily.domain.model.Word

interface WordRepository {
    suspend fun getAllWords(): List<Word>
}

