package com.zhangyizhou666.timetable4icu_ver2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zhangyizhou666.timetable4icu_ver2.data.TimetableViewModel
import com.zhangyizhou666.timetable4icu_ver2.ui.screens.EditCellScreen
import com.zhangyizhou666.timetable4icu_ver2.ui.screens.LunchEditScreen
import com.zhangyizhou666.timetable4icu_ver2.ui.screens.TimetableScreen
import com.zhangyizhou666.timetable4icu_ver2.ui.screens.DetailScreen
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.DarkBlue
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.Silver
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.Timetable4ICU_ver2Theme

class MainActivity : ComponentActivity() {
    // Create ViewModel at activity level to share between screens
    private lateinit var viewModel: TimetableViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize the ViewModel
        viewModel = TimetableViewModel()
        viewModel.initialize(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            Timetable4ICU_ver2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: TimetableViewModel) {
    val navController = rememberNavController()
    
    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "timetable",
            modifier = Modifier.padding(innerPadding)
        ) {
            // Main screen
            composable("timetable") {
                TimetableScreen(navController, viewModel)
            }
            
            // Edit cell screen
            composable(
                route = "edit/{dayIndex}/{periodIndex}",
                arguments = listOf(
                    navArgument("dayIndex") { type = NavType.IntType },
                    navArgument("periodIndex") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 0
                val periodIndex = backStackEntry.arguments?.getInt("periodIndex") ?: 0
                EditCellScreen(navController, viewModel, dayIndex, periodIndex)
            }
            
            // Lunch edit screen
            composable(
                route = "lunch/{dayIndex}/{periodIndex}",
                arguments = listOf(
                    navArgument("dayIndex") { type = NavType.IntType },
                    navArgument("periodIndex") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 0
                val periodIndex = backStackEntry.arguments?.getInt("periodIndex") ?: 0
                LunchEditScreen(navController, viewModel, dayIndex, periodIndex)
            }
            
            // Detail screen
            composable(
                route = "detail/{dayIndex}/{periodIndex}",
                arguments = listOf(
                    navArgument("dayIndex") { type = NavType.IntType },
                    navArgument("periodIndex") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 0
                val periodIndex = backStackEntry.arguments?.getInt("periodIndex") ?: 0
                DetailScreen(navController, viewModel, dayIndex, periodIndex)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Timetable4ICU_ver2Theme {
        Greeting("Android")
    }
}