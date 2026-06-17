package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.VismoraViewModel
import com.example.ui.theme.VismoraThemeColors
import kotlinx.coroutines.launch

@Composable
fun DsaScreen(
    viewModel: VismoraViewModel,
    colors: VismoraThemeColors,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.userProgress.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val selectedSubTab = viewModel.selectedDsaConcept.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // --- 1. CONCEPTS TABS ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val subtabs = listOf(
                Pair("stack", "🥞 STACK (LIFO)"),
                Pair("queue", "🎟️ QUEUE (FIFO)"),
                Pair("sort", "📊 SORT ANIMATOR"),
                Pair("search", "🔍 BINARY SEARCH")
            )
            subtabs.forEach { (tabId, label) ->
                val isActive = selectedSubTab == tabId
                Button(
                    onClick = { viewModel.selectedDsaConcept.value = tabId },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) colors.primary else colors.background
                    ),
                    border = BorderStroke(1.dp, if (isActive) colors.primary else colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("dsa_tab_$tabId")
                ) {
                    Text(
                        text = label,
                        color = if (isActive) Color.White else colors.text,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- 2. ACTIVE VIEWPORT PORT ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (selectedSubTab) {
                "stack" -> StackVisualizerSection(viewModel, colors, progress.fontSizeMultiplier)
                "queue" -> QueueVisualizerSection(viewModel, colors, progress.fontSizeMultiplier)
                "sort" -> SortVisualizerSection(viewModel, colors, progress.fontSizeMultiplier)
                "search" -> SearchVisualizerSection(viewModel, colors, progress.fontSizeMultiplier)
            }
        }
    }
}

