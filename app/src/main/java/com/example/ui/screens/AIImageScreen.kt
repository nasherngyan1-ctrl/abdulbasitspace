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
import androidx.compose.ui.graphics.Brush
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
fun AIImageScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val projectDao = database.projectDao()

    var promptText by remember { mutableStateOf("") }
    var enhancedPrompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Realistic 4K (ڕاستەقینە)") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var isGenerating by remember { mutableStateOf(false) }

    val styles = listOf(
        "Realistic 4K (ڕاستەقینە)",
        "Cyberpunk (سایبەرپەنک)",
        "3D Cartoon (ئەنیمەیشن)",
        "Anime Studio (ئەنیمێ)",
        "Watercolor (ڕەنگی ئاو)",
        "Minimalist Logo (لۆگۆ)"
    )

    val ratios = listOf("1:1", "9:16", "16:9", "4:3")

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "دروستکردنی وێنە بە AI",
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
            // Header explanation
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SpacePrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ستۆدیۆی وێنەی زیرەکی دەستکرد",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "داواکارییەکەت بە کوردی بنووسە؛ سیستەم بە شێوەیەکی زیرەک پڕۆمۆپتەکەت دەوڵەمەند دەکات بۆ بەدەستهێنانی بەرزترین کوالێتی.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt Input
            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                label = { Text("وەسفی وێنەکە بە کوردی") },
                placeholder = { Text("بۆ نموونە: قەڵای هەولێر لە داهاتوویەکی سایبەرپەنک بە ڕووناکی نیۆن...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_image_prompt_input"),
                shape = RoundedCornerShape(14.dp),
                minLines = 3,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Style Selection
            Text(
                text = "ستایلی هونەری:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(styles) { style ->
                    val isSelected = selectedStyle == style
                    Surface(
                        color = if (isSelected) SpacePrimary else SpaceCardBg,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier.clickable { selectedStyle = style }
                    ) {
                        Text(
                            text = style,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.Black else SpaceTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Aspect Ratio
            Text(
                text = "ڕەهەندی وێنە (Aspect Ratio):",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ratios.forEach { ratio ->
                    val isSelected = selectedRatio == ratio
                    Surface(
                        color = if (isSelected) SpaceSecondary else SpaceCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRatio = ratio }
                    ) {
                        Text(
                            text = ratio,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.Black else SpaceTextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Generate Button
            GradientButton(
                text = if (isGenerating) "AI وێنەکە ئامادە دەکات..." else "دروستکردن و فراوانکردنی پڕۆمۆپت",
                icon = Icons.Default.AutoFixHigh,
                enabled = promptText.isNotBlank() && !isGenerating,
                onClick = {
                    isGenerating = true
                    coroutineScope.launch {
                        if (!isOnline) {
                            enhancedPrompt = "پڕۆمۆپتی ئۆفڵاین:\n[وێنەی $promptText بە ستایلی $selectedStyle و ڕەهەندی $selectedRatio بە کوالێتی بەرز و وردەکاری تەواو]\nتێبینی: دروستکردنی ڕاستەوخۆ بە سێرڤەری AI پێویستی بە ئینتەرنێتە."
                            isGenerating = false
                            return@launch
                        }

                        val systemInst = "You are an expert AI image prompt engineer. Convert the user's Kurdish request into an ultra-detailed, professional English prompt suitable for Imagen / Gemini-2.5-flash-image with camera lighting, render engine details, and artistic style. Also provide a summary in Kurdish Sorani."
                        val userQuery = "وەسف: $promptText\nستایل: $selectedStyle\nڕەهەند: $selectedRatio"

                        val res = GeminiClient.generateContent(
                            context = context,
                            prompt = userQuery,
                            systemInstruction = systemInst,
                            modelOverride = "gemini-3.5-flash"
                        )

                        res.fold(
                            onSuccess = { generated ->
                                enhancedPrompt = generated
                                projectDao.insertProject(
                                    ProjectEntity(
                                        title = "پڕۆمۆپتی وێنە: ${promptText.take(20)}",
                                        category = "IMAGE",
                                        description = "ستایل: $selectedStyle • ڕەهەند: $selectedRatio\n$generated"
                                    )
                                )
                                Toast.makeText(context, "پڕۆمۆپت بە سەرکەوتوویی دروستکرا!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { err ->
                                enhancedPrompt = "هەڵە: ${err.message}\nئەگەر کلیل نەبەستراوە، تکایە کلیلی API لە ڕێکخستنەکان پڕبکەرەوە."
                            }
                        )
                        isGenerating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_image_generate_button")
            )

            if (enhancedPrompt.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpacePrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ئەنجامی پڕۆمۆپت و نەخشەی وێنە:",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpacePrimary
                                )
                            )
                            StatusBadge(text = selectedRatio, color = SpaceSecondary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = enhancedPrompt,
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
