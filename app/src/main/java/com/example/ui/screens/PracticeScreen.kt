package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VismoraViewModel
import com.example.ui.theme.VismoraThemeColors

@Composable
fun PracticeScreen(
    viewModel: VismoraViewModel,
    colors: VismoraThemeColors,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var arenaMode by remember { mutableStateOf("quiz") } // quiz or code

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // --- 1. ARENA NAVIGATION CHIPS BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val modes = listOf("quiz" to "🧠 CONCEPT QUIZ ARENA", "code" to "💻 CODING ASSIGNMENT TERMINAL")
            modes.forEach { (modeId, label) ->
                val isActive = arenaMode == modeId
                Button(
                    onClick = { arenaMode = modeId },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) colors.primary else colors.background
                    ),
                    border = BorderStroke(1.dp, if (isActive) colors.primary else colors.cardBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("practice_tab_$modeId")
                ) {
                    Text(
                        text = label,
                        color = if (isActive) Color.White else colors.text,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- 2. ACTIVE WORKSPACE ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (arenaMode == "quiz") {
                QuizArenaSection(viewModel, colors)
            } else {
                CodeTerminalSection(viewModel, colors)
            }
        }
    }
}

// ================= QUIZ ARENA SYSTEM =================
@Composable
fun QuizArenaSection(viewModel: VismoraViewModel, colors: VismoraThemeColors) {
    val currentIdx = viewModel.currentQuizIdx.value
    val question = viewModel.quizzes[currentIdx]
    val chosenOption = viewModel.chosenQuizOption.value
    val isCorrect = viewModel.quizCorrectAnswerGiven.value

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🧠 CONCEPT CHALLENGE ARENA",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.primary
            )
            Text(
                text = "Challenge ${currentIdx + 1} of ${viewModel.quizzes.size}",
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Question display box
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = question.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Options List Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            question.options.forEachIndexed { optIdx, title ->
                val optionIsChosen = chosenOption == optIdx
                val optionIsCorrectChoice = optIdx == question.correctIdx

                val btnBgColor = when {
                    chosenOption != null && optionIsCorrectChoice -> Color(0xFF105B28) // highlight correct green
                    optionIsChosen && !isCorrect!! -> Color(0xFF7D1B1B) // highlight incorrect red
                    else -> colors.surface
                }

                val btnBorderColor = when {
                    chosenOption != null && optionIsCorrectChoice -> Color(0xFF10B981)
                    optionIsChosen && !isCorrect!! -> Color.Red
                    else -> colors.cardBorder
                }

                Surface(
                    color = btnBgColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.1.dp, btnBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = chosenOption == null) {
                            viewModel.submitQuizChoice(optIdx)
                        }
                        .testTag("quiz_option_$optIdx")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter badge (A, B, C, D)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.cardBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ('A' + optIdx).toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = title,
                            fontSize = 13.sp,
                            color = colors.text,
                            modifier = Modifier.weight(1f)
                        )

                        // Suffix Correct/Incorrect Mark
                        if (chosenOption != null) {
                            if (optionIsCorrectChoice) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
                            } else if (optionIsChosen) {
                                Icon(Icons.Default.Cancel, null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feedback explanation block
        AnimatedVisibility(visible = chosenOption != null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.dp, colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isCorrect == true) "🎉 CORRECT ANSWER! (+30 XP)" else "❌ NOT QUITE. STUDY CAREFULLY!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCorrect == true) Color(0xFF10B981) else Color.Red
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            fontSize = 12.sp,
                            color = colors.text
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.nextQuiz() },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("quiz_next_btn")
                ) {
                    Text("NEXT TRIVIA CONCEPTS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ================= CODE PRACTICE SYSTEM =================
@Composable
fun CodeTerminalSection(viewModel: VismoraViewModel, colors: VismoraThemeColors) {
    val activeChallenge = viewModel.challenges[viewModel.activeChallengeIdx.value]
    val compilationFeedback = viewModel.compilationOutputOutput.value
    val runPassed = viewModel.challengePassed.value

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "💻 COMPILER INTERACTIVE TERMINAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Select task chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.challenges.forEachIndexed { index, item ->
                val isActive = viewModel.activeChallengeIdx.value == index
                FilterChip(
                    selected = isActive,
                    onClick = { viewModel.selectChallenge(index) },
                    label = { Text(item.title, fontSize = 11.sp) },
                    modifier = Modifier.testTag("code_challenge_chip_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Problem Description
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.cardBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PROBLEM:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                    Badge(containerColor = colors.primary) {
                        Text(
                            text = "+${activeChallenge.xpReward} XP",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeChallenge.problem,
                    fontSize = 12.sp,
                    color = colors.text
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text writing code editor box (terminal style)
        Text(
            text = "TERMINAL SYNTAX WRITER (Python):",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = viewModel.currentCodeDraft.value,
            onValueChange = { viewModel.currentCodeDraft.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color.Black, shape = RoundedCornerShape(10.dp))
                .border(BorderStroke(1.dp, colors.cardBorder), shape = RoundedCornerShape(10.dp))
                .testTag("code_text_editor"),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFF00FF88) // Matrix green console syntax
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Actions
        Button(
            onClick = { viewModel.compileAndRunChallengeCode() },
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("compiler_run_btn")
        ) {
            Icon(Icons.Default.Terminal, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("COMPILE & VERIFY CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Live sandbox compilation outputs stdout console
        if (compilationFeedback != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SANDBOX COMPILER STDOUT MONITOR:",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(
                        width = 1.1.dp,
                        color = if (runPassed == true) Color(0xFF10B981) else if (runPassed == false) Color.Red else colors.cardBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (runPassed == true) Icons.Default.CheckCircle else if (runPassed == false) Icons.Default.Cancel else Icons.Default.Memory,
                            contentDescription = null,
                            tint = if (runPassed == true) Color(0xFF10B981) else if (runPassed == false) Color.Red else colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (runPassed == true) "TEST VERIFIED! assignment PASSED" else if (runPassed == false) "COMPILE ERROR: TEST FAILED" else "PROCESSING TEST FRAMES...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.text
                        )
                    }
                    Text(
                        text = compilationFeedback,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
