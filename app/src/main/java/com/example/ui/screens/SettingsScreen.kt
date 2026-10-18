package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.example.ui.components.SpaceTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    isOnline: Boolean,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var cacheSizeKb by remember { mutableStateOf(2450L) }

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "ڕێکخستنەکان • ABDULBASIT SPACE",
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
            // Theme settings card
            Text(text = "ڕووکار و شاشە:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = SpacePrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "دۆخی تاریک (Dark Mode)", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onToggleDarkMode
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SpaceCardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = SpaceSecondary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "زمانی سەرەکی ئەپ", style = MaterialTheme.typography.bodyMedium)
                        }
                        StatusBadge(text = "کوردیی سۆرانی (RTL)", color = SpacePrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Storage & Cache
            Text(text = "بەڕێوەبردنی بیرگە و کاش:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
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
                        Column {
                            Text(text = "قەبارەی کاش (Cache)", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "$cacheSizeKb کیلۆبایت خەزنکراوە", style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary))
                        }
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.cacheDir.deleteRecursively()
                                    cacheSizeKb = 0L
                                    Toast.makeText(context, "کاش بە سەرکەوتوویی پاککرایەوە", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "کاش پاککرایەوە", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.CleaningServices, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("پاککردنەوە")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Network Diagnostics
            Text(text = "دۆخی پەیوەندی بە تۆڕ:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
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
                        Text(text = "باری ئینتەرنێت:", style = MaterialTheme.typography.bodyMedium)
                        StatusBadge(
                            text = if (isOnline) "پەیوەستە بە ئینتەرنێت" else "پچڕاوە (ئۆفڵاین)",
                            color = if (isOnline) SpaceAccentEmerald else SpaceAccentAmber
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isOnline)
                            "هەموو مۆدێلە دەرەکییەکان، دروستکردنی ئەپ و وێبسایت و چاتی زیرەک بەردەستن."
                        else
                            "ئامرازەکانی دەستکاریکردنی وێنە، ڤیدیۆ و دروستکردنی PDF بە شێوەی ئۆفڵاین کار دەکەن.",
                        style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary, lineHeight = 18.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App & Creator Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SpacePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ABDULBASIT SPACE",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SpacePrimary)
                            )
                            Text(
                                text = "وەشانی ١.٠ • پلاتفۆرمی مۆبایلی تایبەت",
                                style = MaterialTheme.typography.labelSmall.copy(color = SpaceTextSecondary)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ئەم ئەپە بە تایبەتی بۆ عەبدولباست دروستکراوە بۆ ئەنجامدانی کارەکانی دیزاین، دەستکاری میدیا، کۆدنووسین و زیرەکی دەستکرد لەسەر مۆبایل بە زمانی کوردیی پاراو.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextPrimary, lineHeight = 20.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
