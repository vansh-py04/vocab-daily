package com.vocabdaily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocabdaily.presentation.ui.screens.HomeScreen
import com.vocabdaily.presentation.viewmodel.WordViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot(modifier: Modifier = Modifier) {
    val viewModel: WordViewModel = viewModel()
    MaterialTheme {
        Surface(modifier = modifier) {
            HomeScreen(viewModel = viewModel)
        }
    }
}

