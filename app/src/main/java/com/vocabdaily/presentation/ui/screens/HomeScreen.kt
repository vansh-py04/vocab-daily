package com.vocabdaily.presentation.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.vocabdaily.presentation.ui.components.WordCard
import com.vocabdaily.presentation.viewmodel.WordViewModel
import kotlin.math.abs

@Composable
fun HomeScreen(
    viewModel: WordViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.errorMessage != null -> Text(
                text = state.errorMessage ?: "Unknown error",
                color = MaterialTheme.colorScheme.error,
            )
            state.currentWord == null -> Text(text = "No words found.")
            else -> {
                val word = state.currentWord!!
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(state.index) {
                            detectHorizontalDragGestures(
                                onDragEnd = { },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    if (abs(dragAmount) > 18f) {
                                        if (dragAmount < 0) viewModel.nextWord() else viewModel.prevWord()
                                    }
                                },
                            )
                        },
                ) {
                    AnimatedContent(
                        targetState = word,
                        transitionSpec = {
                            val duration = 220
                            (slideInHorizontally(animationSpec = tween(duration)) { it / 4 } + fadeIn(animationSpec = tween(duration)))
                                .togetherWith(slideOutHorizontally(animationSpec = tween(duration)) { -it / 4 } + fadeOut(animationSpec = tween(duration)))
                        },
                        label = "wordTransition",
                    ) { target ->
                        WordCard(
                            word = target,
                            isExpanded = state.isMeaningExpanded,
                            onToggle = viewModel::toggleMeaning,
                        )
                    }
                }
            }
        }
    }
}

