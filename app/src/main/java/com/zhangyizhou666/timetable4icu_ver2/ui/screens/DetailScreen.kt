package com.zhangyizhou666.timetable4icu_ver2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zhangyizhou666.timetable4icu_ver2.data.TimetableViewModel
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.DarkBlue
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.Silver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: TimetableViewModel,
    dayIndex: Int,
    periodIndex: Int
) {
    val cellName = viewModel.convertIndicesToDay(dayIndex, periodIndex)
    val courseTitle = viewModel.array[dayIndex][periodIndex]
    val instructor = viewModel.arrayInstructor[dayIndex][periodIndex]
    val schedule = viewModel.arraySchedule[dayIndex][periodIndex]
    val room = viewModel.arrayRoom[dayIndex][periodIndex]
    val mode = viewModel.arrayMode[dayIndex][periodIndex]
    val color = viewModel.arrayColor[dayIndex][periodIndex]
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Course Details",
                        color = Silver
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBlue
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Silver
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Course Info Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Course Info",
                            color = Color.Blue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            InfoRow("Title", courseTitle)
                            InfoRow("Instructor", instructor)
                            InfoRow("Schedule", schedule)
                            InfoRow("Room", room)
                            InfoRow("Mode", mode)
                            InfoRow("Color", color)
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete")
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Button(
                            onClick = { navController.navigate("edit/$dayIndex/$periodIndex") },
                            Modifier.weight(1f)
                        ) {
                            Text("Edit")
                        }
                    }
                }
            }
        }
    }
    
    // Delete Course Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Course") },
            text = { Text("Are you sure you want to delete this course? This will remove it from all cells.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCourse(courseTitle)
                        showDeleteConfirmation = false
                        navController.popBackStack()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.width(90.dp)) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Text(
            text = ":",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        Text(
            text = value.ifEmpty { "None" },
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
} 