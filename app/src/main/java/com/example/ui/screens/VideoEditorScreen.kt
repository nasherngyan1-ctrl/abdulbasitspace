package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.components.GradientButton
import com.example.ui.components.SpaceTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun VideoEditorScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val projectDao = database.projectDao()

    var projectName by remember { mutableStateOf("پڕۆژەی ڤیدیۆی نوێ") }
    var aspectRatio by remember { mutableStateOf("9:16") }
    var speed by remember { mutableStateOf(1.0f) }
    var trimStart by remember { mutableStateOf(0f) }
    var trimEnd by remember { mutableStateOf(30f) }
    var selectedFilter by remember { mutableStateOf("سینەمایی") }
    var subtitleText by remember { mutableStateOf("") }
    val addedSubtitles = remember { mutableStateListOf("سڵاو لە ABDULBASIT SPACE", "دەستکاریکردنی پرۆفیشناڵی ڤیدیۆ") }
    var selectedAudioTrack by remember { mutableStateOf("مۆسیقای هێمن و مۆدێرن") }
    var isPlaying by remember { mutableStateOf(false) }

    val ratios = listOf("9:16 (تیکتۆک/ڕیڵز)", "16:9 (یوتیوب)", "1:1 (ئینستاگرام)")
    val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)
    val filters = listOf("سینەمایی", "ڕەش و سپی", "گەرم", "سایبەرپەنک", "سارد", "ئاسایی")
    val audioTracks = listOf("مۆسیقای هێمن و مۆدێرن", "ئاوازی سینەمایی حەماسی", "دەنگی سروشت", "بێ دەنگ")

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "دەستکاریکردنی ڤیدیۆ (CapCut)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                isOnline = isOnline,
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val summary = "ڕەهەند: $aspectRatio • خێرایی: ${speed}x • کات: ${trimStart.toInt()}s بۆ ${trimEnd.toInt()}s • فلتەر: $selectedFilter • ژێرنووس: ${addedSubtitles.joinToString(" | ")}"
                                projectDao.insertProject(
                                    ProjectEntity(
                                        title = projectName,
                                        category = "VIDEO",
                                        description = summary,
                                        payloadJson = summary
                                    )
                                )
                                Toast.makeText(context, "پڕۆژەی ڤیدیۆ پاشەکەوت کرا!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("action_save_video")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "پاشەکەوتکردن",
                            tint = SpaceAccentEmerald
                        )
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
            // Video Preview Canvas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, SpaceCardBorder, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "لێدان",
                            tint = SpacePrimary,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable { isPlaying = !isPlaying }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isPlaying) "پەخش دەکرێت... (${speed}x)" else "پێشبینینی Timeline",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White)
                        )
                    }

                    // Watermark / Subtitle badge inside screen
                    if (addedSubtitles.isNotEmpty()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = addedSubtitles.last(),
                                color = SpacePrimary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Ratio tag top right
                    Surface(
                        color = SpaceCardBg.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = aspectRatio.split(" ").first(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-Track Timeline View
            Text(
                text = "هێڵی کات (Multi-Track Timeline):",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = SpaceCardBg,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Track 1: Video
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ڤیدیۆ:", style = MaterialTheme.typography.labelSmall.copy(color = SpacePrimary, fontWeight = FontWeight.Bold), modifier = Modifier.width(48.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SpacePrimary.copy(alpha = 0.4f))
                                .border(1.dp, SpacePrimary, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "کلیپ ١ (${trimStart.toInt()}s - ${trimEnd.toInt()}s)", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Track 2: Subtitle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "دەق:", style = MaterialTheme.typography.labelSmall.copy(color = SpaceAccentPurple, fontWeight = FontWeight.Bold), modifier = Modifier.width(48.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SpaceAccentPurple.copy(alpha = 0.4f))
                                .border(1.dp, SpaceAccentPurple, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "ژێرنووس و سەردێڕی کوردی", style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Track 3: Audio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "دەنگ:", style = MaterialTheme.typography.labelSmall.copy(color = SpaceAccentEmerald, fontWeight = FontWeight.Bold), modifier = Modifier.width(48.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(SpaceAccentEmerald.copy(alpha = 0.4f))
                                .border(1.dp, SpaceAccentEmerald, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedAudioTrack, style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Aspect Ratio Selector
            Text(text = "ڕەهەندی شاشە:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ratios) { r ->
                    val isSelected = aspectRatio == r
                    Surface(
                        color = if (isSelected) SpacePrimary else SpaceCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier.clickable { aspectRatio = r }
                    ) {
                        Text(
                            text = r,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.Black else SpaceTextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Playback Speed
            Text(text = "خێرایی پەخش (Speed):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                speeds.forEach { sp ->
                    val isSelected = speed == sp
                    Surface(
                        color = if (isSelected) SpaceSecondary else SpaceCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { speed = sp }
                    ) {
                        Text(
                            text = "${sp}x",
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

            Spacer(modifier = Modifier.height(14.dp))

            // Trimming Slider
            Text(
                text = "بڕین و کورتکردنەوە: لە ${trimStart.toInt()} چرکە تا ${trimEnd.toInt()} چرکە",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Slider(
                value = trimEnd,
                onValueChange = { trimEnd = it },
                valueRange = 5f..60f
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle Input & Management
            Text(text = "زیادکردنی ژێرنووس (Subtitles):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = subtitleText,
                    onValueChange = { subtitleText = it },
                    placeholder = { Text("دەقی نوێ بنووسە...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (subtitleText.isNotBlank()) {
                            addedSubtitles.add(subtitleText.trim())
                            subtitleText = ""
                        }
                    }
                ) {
                    Text("زیادکردن")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Audio track selection
            Text(text = "مۆسیقای پاشبنەما (Audio Track):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(audioTracks) { track ->
                    val isSelected = selectedAudioTrack == track
                    Surface(
                        color = if (isSelected) SpaceAccentEmerald else SpaceCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier.clickable { selectedAudioTrack = track }
                    ) {
                        Text(
                            text = track,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.Black else SpaceTextPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