// =================== STACK SECTION ===================
@Composable
fun StackVisualizerSection(viewModel: VismoraViewModel, colors: VismoraThemeColors, fontMultiplier: Float) {
    var customNumText by remember { mutableStateOf("48") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🥞 LIFO Canister Engine",
            fontSize = (18f * fontMultiplier).sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )
        Text(
            text = "Elements are pushed and popped strictly from the top (Last-In, First-Out rule).",
            fontSize = 12.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Complexity Stats Banner
        ComplexityBanner(time = "O(1) push/pop", space = "O(N) total memory", realLife = "Back-button stack, undo system, call frames.", colors = colors)

        Spacer(modifier = Modifier.height(14.dp))

        // Toggle array vs linked list stack visual style
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (viewModel.isStackArrayBased.value) "📁 Array Canister Style" else "🔗 Nodes Chain Style",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
            TextButton(onClick = { viewModel.isStackArrayBased.value = !viewModel.isStackArrayBased.value }) {
                Text("Toggle Structure", fontSize = 11.sp, color = colors.accent)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Highly animated 2D StackVisualizer component simulating Framer Motion drop/lift visual layout physics
            StackVisualizer(
                viewModel = viewModel,
                colors = colors,
                fontMultiplier = fontMultiplier,
                modifier = Modifier.weight(1.1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Sidebar Controls panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Interactive controllers
                Column {
                    Text(text = "INPUT VALUE", fontSize = 10.sp, color = colors.textSecondary)
                    OutlinedTextField(
                        value = customNumText,
                        onValueChange = { customNumText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("stack_input"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = colors.text),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.cardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val v = customNumText.toIntOrNull() ?: (10..99).random()
                            viewModel.pushStack(v)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stack_push_btn")
                    ) {
                        Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PUSH TOP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.popStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, colors.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stack_pop_btn")
                    ) {
                        Icon(Icons.Default.ArrowUpward, null, tint = colors.text, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("POP TOP", color = colors.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Estimated live stats
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Live Memory calculation:", fontSize = 9.sp, color = colors.textSecondary)
                        Text(
                            text = "${viewModel.stackElements.size * 16} Bytes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Operations logs list
        Text(text = "📃 Operation Logs trace:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier.fillMaxWidth().height(90.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.stackLog) { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, null, tint = colors.primary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(log, fontSize = 10.sp, color = colors.text)
                    }
                }
            }
        }
    }
}

// =================== QUEUE SECTION ===================
@Composable
fun QueueVisualizerSection(viewModel: VismoraViewModel, colors: VismoraThemeColors, fontMultiplier: Float) {
    var customNumText by remember { mutableStateOf("9") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🎟️ FIFO Queue Highway",
            fontSize = (18f * fontMultiplier).sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )
        Text(
            text = "First-in, First-out Pipeline. Enqueue elements at Rear port, dequeue at Front port.",
            fontSize = 12.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ComplexityBanner(time = "O(1) insertion", space = "O(N) length", realLife = "Print schedulers, thread execution pools.", colors = colors)

        Spacer(modifier = Modifier.height(12.dp))

        // Highway Configuration Selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val qModes = listOf("standard" to "Standard", "priority" to "Priority", "circular" to "Circular")
            qModes.forEach { (modeId, sLabel) ->
                val isActive = viewModel.queueType.value == modeId
                InputChip(
                    selected = isActive,
                    onClick = { viewModel.queueType.value = modeId },
                    label = { Text(sLabel, fontSize = 10.sp, color = colors.text) },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = colors.primary.copy(alpha = 0.2f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Queue visual conveyor belt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(colors.surface, shape = RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, colors.cardBorder), shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit labels (Front)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DEQUEUE FRONT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    Icon(Icons.Default.ArrowBack, null, tint = colors.primary)
                }

                // Main Queue Nodes Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
                ) {
                    if (viewModel.queueElements.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Queue empty! Enqueue nodes.", fontSize = 11.sp, color = colors.textSecondary)
                        }
                    } else {
                        viewModel.queueElements.forEachIndexed { idx, value ->
                            val isFront = idx == 0
                            val isRear = idx == viewModel.queueElements.size - 1
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .background(
                                        if (isFront) colors.primary 
                                        else if (isRear) colors.accent.copy(alpha = 0.85f)
                                        else colors.background,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, colors.cardBorder, shape = RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (isFront) "FRONT" else if (isRear) "REAR" else "IDX:$idx",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFront || isRear) Color.Black else colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = value.toString(),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (isFront || isRear) Color.Black else colors.text
                                    )
                                }
                            }
                        }
                    }
                }

                // Rear port labels
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ENQUEUE REAR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                    Icon(Icons.Default.ArrowBack, null, tint = colors.accent)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Queue operational controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = customNumText,
                    onValueChange = { customNumText = it },
                    label = { Text("Rear Node Value", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("queue_input"),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = colors.text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.cardBorder
                    )
                )
            }

            Button(
                onClick = {
                    val v = customNumText.toIntOrNull() ?: (1..99).random()
                    viewModel.enqueue(v)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(52.dp)
                    .weight(1f)
                    .testTag("queue_enqueue_btn")
            ) {
                Text("ENQUEUE (REAR)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.dequeue() },
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier
                    .height(52.dp)
                    .weight(1f)
                    .testTag("queue_dequeue_btn")
            ) {
                Text("DEQUEUE FRONT", color = colors.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Queue logging Trace
        Text(text = "🎟️ Queue Log trace:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier.fillMaxWidth().height(90.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.queueLog) { log ->
                    Text("• $log", fontSize = 10.sp, color = colors.text, modifier = Modifier.padding(vertical = 1.dp))
                }
            }
        }
    }
}

// =================== SORT SECTION ===================
@Composable
fun SortVisualizerSection(viewModel: VismoraViewModel, colors: VismoraThemeColors, fontMultiplier: Float) {
    val activeIndices = viewModel.currentSortingIndices.value
    val isRunning = viewModel.isSortingRunning.value

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "📊 Sort Algorithm Animator",
            fontSize = (18f * fontMultiplier).sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )
        Text(
            text = "Watch the swap and pivot indicators examine positions step-by-step.",
            fontSize = 12.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ComplexityBanner(
            time = if (viewModel.sortingAlgorithm.value == "bubble") "O(N^2) avg swaps" else "O(N^2) selections",
            space = "O(1) aux space",
            realLife = "Database paging, alphabetical ordering, leaderboards.",
            colors = colors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Algo Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val algorithms = listOf("bubble" to "Bubble Sort Loop", "selection" to "Selection Min Finder")
            algorithms.forEach { (algoId, label) ->
                val isActive = viewModel.sortingAlgorithm.value == algoId
                Button(
                    onClick = { viewModel.sortingAlgorithm.value = algoId },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) colors.primary else colors.surface
                    ),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(label, color = colors.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-time Bar display viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(colors.surface, shape = RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, colors.cardBorder), shape = RoundedCornerShape(12.dp))
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                viewModel.sortingArray.forEachIndexed { index, value ->
                    val isCompared = index == activeIndices.first || index == activeIndices.second
                    // Scale height
                    val barHeightFraction = value.toFloat() / 100f
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = value.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompared) colors.accent else colors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(barHeightFraction * 0.85f)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isCompared) colors.accent 
                                    else colors.primary.copy(alpha = 0.7f)
                                )
                                .border(
                                    width = if (isCompared) 2.dp else 0.dp,
                                    color = if (isCompared) colors.accent else Color.Transparent,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sorting Stats indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SortStatCard(label = "COMPARISONS", value = viewModel.sortingComps.value.toString(), colors = colors, modifier = Modifier.weight(1f))
            SortStatCard(label = "SWAPS PERFORMED", value = viewModel.sortingSwaps.value.toString(), colors = colors, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.resetSortingArray() },
                colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, colors.cardBorder),
                modifier = Modifier.weight(1f).testTag("sort_reset_btn")
            ) {
                Text("RESET RANDOM", color = colors.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.runSortingVis() },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(8.dp),
                enabled = !isRunning,
                modifier = Modifier.weight(1f).testTag("sort_start_btn")
            ) {
                Text(if (isRunning) "SORTING..." else "RUN SORTING Loop", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SortStatCard(label: String, value: String, colors: VismoraThemeColors, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary)
        }
    }
}

// =================== SEARCH SECTION ===================
@Composable
fun SearchVisualizerSection(viewModel: VismoraViewModel, colors: VismoraThemeColors, fontMultiplier: Float) {
    val low = viewModel.currentSearchLow.value
    val high = viewModel.currentSearchHigh.value
    val mid = viewModel.currentSearchMid.value
    val foundState = viewModel.searchResultFound.value

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🔍 Divide-and-Conquer Binary Search",
            fontSize = (18f * fontMultiplier).sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )
        Text(
            text = "Eliminate half of the search region at each step by comparing mid candidate.",
            fontSize = 12.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ComplexityBanner(time = "O(log N) operations", space = "O(1) space", realLife = "Prefix autocompletion, range queries, dictionary find.", colors = colors)

        Spacer(modifier = Modifier.height(12.dp))

        // Target Number Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Select Target to find:", fontSize = 12.sp, color = colors.text)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val searchList = listOf(14, 33, 52, 84, 95)
                searchList.forEach { num ->
                    val isSelected = viewModel.searchTarget.value == num
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.primary else colors.surface)
                            .border(1.dp, colors.cardBorder, CircleShape)
                            .clickable { viewModel.searchTarget.value = num }
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(
                            num.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else colors.text
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cells Grid display containing binary search bounds
        Text("SORTED CODENODES CARDS GRID:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(colors.surface, shape = RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, colors.cardBorder), shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            viewModel.searchArray.forEachIndexed { idx, value ->
                val isInBounds = idx in low..high
                val isMid = idx == mid
                val isTarget = value == viewModel.searchTarget.value

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isMid -> colors.accent
                                    !isInBounds -> colors.background.copy(alpha = 0.3f)
                                    else -> colors.background
                                }
                            )
                            .border(
                                width = if (isMid) 2.dp else 1.dp,
                                color = if (isMid) colors.accent else colors.cardBorder,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = value.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMid) Color.Black else if (!isInBounds) colors.textSecondary.copy(alpha = 0.5f) else colors.text
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            isMid -> "MID"
                            idx == low -> "LOW"
                            idx == high -> "HIGH"
                            else -> idx.toString()
                        },
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isMid) colors.accent else colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Result Notification
        AnimatedVisibility(visible = foundState != null) {
            Surface(
                color = if (foundState == true) colors.primary.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, if (foundState == true) colors.primary else Color.Red),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (foundState == true) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (foundState == true) colors.primary else Color.Red
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (foundState == true) "SUCCESS! Target ${viewModel.searchTarget.value} found using logarithm divides!" else "COMPLETED: Value not found in search range.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                }
            }
        }

        Button(
            onClick = { viewModel.performBinarySearch() },
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_start_btn")
        ) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("RUN BINARY SEARCH TIMELINES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step-by-Step Search logs
        Text(text = "🔍 Midpoint Pivot History Log:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier.fillMaxWidth().height(100.dp)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.searchSteps) { step ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(6.dp).clip(CircleShape).background(colors.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(step, fontSize = 10.sp, color = colors.text)
                    }
                }
            }
        }
    }
}

// Common complexity statistics banner
@Composable
fun ComplexityBanner(time: String, space: String, realLife: String, colors: VismoraThemeColors) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.cardBorder),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TIME COMPLEXITY: $time",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primary
                )
                Text(
                    text = "SPACE: $space",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.accent
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Real Life: $realLife",
                fontSize = 9.sp,
                color = colors.textSecondary
            )
        }
    }
}
