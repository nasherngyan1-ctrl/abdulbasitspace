package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.remote.GeminiClient
import com.example.ui.components.GradientButton
import com.example.ui.components.SpaceTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ApiKeyManagerScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var geminiKey by remember { mutableStateOf(GeminiClient.getApiKey(context)) }
    var selectedModel by remember { mutableStateOf(GeminiClient.getSelectedModel(context)) }
    var testResult by remember { mutableStateOf<Pair<Boolean, Long>?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    val models = listOf(
        Pair("gemini-3.5-flash", "Gemini 3.5 Flash (خێرا، گفتوگۆ و زانیاری گشتی)"),
        Pair("gemini-3.1-pro-preview", "Gemini 3.1 Pro (کۆدنووسین، شیکاری قووڵ و ئەپ)"),
        Pair("gemini-2.5-flash-image", "Gemini 2.5 Flash Image (دروستکردنی وێنە)"),
        Pair("veo-3.1-fast-generate-preview", "Veo 3.1 Fast (دروستکردنی ڤیدیۆ)")
    )

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "بەڕێوەبردنی کلیلەکانی AI",
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
                        Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = SpaceAccentAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بەڕێوەبردنی کلیلەکان و مۆدێلەکان",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "دەتوانیت کلیلی Gemini یان هەر کلیلێکی تر لێرە دابنێیت. هەموو کلیلەکان بە شێوەیەکی پارێزراو تەنها لەسەر ئەم مۆبایلە هەڵدەگیرێن و لە دەرەوە بڵاونابنەوە.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Gemini API Key Input
            Text(text = "کلیلی Gemini API Key:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = geminiKey,
                onValueChange = {
                    geminiKey = it
                    GeminiClient.saveApiKey(context, it)
                },
                placeholder = { Text("AIzaSy...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_key_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = {
                    if (geminiKey.isNotBlank()) {
                        IconButton(onClick = {
                            geminiKey = ""
                            GeminiClient.saveApiKey(context, "")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "سڕینەوە")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Model Selection
            Text(text = "مۆدێلی بنەڕەتی (Default Model):", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                models.forEach { (modelId, desc) ->
                    val isSelected = selectedModel == modelId
                    Surface(
                        color = if (isSelected) SpaceCardBg else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) SpacePrimary else SpaceCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedModel = modelId
                                GeminiClient.setSelectedModel(context, modelId)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedModel = modelId
                                    GeminiClient.setSelectedModel(context, modelId)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = modelId,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SpacePrimary else SpaceTextPrimary
                                    )
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Test Connection Button
            GradientButton(
                text = if (isTesting) "پشکنینی پەیوەندی..." else "تاقیکردنەوەی پەیوەندی بە مۆدێل",
                icon = Icons.Default.Speed,
                enabled = !isTesting,
                onClick = {
                    isTesting = true
                    coroutineScope.launch {
                        testResult = GeminiClient.testConnection(context, selectedModel)
                        isTesting = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_test_button")
            )

            testResult?.let { (success, latency) ->
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = if (success) SpaceAccentEmerald.copy(alpha = 0.15f) else SpaceAccentRose.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (success) SpaceAccentEmerald else SpaceAccentRose
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (success) "پەیوەندی سەرکەوتوو بوو! (خێرایی: ${latency}ms)" else "پەیوەندی سەرکەوتوو نەبوو! کلیل یان ئینتەرنێت بپشکنە.",
                            color = if (success) SpaceAccentEmerald else SpaceAccentRose,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Automatic Fallback Note
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "یاسای گۆڕینی ئۆتۆماتیکی (Auto Fallback):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SpacePrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "کاتێک ئینتەرنێت نییە یان سێرڤەر وەڵام ناداتەوە، سیستەمی ABDULBASIT SPACE دەستبەجێ دۆخی ناوخۆیی (Offline Fallback) چالاک دەکات بەبێ ئەوەی کارەکانت بوەستن.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
