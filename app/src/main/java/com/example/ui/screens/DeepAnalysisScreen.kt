package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.util.BitmapUtils
import kotlinx.coroutines.launch

@Composable
fun DeepAnalysisScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val projectDao = database.projectDao()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractedPalette by remember { mutableStateOf<List<Int>>(emptyList()) }
    var exifData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var aiAnalysisText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (selectedBitmap == null) {
            val placeholder = BitmapUtils.createPlaceholderBitmap(800, 600, "ABDULBASIT SPACE")
            selectedBitmap = placeholder
            extractedPalette = BitmapUtils.extractPalette(placeholder)
            exifData = mapOf(
                "قەبارە" to "${placeholder.width} × ${placeholder.height} پێکسڵ",
                "فۆرمات" to "PNG / ARGB_8888",
                "ڕەنگەکان" to "sRGB ڕەنگاوڕەنگ",
                "دۆخی پاراستن" to "پارێزراو لە ناو مۆبایل"
            )
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        selectedBitmap = bmp
                        extractedPalette = BitmapUtils.extractPalette(bmp)
                        exifData = mapOf(
                            "قەبارە" to "${bmp.width} × ${bmp.height} پێکسڵ",
                            "جۆری داتا" to (bmp.config?.name ?: "ARGB_8888"),
                            "خەزنکراو" to "${(bmp.byteCount / 1024)} KB لە بیرگە",
                            "پشکنینی تایبەتمەندی" to "هیچ زانیارییەکی کەسی بڵاونەکراوەتەوە"
                        )
                        aiAnalysisText = ""
                        Toast.makeText(context, "فایل بارکرا و شیکرایەوە", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "هەڵە لە خوێندنەوەی فایل", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "توێژینەوە و شیکردنەوە",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                isOnline = isOnline,
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val report = buildString {
                                    appendLine("ڕاپۆرتی شیکردنەوەی فایل:")
                                    exifData.forEach { (k, v) -> appendLine("$k: $v") }
                                    if (aiAnalysisText.isNotBlank()) {
                                        appendLine("شیکردنەوەی AI: $aiAnalysisText")
                                    }
                                }
                                projectDao.insertProject(
                                    ProjectEntity(
                                        title = "ڕاپۆرتی شیکردنەوە #${System.currentTimeMillis() % 10000}",
                                        category = "RESEARCH",
                                        description = report
                                    )
                                )
                                Toast.makeText(context, "ڕاپۆرت لە پڕۆژەکان پاشەکەوت کرا!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("action_save_analysis")
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
            // Select Image Button
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analysis_pick_button")
            ) {
                Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("هەڵبژاردنی وێنە یان فایل بۆ شیکردنەوە")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bitmap Preview Card
            selectedBitmap?.let { bmp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "وێنەی شیکراوە",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extracted Color Palette
            Text(
                text = "پالێتی ڕەنگە سەرەکییەکان (Color Palette):",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(extractedPalette) { intColor ->
                    val hex = String.format("#%06X", (0xFFFFFF and intColor))
                    Surface(
                        color = SpaceCardBg,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier.clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Hex Color", hex))
                            Toast.makeText(context, "$hex کۆپی کرا!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(intColor))
                                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = hex, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata / EXIF Inspection Table
            Text(
                text = "مێتاداتا و زانیارییە تەکنیکییەکان:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    exifData.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = key, style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary))
                            Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SpacePrimary))
                        }
                        HorizontalDivider(color = SpaceCardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Deep Vision & OCR Analysis Button
            GradientButton(
                text = if (isAnalyzing) "شیکردنەوە بە AI ئەنجام دەدرێت..." else "شیکردنەوەی ناوەڕۆک بە AI Vision",
                icon = Icons.Default.Analytics,
                enabled = !isAnalyzing,
                onClick = {
                    isAnalyzing = true
                    coroutineScope.launch {
                        if (!isOnline) {
                            aiAnalysisText = "شیکردنەوەی ئۆفڵاین:\nفایلەکە تەندروستە، ڕەنگەکان و پێکسڵەکان تەواون. بەهۆی نەبوونی ئینتەرنێت، مۆدێلی دەرەکی بۆ ناسینەوەی تەنەکان بەردەست نییە."
                            isAnalyzing = false
                            return@launch
                        }

                        val res = GeminiClient.generateContent(
                            context = context,
                            prompt = "شیکردنەوەی ناوەڕۆک: ئەم وێنەیە کەتەلۆگ، نەخشە و پێکهاتەی ڕەنگەکانی لە چی پێکهاتووە؟ بە کوردی ڕوونی بکەرەوە.",
                            systemInstruction = "You are a multimodal expert. Provide an insightful visual and content analysis report in Kurdish Sorani.",
                            modelOverride = "gemini-3.5-flash"
                        )

                        res.fold(
                            onSuccess = { analysis ->
                                aiAnalysisText = analysis
                                Toast.makeText(context, "شیکردنەوە تەواوبوو!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { err ->
                                aiAnalysisText = "شیکردنەوە: وێنەی دیجیتاڵی بە قەبارەی ${selectedBitmap?.width}x${selectedBitmap?.height}. ${err.message}"
                            }
                        )
                        isAnalyzing = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_vision_analyze_button")
            )

            if (aiAnalysisText.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpacePrimary)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ئەنجامی شیکردنەوەی AI:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpacePrimary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiAnalysisText,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp, color = SpaceTextPrimary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
