package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.VismoraViewModel
import com.example.ui.theme.VismoraThemeColors

@Composable
fun TutorScreen(
    viewModel: VismoraViewModel,
    colors: VismoraThemeColors,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.userProgress.collectAsStateWithLifecycle()
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val aiIsLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    
    val listState = rememberLazyListState()
    var inputQueryText by remember { mutableStateOf("") }

    // Scroll to latest responses automatically
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(bottom = 60.dp) // Leave safety gap for bottom nave bars
    ) {
        // --- 1. GURU AI CHAT HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(14.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, colors.cardBorder), shape = RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mentor Avatar representation
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(colors.primary, colors.accent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Guru AI Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Guru AI",
                            fontSize = (16f * progress.fontSizeMultiplier).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.text
                        )
                        Text(
                            text = "Friendly Socratic Coding Mentor",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // Clear Logs button
                IconButton(
                    onClick = { viewModel.clearChat() }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Reset Chat logs",
                        tint = colors.textSecondary
                    )
                }
            }
        }

        // --- 2. MULTILINE DIALOGS HISTORIES ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            if (chatHistory.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = colors.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Consult Your Mentor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ask questions, understand compiler frames, dry-run bubble sort, or ask Guru AI to recommend custom roadmaps! consulting awards 15 XP.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chatHistory) { entry ->
                        val isUser = entry.role == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary)
                                        .align(Alignment.Top),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .testTag(if (isUser) "user_chat_bubble" else "model_chat_bubble"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) colors.primary.copy(alpha = 0.15f) else colors.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isUser) colors.primary else colors.cardBorder
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 12.dp
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isUser) "YOU" else "GURU AI",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isUser) colors.primary else colors.accent,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = entry.messageText,
                                        fontSize = (13f * progress.fontSizeMultiplier).sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.text,
                                        fontFamily = if (isUser) FontFamily.SansSerif else FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Glowing Floating Typing Indicator
            if (aiIsLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.cardBorder, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = colors.primary,
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guru AI is compiling pedagogical bytes...",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        // --- 3. INPUT BAR CONTROLS FIELD ---
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputQueryText,
                    onValueChange = { inputQueryText = it },
                    placeholder = { Text("Ask line-by-line code, time complexity, BFS tips...", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tutor_chat_input"),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = colors.text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        val query = inputQueryText.trim()
                        if (query.isNotEmpty() && !aiIsLoading) {
                            viewModel.askGuruAi(query)
                            inputQueryText = ""
                        }
                    },
                    modifier = Modifier.testTag("tutor_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send advice",
                        tint = colors.primary
                    )
                }
            }
        }
    }
}
