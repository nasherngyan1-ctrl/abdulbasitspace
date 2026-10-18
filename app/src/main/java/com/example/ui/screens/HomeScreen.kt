package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SpaceTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

data class HomeToolItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val badge: String? = null
)

@Composable
fun HomeScreen(
    isOnline: Boolean,
    onNavigate: (String) -> Unit
) {
    val tools = listOf(
        HomeToolItem(
            id = "ai_chat",
            title = "١. گفتوگۆی AI",
            subtitle = "یاریدەدەری زیرەکی کوردی و کۆد",
            icon = Icons.Default.Chat,
            gradientColors = listOf(SpacePrimary, SpaceSecondary),
            badge = "Gemini"
        ),
        HomeToolItem(
            id = "image_editor",
            title = "٢. دەستکاریکردنی وێنە",
            subtitle = "ئامرازە پێشکەوتووەکان (Picsart)",
            icon = Icons.Default.Tune,
            gradientColors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)),
            badge = "ئۆفڵاین"
        ),
        HomeToolItem(
            id = "ai_image",
            title = "٣. دروستکردنی وێنە بە AI",
            subtitle = "دەق بۆ وێنە و دروستکردنی لۆگۆ",
            icon = Icons.Default.AutoFixHigh,
            gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
        ),
        HomeToolItem(
            id = "video_editor",
            title = "٤. دەستکاریکردنی ڤیدیۆ",
            subtitle = "Timeline، بڕین و فلتەر (CapCut)",
            icon = Icons.Default.MovieFilter,
            gradientColors = listOf(Color(0xFFF97316), Color(0xFFEF4444)),
            badge = "ئۆفڵاین"
        ),
        HomeToolItem(
            id = "ai_video",
            title = "٥. دروستکردنی ڤیدیۆ بە AI",
            subtitle = "سناریۆ بۆ دیمەن و پلانی ڤیدیۆ",
            icon = Icons.Default.VideoCall,
            gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
        ),
        HomeToolItem(
            id = "app_builder",
            title = "٦. دروستکردنی ئەپ بە AI",
            subtitle = "کۆدی ئەندرۆید و پێشبینینی ڕاستەوخۆ",
            icon = Icons.Default.Android,
            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669))
        ),
        HomeToolItem(
            id = "web_builder",
            title = "٧. دروستکردنی وێبسایت بە AI",
            subtitle = "HTML/CSS/JS لەگەڵ پێشبینینی مۆبایل",
            icon = Icons.Default.Language,
            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
        ),
        HomeToolItem(
            id = "deep_analysis",
            title = "٨. توێژینەوە و شیکردنەوە",
            subtitle = "EXIF، مێتاداتا، ڕەنگ و ناسینەوە",
            icon = Icons.Default.ManageSearch,
            gradientColors = listOf(Color(0xFF14B8A6), Color(0xFF0D9488))
        ),
        HomeToolItem(
            id = "pdf_studio",
            title = "٩. PDF و فایلەکان",
            subtitle = "دروستکردنی بەڵگەنامەی کوردی",
            icon = Icons.Default.PictureAsPdf,
            gradientColors = listOf(Color(0xFFE11D48), Color(0xFFBE123C)),
            badge = "ئۆفڵاین"
        ),
        HomeToolItem(
            id = "api_keys",
            title = "١٠. بەڕێوەبردنی کلیلەکانی AI",
            subtitle = "ڕێکخستنی Gemini، مۆدێل و تێست",
            icon = Icons.Default.VpnKey,
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
        ),
        HomeToolItem(
            id = "projects",
            title = "١١. پڕۆژەکانم",
            subtitle = "فایل، کۆد و دەستکارییەکان",
            icon = Icons.Default.FolderSpecial,
            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
        ),
        HomeToolItem(
            id = "settings",
            title = "١٢. ڕێکخستنەکان",
            subtitle = "دۆخی تاریک، داتا و زانیاری ئەپ",
            icon = Icons.Default.Settings,
            gradientColors = listOf(Color(0xFF64748B), Color(0xFF334155))
        )
    )

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "ABDULBASIT SPACE",
                canNavigateBack = false,
                isOnline = isOnline,
                actions = {
                    IconButton(
                        onClick = { onNavigate("settings") },
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "ڕێکخستنەکان",
                            tint = MaterialTheme.colorScheme.onBackground
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Hero Space Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(SpacePrimary, SpaceAccentPurple)),
                        RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = SpaceCardBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    SpacePrimary.copy(alpha = 0.15f),
                                    SpaceAccentPurple.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(SpacePrimary, SpaceAccentPurple)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ABDULBASIT SPACE",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "ناوەندی دیجیتاڵی و زیرەکی دەستکرد",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = SpaceTextSecondary
                                        )
                                    )
                                }
                            }

                            StatusBadge(
                                text = if (isOnline) "ئۆنلاین" else "ئۆفڵاین",
                                color = if (isOnline) SpaceAccentEmerald else SpaceAccentAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "بەخێربێیت بۆ فەزای کەسیی خۆت! دەستکاری وێنە و ڤیدیۆ، دروستکردنی ئەپ و وێبسایت، چاتی AI و بەڵگەنامەکان لە یەک ئەپی مۆبایلدا ئامادەیە.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SpaceTextPrimary.copy(alpha = 0.9f),
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "بەشە سەرەکییەکان (١٢ ئامراز)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "خێرا و مۆدێرن",
                    style = MaterialTheme.typography.labelSmall.copy(color = SpacePrimary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 12 Tools Grid - 2 columns
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (chunk in tools.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (item in chunk) {
                            Box(modifier = Modifier.weight(1f)) {
                                ToolCard(item = item, onClick = { onNavigate(item.id) })
                            }
                        }
                        if (chunk.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ToolCard(
    item: HomeToolItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable(onClick = onClick)
            .testTag("tool_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(item.gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (item.badge != null) {
                    StatusBadge(
                        text = item.badge,
                        color = if (item.badge == "ئۆفڵاین") SpaceAccentEmerald else SpacePrimary
                    )
                }
            }

            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 2
                )
            }
        }
    }
}
