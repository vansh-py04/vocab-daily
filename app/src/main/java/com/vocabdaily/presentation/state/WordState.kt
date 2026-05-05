package com.vocabdaily.presentation.state

import com.vocabdaily.domain.model.Word

data class WordState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val dailyWords: List<Word> = emptyList(),
    val index: Int = 0,
    val isMeaningExpanded: Boolean = false,
) {
    val currentWord: Word?
        get() = dailyWords.getOrNull(index)
}

