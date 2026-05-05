package com.vocabdaily.data.repository

import com.vocabdaily.data.datasource.LocalDataSource
import com.vocabdaily.domain.model.Word
import com.vocabdaily.domain.repository.WordRepository

class WordRepositoryImpl(
    private val localDataSource: LocalDataSource,
) : WordRepository {
    override suspend fun getAllWords(): List<Word> = localDataSource.loadWords()
}

