package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.components.SpaceBottomBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.NetworkMonitor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val networkMonitor = NetworkMonitor(applicationContext)

        setContent {
            var isDarkMode by remember { mutableStateOf(true) }
            val isOnline by networkMonitor.isOnline.collectAsState(initial = networkMonitor.isCurrentlyOnline())

            // Navigation stack
            val backStack = remember { mutableStateListOf("home") }
            val currentScreen = backStack.lastOrNull() ?: "home"

            fun navigateTo(screen: String) {
                if (currentScreen != screen) {
                    backStack.add(screen)
                }
            }

            fun navigateBack() {
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }
            }

            // Hardware back button support
            BackHandler(enabled = backStack.size > 1) {
                navigateBack()
            }

            MyApplicationTheme(darkTheme = isDarkMode) {
                // Mandatory RTL Layout for Sorani Kurdish
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val primaryTabs = setOf("home", "image_editor", "video_editor", "ai_chat", "projects")
                    val showBottomBar = currentScreen in primaryTabs

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            if (showBottomBar) {
                                SpaceBottomBar(
                                    currentRoute = currentScreen,
                                    onNavigate = { route ->
                                        if (currentScreen != route) {
                                            backStack.clear()
                                            backStack.add("home")
                                            if (route != "home") {
                                                backStack.add(route)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                                when (screen) {
                                    "home" -> HomeScreen(
                                        isOnline = isOnline,
                                        onNavigate = { dest -> navigateTo(dest) }
                                    )
                                    "ai_chat" -> AIChatScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "image_editor" -> ImageEditorScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "ai_image" -> AIImageScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "video_editor" -> VideoEditorScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "ai_video" -> AIVideoScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "app_builder" -> AppBuilderScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "web_builder" -> WebBuilderScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "deep_analysis" -> DeepAnalysisScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "pdf_studio" -> PdfStudioScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "api_keys" -> ApiKeyManagerScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "projects" -> ProjectsScreen(
                                        isOnline = isOnline,
                                        onNavigateBack = { navigateBack() }
                                    )
                                    "settings" -> SettingsScreen(
                                        isOnline = isOnline,
                                        isDarkMode = isDarkMode,
                                        onToggleDarkMode = { isDarkMode = it },
                                        onNavigateBack = { navigateBack() }
                                    )
                                    else -> HomeScreen(
                                        isOnline = isOnline,
                                        onNavigate = { dest -> navigateTo(dest) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
