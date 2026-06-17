package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.VismoraViewModel
import com.example.ui.theme.VismoraThemeColors
import com.example.ui.theme.VismoraThemeHolder

@Composable
fun HomeScreen(
    viewModel: VismoraViewModel,
    colors: VismoraThemeColors,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.userProgress.collectAsStateWithLifecycle()
    val badges by viewModel.earnedBadges.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Interactive animated floating scale for hero header
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val heroScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp) // space for bottom navigation
    ) {
        // --- 1. HERO SECTION & CINEMATIC HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .drawBehind {
                    // Modern background radial gradient matching active theme's primary color
                    val brush = Brush.radialGradient(
                        colors = listOf(colors.primary.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(size.width / 2, size.height / 3),
                        radius = size.width * 0.9f
                    )
                    drawRect(brush)
                    
                    // Simple cyberpunk background horizontal grid lines
                    val lineCount = 6
                    val gap = size.height / lineCount
                    for (i in 1..lineCount) {
                        val thickness = (i.toFloat() / lineCount) * 1.5f
                        drawLine(
                            color = colors.primary.copy(alpha = 0.1f * (i.toFloat() / lineCount)),
                            start = Offset(0f, i * gap),
                            end = Offset(size.width, i * gap),
                            strokeWidth = thickness
                        )
                    }
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-10).dp)
            ) {
                // Animated Floating Logo Canvas representation
                VismoraLogoCanvas(colors = colors)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "VISMORA",
                    fontSize = (28f * progress.fontSizeMultiplier).sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.text,
                    modifier = Modifier.testTag("app_logo_title")
                )

                Text(
                    text = "Where Coding Comes Alive.",
                    fontSize = (13f * progress.fontSizeMultiplier).sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Hero button
                Button(
                    onClick = { viewModel.currentBottomTab.value = "dsa" },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("start_visualizing_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Begin",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EXPLORE UNIVERSE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- 2. THEME INSTANT SWITCHER ROW ---
        Text(
            text = "🎬 CHOOSE YOUR LEARNING ATMOSPHERE",
            fontSize = (14f * progress.fontSizeMultiplier).sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val themesList = listOf(
                Triple("netflix", "Netflix Cine", colors.primary),
                Triple("developer", "Code Dev", Color(0xFF007ACC)),
                Triple("light", "Bright Day", Color(0xFF2563EB)),
                Triple("cyberpunk", "Neon Cyber", Color(0xFFEA00D9)),
                Triple("galaxy", "Nebula Orb", Color(0xFF8B5CF6)),
                Triple("nature", "Zen Forest", Color(0xFF10B981))
            )
            items(themesList) { (themeId, label, themeColor) ->
                val isActive = progress.currentTheme == themeId
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isActive) themeColor.copy(alpha = 0.25f) 
                            else colors.surface
                        )
                        .border(
                            width = if (isActive) 2.5.dp else 1.dp,
                            color = if (isActive) themeColor else colors.cardBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.switchTheme(themeId) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(themeColor)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            color = if (isActive) themeColor else colors.text,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 3. GAMIFICATION WIDGET ---
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LEVEL ${progress.level} RANK",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (18f * progress.fontSizeMultiplier).sp,
                            color = colors.primary
                        )
                        Text(
                            text = "Fearless Learning Champion",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                    
                    // Daily Active Streak Button
                    Surface(
                        color = colors.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, colors.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { viewModel.checkInStreak() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Flame",
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${progress.streakCount} DAYS",
                                color = colors.text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // XP Progress Bar
                val xpLimit = progress.level * 100
                val progressFraction = progress.xp.toFloat() / xpLimit
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${progress.xp} XP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = colors.primary,
                        trackColor = colors.cardBorder,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$xpLimit XP",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Earned Badges Row
                Text(
                    text = "🏆 RECENT AWARDS (${badges.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (badges.isEmpty()) {
                    Text(
                        text = "No badges unlocked yet! Dive into DSA or Practice to unlock achievements.",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(badges) { badge ->
                            AssistChip(
                                onClick = {},
                                label = { Text(badge.name, fontSize = 10.sp, color = colors.text) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (badge.iconName) {
                                            "psychology" -> Icons.Default.Psychology
                                            "account_tree" -> Icons.Default.AccountTree
                                            "military_tech" -> Icons.Default.MilitaryTech
                                            else -> Icons.Default.WorkspacePremium
                                        },
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(containerColor = colors.surface),
                                border = BorderStroke(1.dp, colors.cardBorder)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 4. NETFLIX HORIZONTAL CONTENT CARDS ROWS ---

        // Row A: 🔥 TRENDING DSA CONCEPTS
        ContentRowTitle(emoji = "🔥", title = "Trending DSA Concepts", multiplier = progress.fontSizeMultiplier, colors = colors)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val dsaItems = listOf(
                Triple("stack", "Stack Canister", "Drop items in LIFO orders"),
                Triple("queue", "Queue Highway", "First-in, First-out pipelines"),
                Triple("sort", "Bubble Sort", "Interactive swapping animations"),
                Triple("search", "Binary Search", "Dividing sorted indices logarithmically")
            )
            items(dsaItems) { (conceptId, name, desc) ->
                ContentCard(
                    title = name,
                    description = desc,
                    category = "DSA Engine",
                    colors = colors,
                    onClick = {
                        viewModel.selectedDsaConcept.value = conceptId
                        viewModel.currentBottomTab.value = "dsa"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Row B: 📚 PROGRAMMING LANGUAGES
        ContentRowTitle(emoji = "📚", title = "Programming Languages", multiplier = progress.fontSizeMultiplier, colors = colors)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val langItems = listOf(
                Pair("Python", "Master loops, syntax, lists and sum-challenges."),
                Pair("Java", "Object oriented compilers and robust class systems."),
                Pair("C", "Unlock memory addresses and low-level processing.")
            )
            items(langItems) { (name, desc) ->
                ContentCard(
                    title = name,
                    description = desc,
                    category = "Language Theory",
                    colors = colors,
                    onClick = { viewModel.currentBottomTab.value = "practice" }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 5. ACCESSIBILITY & AUDIO ADJUST CONTROLS PANEL ---
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚙️ ACCESSIBILITY & DISPLAY SANDBOX",
                    fontWeight = FontWeight.Bold,
                    fontSize = (13f * progress.fontSizeMultiplier).sp,
                    color = colors.text
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Adjust Display Font Scaling",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (progress.fontSizeMultiplier > 0.82f) viewModel.updateFontSize(progress.fontSizeMultiplier - 0.1f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, "Shrink", tint = colors.text)
                        }
                        Text(
                            text = "${(progress.fontSizeMultiplier * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { if (progress.fontSizeMultiplier < 1.48f) viewModel.updateFontSize(progress.fontSizeMultiplier + 0.1f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, "Grow", tint = colors.text)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "High Contrast Text Style",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Switch(
                        checked = progress.highContrastMode,
                        onCheckedChange = { viewModel.toggleHighContrast(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Achromatopsia Colorblind Aid",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Switch(
                        checked = progress.colorBlindMode,
                        onCheckedChange = { viewModel.toggleColorBlind(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }
            }
        }
    }
}

@Composable
fun ContentRowTitle(emoji: String, title: String, multiplier: Float, colors: VismoraThemeColors) {
    Text(
        text = "$emoji $title",
        fontSize = (16f * multiplier).sp,
        fontWeight = FontWeight.Bold,
        color = colors.text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
}

@Composable
fun ContentCard(
    title: String,
    description: String,
    category: String,
    colors: VismoraThemeColors,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = Modifier
            .width(170.dp)
            .height(135.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = category.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = colors.textSecondary,
                    maxLines = 3
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun VismoraLogoCanvas(colors: VismoraThemeColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = Modifier.size(72.dp)) {
        // Draw orbital shield/brain coding network
        val canvasSize = size.width
        val center = Offset(canvasSize / 2, canvasSize / 2)

        // Cosmic central outer circle (shield outline)
        drawCircle(
            color = colors.primary.copy(alpha = 0.2f),
            radius = canvasSize * 0.45f,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw dynamic rotating particles on orbital shield
        val progressAngle = Math.toRadians(rotation.toDouble())
        val px = (center.x + (canvasSize * 0.45f) * Math.cos(progressAngle)).toFloat()
        val py = (center.y + (canvasSize * 0.45f) * Math.sin(progressAngle)).toFloat()
        drawCircle(
            color = colors.accent,
            radius = 5.dp.toPx(),
            center = Offset(px, py)
        )

        // Draw coding brackets symbol: < / > (represented dynamically as intersecting vector lines)
        val path1 = Path().apply {
            moveTo(center.x - 12.dp.toPx(), center.y - 8.dp.toPx())
            lineTo(center.x - 22.dp.toPx(), center.y)
            lineTo(center.x - 12.dp.toPx(), center.y + 8.dp.toPx())
        }
        drawPath(
            path = path1,
            color = colors.primary,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        val path2 = Path().apply {
            moveTo(center.x + 12.dp.toPx(), center.y - 8.dp.toPx())
            lineTo(center.x + 22.dp.toPx(), center.y)
            lineTo(center.x + 12.dp.toPx(), center.y + 8.dp.toPx())
        }
        drawPath(
            path = path2,
            color = colors.accent,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Central lightning slash (rising arrow / gradient neural line)
        drawLine(
            color = colors.text,
            start = Offset(center.x - 4.dp.toPx(), center.y + 12.dp.toPx()),
            end = Offset(center.x + 4.dp.toPx(), center.y - 12.dp.toPx()),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
