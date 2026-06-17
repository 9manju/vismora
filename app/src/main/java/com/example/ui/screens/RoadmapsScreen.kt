package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VismoraViewModel
import com.example.ui.theme.VismoraThemeColors
import com.example.data.RoadmapNode

@Composable
fun RoadmapsScreen(
    viewModel: VismoraViewModel,
    colors: VismoraThemeColors,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedNodeIdx by remember { mutableStateOf(0) }
    val node = viewModel.roadmaps[selectedNodeIdx]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        // --- 1. ROADMAP HEADER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🗺️ Developer Career Roadmaps",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Interactive, step-by-step tracks to master coding and secure interviews fearlessly.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // --- 2. DEVELOPMENT SELECTION CHIPS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val domains = listOf("Python Developer Pathway", "AI Engineer Pathway", "Full Stack System Designer")
            domains.forEachIndexed { i, label ->
                val isActive = i == 0
                FilterChip(
                    selected = isActive,
                    onClick = {},
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                        selectedLabelColor = colors.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. TIMELINE GRAPH ROW CONSOLE ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Visual vertical timeline spine
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(45.dp)
                    .padding(top = 12.dp)
            ) {
                viewModel.roadmaps.forEachIndexed { idx, item ->
                    val isActive = selectedNodeIdx == idx
                    
                    // Sphere Node
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isActive) colors.primary else colors.surface)
                            .border(
                                width = if (isActive) 2.5.dp else 1.dp,
                                color = if (isActive) colors.primary else colors.cardBorder,
                                shape = CircleShape
                            )
                            .clickable { selectedNodeIdx = idx }
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = when (idx) {
                                0 -> Icons.Default.DirectionsRun
                                1 -> Icons.Default.AltRoute
                                2 -> Icons.Default.Storage
                                else -> Icons.Default.AutoAwesome
                            },
                            contentDescription = null,
                            tint = if (isActive) Color.White else colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Vertical Connector line
                    if (idx < viewModel.roadmaps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(56.dp)
                                .background(colors.cardBorder)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Milestone description text list
            Column(modifier = Modifier.weight(1f)) {
                viewModel.roadmaps.forEachIndexed { idx, item ->
                    val isActive = selectedNodeIdx == idx
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) colors.surface else Color.Transparent
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isActive) colors.primary else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clickable { selectedNodeIdx = idx }
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Badge(
                                    containerColor = when (item.difficulty) {
                                        "Beginner" -> Color(0xFF0F5132)
                                        "Intermediate" -> Color(0xFF664D03)
                                        else -> colors.primary
                                    }
                                ) {
                                    Text(item.difficulty.uppercase(), fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isActive) colors.primary else colors.text
                                )
                                Text(
                                    text = "Earns +${item.xpReward} XP Milestone",
                                    fontSize = 10.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- 4. DETAILS SECTION FOR SELECTED NODE ---
        Text(
            text = "📖 SYLLABUS DETAIL INDEX",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

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
                    text = node.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primary
                )
                Text(
                    text = node.description,
                    fontSize = 12.sp,
                    color = colors.text,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Skills Taught List
                Text(
                    text = "🔑 INTERVIEW-VALUED SKILLS TAUGHT:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    node.skills.forEach { skill ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(skill, fontSize = 9.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Suggesed Projects List
                Text(
                    text = "🚀 PORTFOLIO MINI PROJECTS:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                node.projects.forEach { prj ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FolderOpen, null, tint = colors.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(prj, fontSize = 11.sp, color = colors.text)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Suggest certifications
                Text(
                    text = "🏆 SUGGESTED INDUSTRY VERIFICATIONS:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Text(
                    text = "Google Professional Cloud Dev, PCEP Entry Python, AWS Certified AI Practitioner.",
                    fontSize = 11.sp,
                    color = colors.text,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mark milestone completed button!
                Button(
                    onClick = { viewModel.awardXp(node.xpReward) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("roadmap_claim_btn")
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CLAIM MILESTONE XP (+${node.xpReward} XP)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
