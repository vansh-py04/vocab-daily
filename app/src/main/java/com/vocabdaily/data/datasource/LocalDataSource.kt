package com.vocabdaily.data.datasource

import com.vocabdaily.domain.model.Word

interface LocalDataSource {
    suspend fun loadWords(): List<Word>
}

