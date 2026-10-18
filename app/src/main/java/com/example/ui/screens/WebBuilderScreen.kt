package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.AppDatabase
import com.example.data.model.ProjectEntity
import com.example.data.remote.GeminiClient
import com.example.ui.components.GradientButton
import com.example.ui.components.SpaceTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WebBuilderScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val projectDao = database.projectDao()

    var websitePrompt by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: پێشبینینی ڕاستەوخۆ, 1: کۆدی وێب
    var isMobileView by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }

    var htmlCode by remember {
        mutableStateOf(
            """
            <!DOCTYPE html>
            <html lang="ckb" dir="rtl">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>ABDULBASIT SPACE</title>
                <style>
                    body {
                        font-family: system-ui, -apple-system, sans-serif;
                        background: #0f172a;
                        color: #f8fafc;
                        margin: 0;
                        padding: 24px;
                        text-align: center;
                    }
                    .hero {
                        background: linear-gradient(135deg, #1e293b, #0f172a);
                        border: 1px solid #334155;
                        border-radius: 16px;
                        padding: 32px 16px;
                    }
                    h1 { color: #38bdf8; margin-bottom: 8px; font-size: 24px; }
                    p { color: #94a3b8; line-height: 1.6; font-size: 14px; }
                    .btn {
                        display: inline-block;
                        background: #38bdf8;
                        color: #000;
                        font-weight: bold;
                        padding: 10px 24px;
                        border-radius: 8px;
                        text-decoration: none;
                        margin-top: 16px;
                    }
                </style>
            </head>
            <body>
                <div class="hero">
                    <h1>وێبسایتی فەرمی ABDULBASIT SPACE</h1>
                    <p>ئەم وێبسایتە بە شێوەیەکی ئۆتۆماتیکی لە ڕێگەی زیرەکی دەستکردەوە دروستکراوە بە تەواوی پشتگیری لە زمانی کوردی و شاشەی مۆبایل دەکات.</p>
                    <a href="#" class="btn" onclick="alert('سڵاو عەبدولباست!')">تاقیکردنەوەی کردار</a>
                </div>
            </body>
            </html>
            """.trimIndent()
        )
    }

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "دروستکردنی وێبسایت بە AI",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                isOnline = isOnline,
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                projectDao.insertProject(
                                    ProjectEntity(
                                        title = if (websitePrompt.isNotBlank()) websitePrompt.take(24) else "پڕۆژەی وێبسایت",
                                        category = "WEB",
                                        description = "وێبسایتی HTML/CSS دروستکراو بە زمانی کوردی",
                                        payloadJson = htmlCode
                                    )
                                )
                                Toast.makeText(context, "وێبسایت لە پڕۆژەکان پاشەکەوت کرا!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("action_save_website")
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
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = SpaceSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "دروستکەری ماڵپەڕ لە ڕێگەی پڕۆمۆپت",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "داواکاری ماڵپەڕەکەت بە زمانی کوردی بنووسە تا کۆدی تەواوی HTML، CSS و JavaScript دروست بکات لەگەڵ بینینی ڕاستەوخۆ.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = websitePrompt,
                onValueChange = { websitePrompt = it },
                label = { Text("وەسفی ماڵپەڕەکەت بە کوردی") },
                placeholder = { Text("بۆ نموونە: ماڵپەڕێکی مۆدێرن بۆ پۆرتفۆلیۆ بە مێنیو، بەشی خزمەتگوزاری و فۆرمی پەیوەندی...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("web_builder_prompt_input"),
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(14.dp))

            GradientButton(
                text = if (isGenerating) "AI ماڵپەڕەکە دادەڕێژێت..." else "دروستکردن و نوێکردنەوەی ماڵپەڕ",
                icon = Icons.Default.Web,
                enabled = websitePrompt.isNotBlank() && !isGenerating,
                onClick = {
                    isGenerating = true
                    coroutineScope.launch {
                        if (!isOnline) {
                            htmlCode = """
                                <!DOCTYPE html>
                                <html lang="ckb" dir="rtl">
                                <head><meta charset="UTF-8"><title>$websitePrompt</title>
                                <style>body{background:#0b0f19;color:#fff;padding:20px;text-align:center;font-family:sans-serif;}h1{color:#38bdf8;}</style>
                                </head><body><h1>$websitePrompt</h1><p>ماڵپەڕی ئۆفڵاینی دروستکراو لە ABDULBASIT SPACE.</p></body></html>
                            """.trimIndent()
                            isGenerating = false
                            return@launch
                        }

                        val systemInst = "You are a professional web developer. Output a complete, single-file HTML document (containing modern CSS inside <style> and interactive JavaScript inside <script>) with dir='rtl' and Kurdish Sorani text according to the user's prompt. Do NOT wrap in markdown quotes if possible, output pure HTML code."
                        val res = GeminiClient.generateContent(
                            context = context,
                            prompt = "ماڵپەڕێک دروست بکە: $websitePrompt",
                            systemInstruction = systemInst,
                            modelOverride = "gemini-3.1-pro-preview"
                        )

                        res.fold(
                            onSuccess = { code ->
                                val clean = code.replace("```html", "").replace("```", "").trim()
                                htmlCode = clean
                                Toast.makeText(context, "ماڵپەڕ نوێکرایەوە!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { err ->
                                Toast.makeText(context, "هەڵە: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                        isGenerating = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("web_builder_generate_button")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SpacePrimary
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("پێشبینینی ڕاستەوخۆ (WebView)", style = MaterialTheme.typography.labelSmall) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("کۆدی سەرچاوە (HTML/CSS)", style = MaterialTheme.typography.labelSmall) })
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedTab) {
                0 -> {
                    // Responsive Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "قەبارەی پیشاندان:", style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary))
                        Row {
                            FilterChip(
                                selected = isMobileView,
                                onClick = { isMobileView = true },
                                label = { Text("مۆبایل") },
                                leadingIcon = { Icon(Icons.Default.PhoneAndroid, null, modifier = Modifier.size(16.dp)) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = !isMobileView,
                                onClick = { isMobileView = false },
                                label = { Text("تەواوی شاشە") },
                                leadingIcon = { Icon(Icons.Default.Laptop, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real In-App WebView
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(380.dp)
                            .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    webViewClient = WebViewClient()
                                    loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                                }
                            },
                            update = { webView ->
                                webView.loadDataWithBaseURL(null, htmlCode, "text/html", "UTF-8", null)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("web_preview_view")
                        )
                    }
                }

                1 -> {
                    // Code viewer
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
                                Text(text = "index.html", style = MaterialTheme.typography.labelMedium.copy(color = SpacePrimary))
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Website Code", htmlCode))
                                        Toast.makeText(context, "کۆدی ماڵپەڕ کۆپی کرا!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "کۆپی", tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = htmlCode,
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
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
