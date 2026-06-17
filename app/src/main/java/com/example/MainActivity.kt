package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.VismoraViewModel
import com.example.ui.screens.*
import com.example.ui.theme.VismoraThemeHolder
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: VismoraViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val progress by viewModel.userProgress.collectAsStateWithLifecycle()
      val colors = VismoraThemeHolder.getColors(progress.currentTheme)

      MyApplicationTheme(darkTheme = colors.isDark, dynamicColor = false) {
        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
          bottomBar = {
            NavigationBar(
              containerColor = colors.surface,
              tonalElevation = 8.dp,
              modifier = Modifier.testTag("app_bottom_bar")
            ) {
              val currentTab = viewModel.currentBottomTab.value

              NavigationBarItem(
                selected = currentTab == "home",
                onClick = { viewModel.currentBottomTab.value = "home" },
                icon = { Icon(Icons.Default.Home, null) },
                label = { Text("Home", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = colors.primary,
                  selectedTextColor = colors.primary,
                  unselectedIconColor = colors.textSecondary,
                  unselectedTextColor = colors.textSecondary,
                  indicatorColor = colors.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("bottom_tab_home")
              )

              NavigationBarItem(
                selected = currentTab == "dsa",
                onClick = { viewModel.currentBottomTab.value = "dsa" },
                icon = { Icon(Icons.Default.AccountTree, null) },
                label = { Text("DSA Visuals", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = colors.primary,
                  selectedTextColor = colors.primary,
                  unselectedIconColor = colors.textSecondary,
                  unselectedTextColor = colors.textSecondary,
                  indicatorColor = colors.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("bottom_tab_dsa")
              )

              NavigationBarItem(
                selected = currentTab == "tutor",
                onClick = { viewModel.currentBottomTab.value = "tutor" },
                icon = { Icon(Icons.Default.Psychology, null) },
                label = { Text("Guru AI", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = colors.primary,
                  selectedTextColor = colors.primary,
                  unselectedIconColor = colors.textSecondary,
                  unselectedTextColor = colors.textSecondary,
                  indicatorColor = colors.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("bottom_tab_tutor")
              )

              NavigationBarItem(
                selected = currentTab == "roadmaps",
                onClick = { viewModel.currentBottomTab.value = "roadmaps" },
                icon = { Icon(Icons.Default.Map, null) },
                label = { Text("Tracks", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = colors.primary,
                  selectedTextColor = colors.primary,
                  unselectedIconColor = colors.textSecondary,
                  unselectedTextColor = colors.textSecondary,
                  indicatorColor = colors.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("bottom_tab_roadmaps")
              )

              NavigationBarItem(
                selected = currentTab == "practice",
                onClick = { viewModel.currentBottomTab.value = "practice" },
                icon = { Icon(Icons.Default.EmojiEvents, null) },
                label = { Text("Practice", fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = colors.primary,
                  selectedTextColor = colors.primary,
                  unselectedIconColor = colors.textSecondary,
                  unselectedTextColor = colors.textSecondary,
                  indicatorColor = colors.primary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("bottom_tab_practice")
              )
            }
          }
        ) { innerPadding ->
          Surface(
            modifier = Modifier
              .fillMaxSize()
              .background(colors.background)
              .padding(innerPadding)
          ) {
            when (viewModel.currentBottomTab.value) {
              "home" -> HomeScreen(viewModel, colors)
              "dsa" -> DsaScreen(viewModel, colors)
              "tutor" -> TutorScreen(viewModel, colors)
              "roadmaps" -> RoadmapsScreen(viewModel, colors)
              "practice" -> PracticeScreen(viewModel, colors)
              else -> HomeScreen(viewModel, colors)
            }
          }
        }
      }
    }
  }
}
