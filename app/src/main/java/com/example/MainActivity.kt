package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ads.UnityAdsManager
import com.example.ui.screens.ExamKitScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PhotoStudioScreen
import com.example.ui.screens.SavedHubScreen
import com.example.ui.screens.SignatureStudioScreen
import com.example.ui.screens.ThumbImpressionScreen
import com.example.ui.theme.ImageCompressCloudTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Unity Ads SDK with Game ID: 800418075
        UnityAdsManager.init(this, testMode = true)

        setContent {
            ImageCompressCloudTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
            AppScreen.PHOTO_STUDIO -> PhotoStudioScreen(viewModel = viewModel)
            AppScreen.SIGNATURE_STUDIO -> SignatureStudioScreen(viewModel = viewModel)
            AppScreen.THUMB_STUDIO -> ThumbImpressionScreen(viewModel = viewModel)
            AppScreen.EXAM_KIT -> ExamKitScreen(viewModel = viewModel)
            AppScreen.SAVED_HUB -> SavedHubScreen(viewModel = viewModel)
        }
    }
}
