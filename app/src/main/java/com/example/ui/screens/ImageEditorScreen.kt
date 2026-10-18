package com.example.ui.screens

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
import com.example.ui.components.GradientButton
import com.example.ui.components.SpaceTopBar
import com.example.ui.theme.*
import com.example.util.BitmapUtils
import kotlinx.coroutines.launch

@Composable
fun ImageEditorScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val projectDao = database.projectDao()

    var baseBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val undoStack = remember { mutableStateListOf<Bitmap>() }
    val redoStack = remember { mutableStateListOf<Bitmap>() }

    var selectedTab by remember { mutableStateOf(0) } // 0: فلتەرەکان, 1: ڕێکخستنی ڕەنگ, 2: دەق و نووسین, 3: سووڕاندن

    // Color adjust sliders
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(1f) }
    var saturation by remember { mutableStateOf(1f) }

    // Text Overlay
    var textToAdd by remember { mutableStateOf("") }
    var textFontSize by remember { mutableStateOf(48f) }
    var selectedTextColor by remember { mutableStateOf(android.graphics.Color.WHITE) }

    // Init with placeholder if null
    LaunchedEffect(Unit) {
        if (baseBitmap == null) {
            val placeholder = BitmapUtils.createPlaceholderBitmap(800, 600, "ABDULBASIT SPACE")
            baseBitmap = placeholder
            currentBitmap = placeholder
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        baseBitmap = bmp
                        currentBitmap = bmp
                        undoStack.clear()
                        redoStack.clear()
                        Toast.makeText(context, "وێنە بارکرا", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "نەتوانرا وێنە بکرێتەوە", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun pushHistory(newBmp: Bitmap) {
        currentBitmap?.let { undoStack.add(it) }
        redoStack.clear()
        currentBitmap = newBmp
    }

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "دەستکاریکردنی وێنە (Picsart)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                isOnline = isOnline,
                actions = {
                    IconButton(
                        onClick = {
                            if (undoStack.isNotEmpty()) {
                                val previous = undoStack.removeLast()
                                currentBitmap?.let { redoStack.add(it) }
                                currentBitmap = previous
                            }
                        },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier.testTag("action_undo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "گەڕانەوە بۆ پێشوو",
                            tint = if (undoStack.isNotEmpty()) SpacePrimary else SpaceTextMuted
                        )
                    }

                    IconButton(
                        onClick = {
                            if (redoStack.isNotEmpty()) {
                                val next = redoStack.removeLast()
                                currentBitmap?.let { undoStack.add(it) }
                                currentBitmap = next
                            }
                        },
                        enabled = redoStack.isNotEmpty(),
                        modifier = Modifier.testTag("action_redo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "چوونە پێشەوە",
                            tint = if (redoStack.isNotEmpty()) SpacePrimary else SpaceTextMuted
                        )
                    }

                    IconButton(
                        onClick = {
                            currentBitmap?.let { bmp ->
                                coroutineScope.launch {
                                    val file = BitmapUtils.saveBitmapToFile(context, bmp)
                                    projectDao.insertProject(
                                        ProjectEntity(
                                            title = "دەستکاری وێنە #${System.currentTimeMillis() % 10000}",
                                            category = "IMAGE",
                                            description = "وێنەی دەستکاریکراو بە ئامرازەکانی ستۆدیۆ",
                                            thumbnailUri = file.absolutePath
                                        )
                                    )
                                    Toast.makeText(context, "لە پڕۆژەکان پاشەکەوت کرا!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.testTag("action_save_image")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo Picker & Load Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.testTag("button_pick_image")
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("هێنانەناوەوە لە گەلەری")
                }

                Text(
                    text = "${currentBitmap?.width ?: 0} × ${currentBitmap?.height ?: 0} px",
                    style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Image Preview Canvas Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .border(1.dp, SpaceCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    currentBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "پێشبینینی وێنە",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs for Toolsets
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SpacePrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("فلتەرەکان", style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("ڕەنگ و ڕووناکی", style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("دەق و ستیکەر", style = MaterialTheme.typography.labelSmall) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("سووڕاندن", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tool Content based on Selected Tab
            when (selectedTab) {
                0 -> {
                    // Filter Chips
                    Text(
                        text = "فلتەرێکی هونەری هەڵبژێرە:",
                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(BitmapUtils.FilterType.entries.toTypedArray()) { filter ->
                            Surface(
                                color = SpaceCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                                modifier = Modifier.clickable {
                                    baseBitmap?.let { bmp ->
                                        val filtered = BitmapUtils.applyFilter(bmp, filter)
                                        pushHistory(filtered)
                                    }
                                }
                            ) {
                                Text(
                                    text = filter.kurdishName,
                                    style = MaterialTheme.typography.labelSmall.copy(color = SpacePrimary),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // Color Sliders
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "ڕووناکی: ${brightness.toInt()}",
                            style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary)
                        )
                        Slider(
                            value = brightness,
                            onValueChange = { brightness = it },
                            valueRange = -80f..80f,
                            onValueChangeFinished = {
                                baseBitmap?.let { bmp ->
                                    val adj = BitmapUtils.applyColorAdjustments(bmp, brightness, contrast, saturation)
                                    pushHistory(adj)
                                }
                            }
                        )

                        Text(
                            text = "کۆنتراست: ${String.format("%.1f", contrast)}",
                            style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary)
                        )
                        Slider(
                            value = contrast,
                            onValueChange = { contrast = it },
                            valueRange = 0.5f..2.0f,
                            onValueChangeFinished = {
                                baseBitmap?.let { bmp ->
                                    val adj = BitmapUtils.applyColorAdjustments(bmp, brightness, contrast, saturation)
                                    pushHistory(adj)
                                }
                            }
                        )

                        Text(
                            text = "تێری ڕەنگ (Saturation): ${String.format("%.1f", saturation)}",
                            style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary)
                        )
                        Slider(
                            value = saturation,
                            onValueChange = { saturation = it },
                            valueRange = 0f..2.5f,
                            onValueChangeFinished = {
                                baseBitmap?.let { bmp ->
                                    val adj = BitmapUtils.applyColorAdjustments(bmp, brightness, contrast, saturation)
                                    pushHistory(adj)
                                }
                            }
                        )
                    }
                }

                2 -> {
                    // Add Text Layer
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = textToAdd,
                            onValueChange = { textToAdd = it },
                            label = { Text("دەقی کوردی بۆ وێنەکە") },
                            placeholder = { Text("بۆ نموونە: بیرەوەری ٢٠٢٦") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "قەبارەی فۆنت: ${textFontSize.toInt()} px",
                            style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary)
                        )
                        Slider(
                            value = textFontSize,
                            onValueChange = { textFontSize = it },
                            valueRange = 24f..96f
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (textToAdd.isNotBlank()) {
                                    currentBitmap?.let { bmp ->
                                        val withText = BitmapUtils.addTextOverlay(
                                            src = bmp,
                                            text = textToAdd,
                                            textColor = selectedTextColor,
                                            textSize = textFontSize
                                        )
                                        pushHistory(withText)
                                        textToAdd = ""
                                        Toast.makeText(context, "دەقەکە زیادکرا", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.TextFields, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("زیادکردنی دەق بۆ ناوەڕۆکی وێنە")
                        }
                    }
                }

                3 -> {
                    // Rotation & Flip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                currentBitmap?.let { bmp ->
                                    val rotated = BitmapUtils.rotateBitmap(bmp, -90f)
                                    pushHistory(rotated)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.RotateLeft, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("٩٠° چەپ")
                        }

                        OutlinedButton(
                            onClick = {
                                currentBitmap?.let { bmp ->
                                    val rotated = BitmapUtils.rotateBitmap(bmp, 90f)
                                    pushHistory(rotated)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.RotateRight, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("٩٠° ڕاست")
                        }

                        OutlinedButton(
                            onClick = {
                                currentBitmap?.let { bmp ->
                                    val flipped = BitmapUtils.flipBitmap(bmp, horizontal = true)
                                    pushHistory(flipped)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Flip, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("پێچەوانە")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
