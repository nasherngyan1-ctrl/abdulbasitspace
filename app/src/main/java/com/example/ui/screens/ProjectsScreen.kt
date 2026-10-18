package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.SpaceTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val projectDao = database.projectDao()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val allProjects by projectDao.getAllProjects().collectAsState(initial = emptyList())

    val filteredProjects = allProjects.filter { proj ->
        val matchesCategory = selectedCategory == "ALL" || proj.category == selectedCategory
        val matchesQuery = searchQuery.isBlank() || proj.title.contains(searchQuery, ignoreCase = true) || proj.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    val categories = listOf(
        Pair("ALL", "هەمووی"),
        Pair("IMAGE", "وێنەکان"),
        Pair("VIDEO", "ڤیدیۆکان"),
        Pair("APP", "ئەپەکان"),
        Pair("WEB", "ماڵپەڕەکان"),
        Pair("PDF", "بەڵگەنامەی PDF"),
        Pair("RESEARCH", "توێژینەوە")
    )

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "پڕۆژەکانم • بەڕێوەبردنی فایل",
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("گەڕان لە نێوان پڕۆژەکاندا...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("projects_search_field"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { (catKey, catLabel) ->
                    val isSelected = selectedCategory == catKey
                    Surface(
                        color = if (isSelected) SpacePrimary else SpaceCardBg,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier.clickable { selectedCategory = catKey }
                    ) {
                        Text(
                            text = catLabel,
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

            // List of Projects
            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = SpaceTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "هیچ پڕۆژەیەک نەدۆزرایەوە",
                            style = MaterialTheme.typography.bodyMedium.copy(color = SpaceTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "دەتوانیت لە بەشەکانی وێنە، ڤیدیۆ یان کۆد پڕۆژەی نوێ پاشەکەوت بکەیت.",
                            style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextMuted)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(filteredProjects) { proj ->
                        ProjectItemCard(
                            project = proj,
                            onDelete = {
                                coroutineScope.launch {
                                    projectDao.deleteProject(proj)
                                    Toast.makeText(context, "پڕۆژەکە سڕایەوە", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopy = {
                                val text = "${proj.title}\n${proj.description}\n${proj.payloadJson}"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Project Details", text))
                                Toast.makeText(context, "زانیارییەکان کۆپی کران!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectItemCard(
    project: ProjectEntity,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(project.timestamp))

    val (badgeColor, categoryName) = when (project.category) {
        "IMAGE" -> Pair(Color(0xFFEC4899), "وێنە")
        "VIDEO" -> Pair(Color(0xFFF97316), "ڤیدیۆ")
        "APP" -> Pair(SpaceAccentEmerald, "ئەپ")
        "WEB" -> Pair(SpacePrimary, "ماڵپەڕ")
        "PDF" -> Pair(SpaceAccentRose, "PDF")
        else -> Pair(SpaceSecondary, "توێژینەوە")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SpaceTextPrimary),
                    maxLines = 1
                )
                StatusBadge(text = categoryName, color = badgeColor)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextSecondary, lineHeight = 18.sp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextMuted, fontSize = 10.sp)
                )

                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "کۆپیکردن", tint = SpacePrimary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "سڕینەوە", tint = SpaceAccentRose, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
