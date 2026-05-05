package com.vocabdaily.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vocabdaily.data.local.AssetLocalDataSource
import com.vocabdaily.data.repository.WordRepositoryImpl
import com.vocabdaily.domain.usecase.GetDailyWords
import com.vocabdaily.presentation.state.WordState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WordViewModel(app: Application) : AndroidViewModel(app) {
    private val localDataSource = AssetLocalDataSource(app.applicationContext)
    private val repository = WordRepositoryImpl(localDataSource)
    private val getDailyWords = GetDailyWords(repository)

    private val _state = MutableStateFlow(WordState())
    val state: StateFlow<WordState> = _state

    private var currentLevel: String = "B1"

    init {
        refreshDailyWords()
    }

    fun refreshDailyWords(count: Int = 10) {
        _state.update { it.copy(isLoading = true, errorMessage = null, index = 0, isMeaningExpanded = false) }
        viewModelScope.launch(Dispatchers.Default) {
            runCatching { getDailyWords(currentLevel = currentLevel, count = count) }
                .onSuccess { words ->
                    _state.update { it.copy(isLoading = false, dailyWords = words, index = 0, isMeaningExpanded = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load words") }
                }
        }
    }

    fun nextWord() {
        _state.update { state ->
            if (state.dailyWords.isEmpty()) state
            else state.copy(
                index = (state.index + 1).coerceAtMost(state.dailyWords.lastIndex),
                isMeaningExpanded = false,
            )
        }
    }

    fun prevWord() {
        _state.update { state ->
            if (state.dailyWords.isEmpty()) state
            else state.copy(
                index = (state.index - 1).coerceAtLeast(0),
                isMeaningExpanded = false,
            )
        }
    }

    fun toggleMeaning() {
        _state.update { it.copy(isMeaningExpanded = !it.isMeaningExpanded) }
    }
}

