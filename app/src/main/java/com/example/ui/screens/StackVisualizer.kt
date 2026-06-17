package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VismoraViewModel
import com.example.ui.theme.VismoraThemeColors
import kotlinx.coroutines.delay

/**
 * A highly animated 2D Stack Visualizer simulating fluid layout animations (as in Framer Motion).
 * It provides custom bouncy drop physics for 'Push' and lifting rocket exit animations for 'Pop'.
 */
@Composable
fun StackVisualizer(
    viewModel: VismoraViewModel,
    colors: VismoraThemeColors,
    fontMultiplier: Float,
    modifier: Modifier = Modifier
) {
    val stackList = viewModel.stackElements
    val isArrayBased = viewModel.isStackArrayBased.value

    // Keep track of previously rendered stack size to distinguish push vs pop
    var prevSize by remember { mutableStateOf(stackList.size) }
    // Triggers local visual state updates
    var lastOperation by remember { mutableStateOf<String?>(null) } // "push", "pop", or null

    LaunchedEffect(stackList.size) {
        if (stackList.size > prevSize) {
            lastOperation = "push"
        } else if (stackList.size < prevSize) {
            lastOperation = "pop"
        }
        prevSize = stackList.size
        delay(800)
        lastOperation = null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.surface,
                        colors.surface.copy(alpha = 0.6f)
                    )
                )
            )
            .border(
                BorderStroke(1.5.dp, colors.cardBorder),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Core 2D beaker/container
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Canister Header Metadata / Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Stack Structure",
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CANISTER ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                }

                // Size Badge
                Surface(
                    color = colors.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "SIZE: ${stackList.size} / 8",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Canister beaker representation
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .weight(1f)
                    .background(
                        colors.background.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .border(
                        BorderStroke(3.dp, colors.cardBorder),
                        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Background grid depth ticks
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(6) { tick ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.cardBorder.copy(alpha = 0.15f))
                        )
                    }
                }

                // Vertical Column of stacked boxes
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (stackList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = colors.textSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "CANISTER EMPTY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        // Render elements top-to-bottom or bottom-to-top depending on your visual model.
                        // Standard human expectation for gravity beaker is index 0 (top of stack) on the top, and bottom of stack on the bottom.
                        // So we reverse the list for visual layout drawing! (Bottom item of list printed first, Top of stack printed last at top).
                        val reversedElements = stackList.asReversed()

                        reversedElements.forEachIndexed { revIdx, value ->
                            // The actual stack index (index 0 is the top, index size-1 is the bottom)
                            val actualStackIdx = stackList.size - 1 - revIdx
                            val isTop = actualStackIdx == 0

                            // Bouncy land spring scale/offset animation
                            val itemScaleState = remember { Animatable(0f) }

                            // Trigger spring physics animation when element is first layout-rendered
                            LaunchedEffect(value) {
                                itemScaleState.snapTo(if (lastOperation == "push" && isTop) 0.6f else 1f)
                                itemScaleState.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }

                            // Glowing brush for Top LIFO element
                            val boxModifier = if (isTop) {
                                Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                                    .scale(itemScaleState.value)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                colors.primary,
                                                colors.accent
                                            )
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .scale(itemScaleState.value)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.surface)
                                    .border(1.2.dp, colors.cardBorder, shape = RoundedCornerShape(6.dp))
                            }

                            Box(
                                modifier = boxModifier.padding(vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Index tag or indicator
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isTop) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Top Indicator",
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "TOP",
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        } else {
                                            Text(
                                                text = "idx:$actualStackIdx",
                                                color = colors.textSecondary,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Display value
                                    Text(
                                        text = value.toString(),
                                        color = if (isTop) Color.White else colors.text,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    // Address Pointer display based on structural toggle configuration
                                    Text(
                                        text = if (isArrayBased) "[${actualStackIdx}]" else "•->",
                                        color = if (isTop) Color.White.copy(alpha = 0.6f) else colors.textSecondary.copy(alpha = 0.4f),
                                        fontSize = 7.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (revIdx < reversedElements.size - 1) {
                                Spacer(modifier = Modifier.height(5.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
