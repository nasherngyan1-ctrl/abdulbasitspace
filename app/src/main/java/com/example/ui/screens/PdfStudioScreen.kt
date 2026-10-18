package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.FileProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ProjectEntity
import com.example.ui.components.GradientButton
import com.example.ui.components.SpaceTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.BitmapUtils
import com.example.util.PdfExporter
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun PdfStudioScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val projectDao = database.projectDao()

    var docTitle by remember { mutableStateOf("ڕاپۆرتی کارەکانی ABDULBASIT SPACE") }
    var docContent by remember {
        mutableStateOf(
            """
            ئەم بەڵگەنامەیە پوختەی کارەکان، پرۆژە دیجیتاڵییەکان، دەستکارییەکانی وێنە و ئەنجامی توێژینەوەکان لە خۆ دەگرێت.
            هەموو زانیارییەکان بە شێوەیەکی خۆماڵی لە ناو مۆبایلەکەتدا بە پارێزراوی پاشەکەوت کراون.
            """.trimIndent()
        )
    }
    var selectedCategory by remember { mutableStateOf("ڕاپۆرتی گشتی") }
    var attachImage by remember { mutableStateOf(true) }
    var lastExportedFile by remember { mutableStateOf<File?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "بەشی PDF و کۆکردنەوەی داتا",
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
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = SpaceAccentRose)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ستۆدیۆی دروستکردنی PDF بە کوردی",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "دەق، زانیاری کەسی، وێنە و ئەنجامەکانی AI بگۆڕە بۆ بەڵگەنامەی ڕێکخراوی PDF کە بە شێوەیەکی تەواو لە ڕاستەوە بۆ چەپ (RTL) دیزاین کراوە.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = docTitle,
                onValueChange = { docTitle = it },
                label = { Text("ناونیشانی بەڵگەنامە (Title)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pdf_title_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = docContent,
                onValueChange = { docContent = it },
                label = { Text("ناوەڕۆکی بەڵگەنامە") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pdf_content_input"),
                shape = RoundedCornerShape(12.dp),
                minLines = 4,
                maxLines = 8
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "دانانی وێنەی ڕازاوە لە ناو PDF:",
                    style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextPrimary)
                )
                Switch(
                    checked = attachImage,
                    onCheckedChange = { attachImage = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            GradientButton(
                text = if (isExporting) "بەڵگەنامەکە ئامادە دەکرێت..." else "دروستکردنی فایلی PDF",
                icon = Icons.Default.FileDownload,
                enabled = docTitle.isNotBlank() && !isExporting,
                onClick = {
                    isExporting = true
                    coroutineScope.launch {
                        try {
                            val bmp = if (attachImage) {
                                BitmapUtils.createPlaceholderBitmap(600, 300, docTitle)
                            } else null

                            val file = PdfExporter.generateProjectPdf(
                                context = context,
                                title = docTitle,
                                description = docContent,
                                category = selectedCategory,
                                bitmap = bmp,
                                metadata = mapOf(
                                    "خاوەنی پڕۆژە" to "عەبدولباست",
                                    "سیستەم" to "ABDULBASIT SPACE Mobile",
                                    "کوالیتی لاپەڕە" to "A4 High-Res Vector",
                                    "پشتگیری زمان" to "کوردی سۆرانی (RTL)"
                                )
                            )

                            lastExportedFile = file

                            projectDao.insertProject(
                                ProjectEntity(
                                    title = docTitle,
                                    category = "PDF",
                                    description = "فایلی PDF خەزنکراو: ${file.name}",
                                    thumbnailUri = file.absolutePath
                                )
                            )

                            Toast.makeText(context, "فایلی PDF بە سەرکەوتوویی دروستکرا!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "هەڵە لە دروستکردنی PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        isExporting = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pdf_create_button")
            )

            if (lastExportedFile != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SpaceAccentEmerald)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "فایلی ئامادەکراو:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpaceAccentEmerald)
                            )
                            StatusBadge(text = "${(lastExportedFile?.length() ?: 0) / 1024} KB", color = SpaceAccentEmerald)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lastExportedFile?.name ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "شوێنی هەڵگرتن: ${lastExportedFile?.absolutePath}",
                            style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextMuted)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val file = lastExportedFile ?: return@OutlinedButton
                                    try {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.provider",
                                            file
                                        )
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            type = "application/pdf"
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "هاوبەشکردنی PDF"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "فایلەکە لە ڕێگەی مۆبایلەوە ئامادەیە", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("هاوبەشکردن")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
