package com.example.ui.screens

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
fun AIVideoScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val projectDao = database.projectDao()

    var topicText by remember { mutableStateOf("") }
    var videoType by remember { mutableStateOf("ڕیکلامی کورت (Short Promo)") }
    var durationText by remember { mutableStateOf("٣٠ چرکە") }
    var generatedPlan by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val videoTypes = listOf("ڕیکلامی کورت (Short Promo)", "ڤیدیۆی فێرکاری (Tutorial)", "سینەمایی حیکایەت (Cinematic Story)", "تیکتۆک و ڕیڵز (Shorts/Reels)")
    val durations = listOf("١٥ چرکە", "٣٠ چرکە", "٦٠ چرکە", "٢ خولەک")

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "دروستکردنی ڤیدیۆ بە AI (Veo)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                isOnline = isOnline
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
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MovieCreation, contentDescription = null, tint = SpaceAccentPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "داڕشتنی سناریۆ و دیمەنی ڤیدیۆ بە AI",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "بیرۆکەی ڤیدیۆکەت بنووسە تا سناریۆی تەواو، کاتی هەر دیمەنێک، جوڵەی کامێرا و پڕۆمۆپتی ئینگلیزی تایبەت بە مۆدێلی Veo دروست بکات.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = topicText,
                onValueChange = { topicText = it },
                label = { Text("بیرۆکە یان بابەتی ڤیدیۆ") },
                placeholder = { Text("بۆ نموونە: ڤیدیۆیەکی خێرا بۆ ناساندنی ئەپی ABDULBASIT SPACE لە تیکتۆک...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_video_prompt_input"),
                shape = RoundedCornerShape(14.dp),
                minLines = 3,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "جۆری ڤیدیۆ:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(videoTypes) { type ->
                    val isSelected = videoType == type
                    Surface(
                        color = if (isSelected) SpaceAccentPurple else SpaceCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier.clickable { videoType = type }
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else SpaceTextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = "ماوەی پێشبینیکراو:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                durations.forEach { dur ->
                    val isSelected = durationText == dur
                    Surface(
                        color = if (isSelected) SpacePrimary else SpaceCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { durationText = dur }
                    ) {
                        Text(
                            text = dur,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.Black else SpaceTextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            GradientButton(
                text = if (isGenerating) "AI سناریۆکە ئامادە دەکات..." else "دروستکردنی سناریۆ و پلانی دیمەنەکان",
                icon = Icons.Default.AutoFixHigh,
                enabled = topicText.isNotBlank() && !isGenerating,
                onClick = {
                    isGenerating = true
                    coroutineScope.launch {
                        if (!isOnline) {
                            generatedPlan = """
                                پلانی ئۆفڵاین بۆ $topicText ($durationText):
                                
                                دیمەنی ١ (٠-٥ چرکە):
                                • گرتەی سەرنجڕاکێش و سەردێڕی دەستپێک
                                • کامێرا: گەورەکردنی هێواش (Slow Zoom-In)
                                • دەنگ: ئاوازی خێرای مۆدێرن
                                
                                دیمەنی ٢ (٥-٢٠ چرکە):
                                • نمایشکردنی تایبەتمەندییە سەرەکییەکان
                                • دەق لەسەر شاشە: خێرا، ئاسان، بە زمانی کوردی
                                
                                دیمەنی ٣ (٢٠-٣٠ چرکە):
                                • بانگەواز بۆ کردار (Call to action): داگرتنی ABDULBASIT SPACE
                            """.trimIndent()
                            isGenerating = false
                            return@launch
                        }

                        val systemInst = "You are a professional video director and AI video prompt engineer. Write a complete video storyboard script in Kurdish Sorani. For each scene, specify: Scene number, Shot duration, Visual description, Camera movement (drone, pan, tilt), Kurdish narration voiceover, and the exact English prompt for Veo video generation."
                        val query = "بابەت: $topicText\nجۆر: $videoType\nماوە: $durationText"

                        val res = GeminiClient.generateContent(
                            context = context,
                            prompt = query,
                            systemInstruction = systemInst,
                            modelOverride = "gemini-3.5-flash"
                        )

                        res.fold(
                            onSuccess = { plan ->
                                generatedPlan = plan
                                projectDao.insertProject(
                                    ProjectEntity(
                                        title = "سناریۆی ڤیدیۆ: ${topicText.take(20)}",
                                        category = "VIDEO",
                                        description = "جۆر: $videoType • ماوە: $durationText\n$plan"
                                    )
                                )
                                Toast.makeText(context, "سناریۆ لە پڕۆژەکان پاشەکەوت کرا!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { err ->
                                generatedPlan = "هەڵە: ${err.message}"
                            }
                        )
                        isGenerating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_video_generate_button")
            )

            if (generatedPlan.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpaceAccentPurple)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "سناریۆ و پلانی دیمەنەکانی ڤیدیۆ:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpaceAccentPurple
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = generatedPlan,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SpaceTextPrimary,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
