package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.model.ProjectEntity
import com.example.data.remote.GeminiClient
import com.example.ui.components.GradientButton
import com.example.ui.components.SpaceTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AppBuilderScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val projectDao = database.projectDao()

    var appPrompt by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: پێشبینین (Preview), 1: کۆد (Code), 2: فایلەکان (Files), 3: بیلد و APK
    var isGenerating by remember { mutableStateOf(false) }

    var generatedCode by remember {
        mutableStateOf(
            """
            // کاتی دروستکراو بە ABDULBASIT SPACE
            @Composable
            fun MyGeneratedApp() {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "بەخێربێیت بۆ ئەپی نوێ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { /* کردار */ }, modifier = Modifier.fillMaxWidth()) {
                            Text("دوگمەی کارلێککەر")
                        }
                    }
                }
            }
            """.trimIndent()
        )
    }

    var appTitlePreview by remember { mutableStateOf("ئەپی نموونەیی کوردی") }
    var appDescriptionPreview by remember { mutableStateOf("پێشبینینی ڕاستەوخۆی ئەپەکەت لێرە پیشان دەدرێت") }

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "دروستکردنی ئەپ بە AI",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                isOnline = isOnline,
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                projectDao.insertProject(
                                    ProjectEntity(
                                        title = appTitlePreview,
                                        category = "APP",
                                        description = appDescriptionPreview,
                                        payloadJson = generatedCode
                                    )
                                )
                                Toast.makeText(context, "کۆدی ئەپ لە پڕۆژەکان پاشەکەوت کرا!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("action_save_app_code")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = "پاشەکەوت", tint = SpaceAccentEmerald)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header card
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = SpaceAccentEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "دروستکەری ئەندرۆید بە پڕۆمۆپت",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "داواکاری ئەپەکەت بە زمانی کوردی بنووسە؛ AI تەلارسازی، کۆدی Jetpack Compose و پێشبینینی کارلێککەر دروست دەکات.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = appPrompt,
                onValueChange = { appPrompt = it },
                label = { Text("وەسفی ئەپەکەت بە کوردی") },
                placeholder = { Text("بۆ نموونە: ئەپێکی فرۆشتنی پەڕتووک بە کەتەلۆگ، سەبەتەی کڕین و گەڕان...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_builder_prompt_input"),
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(14.dp))

            GradientButton(
                text = if (isGenerating) "AI کۆدەکە دادەڕێژێت..." else "دروستکردنی کۆد و پێشبینین",
                icon = Icons.Default.Code,
                enabled = appPrompt.isNotBlank() && !isGenerating,
                onClick = {
                    isGenerating = true
                    coroutineScope.launch {
                        appTitlePreview = appPrompt.take(24)
                        appDescriptionPreview = "ئەپی دروستکراو بۆ: $appPrompt"

                        if (!isOnline) {
                            generatedCode = """
                                // پڕۆژەی ئۆفڵاین بۆ $appPrompt
                                package com.example.spaceapp
                                
                                import androidx.compose.foundation.layout.*
                                import androidx.compose.material3.*
                                import androidx.compose.runtime.*
                                import androidx.compose.ui.Modifier
                                import androidx.compose.ui.unit.dp
                                
                                @Composable
                                fun MainAppScreen() {
                                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                        Text("$appPrompt", style = MaterialTheme.typography.headlineMedium)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Card(modifier = Modifier.fillMaxWidth()) {
                                            Text("ناوەڕۆکی سەرەکی ئەپ", modifier = Modifier.padding(16.dp))
                                        }
                                    }
                                }
                            """.trimIndent()
                            isGenerating = false
                            return@launch
                        }

                        val systemInst = "You are a senior Android Kotlin & Jetpack Compose engineer. Generate complete, clean, compilable Jetpack Compose Kotlin code according to the user's prompt. Provide UI composables with state, buttons, and Kurdish labels."
                        val res = GeminiClient.generateContent(
                            context = context,
                            prompt = "ئەپێک دروست بکە: $appPrompt",
                            systemInstruction = systemInst,
                            modelOverride = "gemini-3.1-pro-preview"
                        )

                        res.fold(
                            onSuccess = { code ->
                                generatedCode = code
                                Toast.makeText(context, "کۆدی ئەپ دروستکرا!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { err ->
                                generatedCode = "// هەڵە: ${err.message}"
                            }
                        )
                        isGenerating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_builder_generate_button")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Navigation Tabs (Preview, Code, Files, APK)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SpacePrimary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("پێشبینین", style = MaterialTheme.typography.labelSmall) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("کۆد (Kotlin)", style = MaterialTheme.typography.labelSmall) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("فایلەکان", style = MaterialTheme.typography.labelSmall) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("بیلد و APK", style = MaterialTheme.typography.labelSmall) })
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Interactive Sandbox Preview
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = SpaceCardBg)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = appTitlePreview,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SpacePrimary)
                                )
                                StatusBadge(text = "پێشبینینی ڕاستەوخۆ", color = SpaceAccentEmerald)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = appDescriptionPreview,
                                style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Mock Items inside preview
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "بەشی سەرەکی و کارلێککەر",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "ئەم بەشە لەسەر مۆبایلی ئەندرۆید بە تەواوی ئامادەیە بۆ وەرگرتنی داتا و بەستنەوە بە بنکەدراوە.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "کرداری پێشبینین تاقیکرایەوە!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("تاقیکردنەوەی دوگمە")
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Code Viewer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "MainActivity.kt", style = MaterialTheme.typography.labelMedium.copy(color = SpacePrimary))
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("App Code", generatedCode))
                                        Toast.makeText(context, "کۆد کۆپی کرا!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "کۆپیکردن", tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = generatedCode,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }

                2 -> {
                    // Project File Explorer
                    Surface(
                        color = SpaceCardBg,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "پێکهاتەی فۆڵدەر و فایلەکانی پڕۆژە:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(10.dp))
                            val files = listOf(
                                Pair("app/src/main/AndroidManifest.xml", Icons.Default.Description),
                                Pair("app/src/main/java/MainActivity.kt", Icons.Default.Code),
                                Pair("app/src/main/java/ui/screens/HomeScreen.kt", Icons.Default.Code),
                                Pair("app/src/main/java/ui/theme/Theme.kt", Icons.Default.Palette),
                                Pair("app/build.gradle.kts", Icons.Default.Build)
                            )
                            files.forEach { (path, icon) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = SpacePrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = path, style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextPrimary))
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // APK Build Explanation & Guidance
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "دۆخی بیلد و دروستکردنی APK:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpacePrimary))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ئەم پڕۆژەیە لە ژینگەی AI Studio کلاود دەستبەجێ کۆمپایل و پشکنینی بۆ دەکرێت. دەتوانیت لە ڕێگەی مێنیوی سەرەوە APK/AAB دەربکەیت یان کۆدەکە وەک ZIP داگریت بۆ دامەزراندن لەسەر مۆبایلەکەت.",
                                style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary, lineHeight = 20.sp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            StatusBadge(text = "پڕۆژەی ئامادە بۆ بیلد", color = SpaceAccentEmerald)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
