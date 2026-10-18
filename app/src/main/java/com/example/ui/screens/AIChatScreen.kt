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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.ChatMessageEntity
import com.example.data.remote.GeminiClient
import com.example.ui.components.SpaceTopBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AIChatScreen(
    isOnline: Boolean,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val chatDao = database.chatDao()

    val messages by chatDao.getMessages("default").collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "چۆن وێنەیەک بە فۆتۆشۆپ جوانتر بکەم؟",
        "کۆدێکی کورتی Jetpack Compose بۆ مێنیو بنووسە",
        "سناریۆیەکی کورتی ڕیکلام بۆ ڤیدیۆی تیکتۆک",
        "چۆن وێبسایتێک خێراتر بکەم؟",
        "بیرۆکەی لۆگۆ بۆ ABDULBASIT SPACE"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            SpaceTopBar(
                title = "گفتوگۆی AI • ABDULBASIT SPACE",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                isOnline = isOnline,
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                chatDao.clearConversation("default")
                                Toast.makeText(context, "چاتەکە پاککرایەوە", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("action_clear_chat")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "سڕینەوەی چات",
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
        ) {
            // Quick preset prompt chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier.clickable {
                            inputText = prompt
                        }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall.copy(color = SpacePrimary),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Message List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = SpacePrimary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "سڵاو عەبدولباست! چۆن دەتوانم ئەمڕۆ یارمەتیت بدەم؟",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "دەتوانیت پرسیار بکەیت دەربارەی وێنە، ڤیدیۆ، دروستکردنی ئەپ و وێبسایت، یان نووسینی دەق بە زمانی کوردی.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SpaceTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { msg ->
                            val isUser = msg.sender == "USER"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    color = if (isUser) SpacePrimary else SpaceCardBg,
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    ),
                                    border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder),
                                    modifier = Modifier.widthIn(max = 300.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = msg.content,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (isUser) Color.Black else SpaceTextPrimary,
                                                lineHeight = 22.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (isUser) "تۆ" else "ABDULBASIT AI",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (isUser) Color.Black.copy(alpha = 0.6f) else SpaceTextMuted,
                                                    fontSize = 10.sp
                                                )
                                            )
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("AI Response", msg.content))
                                                    Toast.makeText(context, "کۆپی کرا", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "کۆپیکردن",
                                                    tint = if (isUser) Color.Black.copy(alpha = 0.6f) else SpaceTextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Surface(
                            color = SpaceCardBg,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SpacePrimary)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = SpacePrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI بیر دەکاتەوە...",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SpacePrimary)
                                )
                            }
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, SpaceCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "داواکارییەکەت بە کوردی بنووسە...",
                                style = MaterialTheme.typography.bodySmall.copy(color = SpaceTextMuted)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SpacePrimary,
                            unfocusedBorderColor = SpaceCardBorder,
                            focusedContainerColor = SpaceCardBg,
                            unfocusedContainerColor = SpaceCardBg
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val userText = inputText.trim()
                            if (userText.isNotBlank() && !isLoading) {
                                inputText = ""
                                isLoading = true

                                coroutineScope.launch {
                                    chatDao.insertMessage(
                                        ChatMessageEntity(
                                            sender = "USER",
                                            content = userText
                                        )
                                    )

                                    if (!isOnline) {
                                        val offlineReply = "لە دۆخی ئۆفڵایندایت. خزمەتگوزاری مۆدێلە دەرەکییەکان پێویستیان بە ئینتەرنێتە، بەڵام دەتوانیت ئامرازەکانی وێنە، ڤیدیۆ و PDF بە شێوەی ئۆفڵاین بەکاربهێنیت."
                                        chatDao.insertMessage(
                                            ChatMessageEntity(
                                                sender = "AI",
                                                content = offlineReply
                                            )
                                        )
                                        isLoading = false
                                        return@launch
                                    }

                                    val result = GeminiClient.generateContent(
                                        context = context,
                                        prompt = userText,
                                        systemInstruction = "You are ABDULBASIT SPACE's intelligent assistant. Respond in fluent, polite Kurdish Sorani. Help with coding, image ideas, video plans, and digital apps."
                                    )

                                    result.fold(
                                        onSuccess = { reply ->
                                            chatDao.insertMessage(
                                                ChatMessageEntity(
                                                    sender = "AI",
                                                    content = reply
                                                )
                                            )
                                        },
                                        onFailure = { err ->
                                            val errMsg = err.message ?: "هەڵەی نەزانراو لە کاتی پەیوەندی بە AI"
                                            chatDao.insertMessage(
                                                ChatMessageEntity(
                                                    sender = "AI",
                                                    content = "هەڵە: $errMsg\nئەگەر کلیلت نییە، دەتوانیت لە بەشی ڕێکخستنی کلیلەکان کلیلی Gemini دابنێیت."
                                                )
                                            )
                                        }
                                    )

                                    isLoading = false
                                }
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) SpacePrimary else SpaceCardBorder)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "ناردن",
                            tint = if (inputText.isNotBlank()) Color.Black else SpaceTextMuted
                        )
                    }
                }
            }
        }
    }
}
