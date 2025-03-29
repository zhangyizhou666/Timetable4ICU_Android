package com.zhangyizhou666.timetable4icu_ver2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.zhangyizhou666.timetable4icu_ver2.data.TimetableViewModel
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.DarkBlue
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.Silver
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.getAllCourseColors
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.getColorFromName
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.getColorNameFromIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCellScreen(
    navController: NavController,
    viewModel: TimetableViewModel,
    dayIndex: Int,
    periodIndex: Int
) {
    val context = LocalContext.current
    val cellName = viewModel.convertIndicesToDay(dayIndex, periodIndex)
    
    var courseTitle by remember { mutableStateOf(viewModel.array[dayIndex][periodIndex]) }
    var instructor by remember { mutableStateOf(viewModel.arrayInstructor[dayIndex][periodIndex]) }
    var schedule by remember { mutableStateOf(viewModel.arraySchedule[dayIndex][periodIndex].ifEmpty { cellName }) }
    var room by remember { mutableStateOf(viewModel.arrayRoom[dayIndex][periodIndex]) }
    var courseno by remember { mutableStateOf(viewModel.arrayCourseno[dayIndex][periodIndex]) }
    var mode by remember { mutableStateOf(viewModel.arrayMode[dayIndex][periodIndex]) }
    var colorName by remember { mutableStateOf(viewModel.arrayColor[dayIndex][periodIndex]) }
    var startTime by remember { mutableStateOf(viewModel.arrayStartTime[dayIndex][periodIndex]) }
    var endTime by remember { mutableStateOf(viewModel.arrayEndTime[dayIndex][periodIndex]) }
    
    var showCourseList by remember { mutableStateOf(false) }
    var filteredCourses by remember { mutableStateOf(emptyList<TimetableViewModel.CourseData>()) }
    
    // Add state for schedule editor
    var showScheduleEditor by remember { mutableStateOf(false) }
    
    // Add state for overlap warning
    var showOverlapWarning by remember { mutableStateOf(false) }
    var overlappingSlots by remember { mutableStateOf("") }
    
    LaunchedEffect(cellName) {
        // Load courses for this cell using time-based filtering
        filteredCourses = viewModel.filterCoursesForTimeSlot(dayIndex, periodIndex)
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Edit Cell",
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
                },
                actions = {
                    IconButton(onClick = {
                        // Save changes using updateMultipleCells to update all cells in the schedule
                        viewModel.updateMultipleCells(
                            dayIndex = dayIndex,
                            periodIndex = periodIndex,
                            courseTitle = courseTitle,
                            instructor = instructor,
                            schedule = schedule,
                            courseno = courseno,
                            room = room,
                            mode = mode,
                            colorName = colorName,
                            startTime = startTime,
                            endTime = endTime
                        )
                        
                        // Navigate back
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
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
            // Manual entry section
            item {
                Text(
                    text = "Manual Entry",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Divider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Course title field
                OutlinedTextField(
                    value = courseTitle,
                    onValueChange = { courseTitle = it },
                    label = { Text("Course Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Instructor field
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Instructor") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Schedule field with editor button
                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Schedule") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showScheduleEditor = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Edit Schedule"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Room field
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Course number field
                OutlinedTextField(
                    value = courseno,
                    onValueChange = { courseno = it },
                    label = { Text("Course Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Mode field
                OutlinedTextField(
                    value = mode,
                    onValueChange = { mode = it },
                    label = { Text("Teaching Mode") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Time fields
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End Time (HH:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Color selection
                Text(
                    text = "Cell Color",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    getAllCourseColors().forEachIndexed { index, colorPair ->
                        val (name, color) = colorPair
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    colorName = name
                                }
                                .then(
                                    if (name == colorName) {
                                        Modifier.borderDecorator(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Clear button
                Button(
                    onClick = {
                        courseTitle = ""
                        instructor = ""
                        schedule = cellName
                        room = ""
                        courseno = ""
                        mode = ""
                        colorName = "white"
                        startTime = ""
                        endTime = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Clear Cell")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Course list section
            item {
                Text(
                    text = "Available Courses",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Divider()
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Course list
            if (filteredCourses.isEmpty()) {
                item {
                    Text(
                        text = "No courses available for this time slot",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filteredCourses) { course ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                // Check for course overlap using time information
                                val result = viewModel.checkCourseOverlap(
                                    course.courseTitle, 
                                    course.schedule,
                                    course.startTime,
                                    course.endTime
                                )
                                val hasOverlap = result.first
                                val overlapCourses = result.second
                                
                                if (hasOverlap) {
                                    // Create a list of conflicting course titles
                                    val conflictingCourseTitles = overlapCourses.split(", ")
                                    
                                    // Remove all conflicting courses completely
                                    conflictingCourseTitles.forEach { conflictingTitle ->
                                        if (conflictingTitle.isNotEmpty()) {
                                            // Call deleteCourse to remove the entire course
                                            viewModel.deleteCourse(conflictingTitle)
                                        }
                                    }
                                    
                                    // Show a toast indicating which courses were removed
                                    Toast.makeText(
                                        context,
                                        "Removed conflicting ${if (conflictingCourseTitles.size > 1) "courses" else "course"}: $overlapCourses",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                
                                courseTitle = course.courseTitle
                                instructor = course.instructor
                                schedule = course.schedule
                                room = course.room
                                courseno = course.courseno
                                mode = course.mode
                                startTime = course.startTime
                                endTime = course.endTime
                                
                                // Assign color based on course type
                                colorName = when {
                                    course.courseno.startsWith("ELA") -> "lightBlue"
                                    course.courseno.startsWith("JLP") -> "lightGreen"
                                    course.courseno.startsWith("GEX") -> "lightPurple"
                                    course.courseno.startsWith("HSS") -> "lightOrange"
                                    course.courseno.startsWith("MCC") -> "rosePink"
                                    course.courseno.startsWith("LNG") -> "lightYellow"
                                    else -> "white"
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = course.courseTitle,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Instructor: ${course.instructor}",
                                fontSize = 14.sp
                            )
                            
                            if (course.room.isNotEmpty() && course.room != "NO DATA") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Room: ${course.room}",
                                    fontSize = 14.sp
                                )
                            }
                            
                            if (course.courseno.isNotEmpty() && course.courseno != "NO DATA") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Course No: ${course.courseno}",
                                    fontSize = 14.sp
                                )
                            }
                            
                            if (course.mode.isNotEmpty() && course.mode != "NO DATA") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mode: ${course.mode}",
                                    fontSize = 14.sp
                                )
                            }
                            
                            // Display time information if available
                            if (course.timeSlots.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Column {
                                    Text(
                                        text = "Time Slots:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF007ACC) // Blue color for time header
                                    )
                                    course.timeSlots.forEach { slot ->
                                        Text(
                                            text = "${getDayName(slot.day)}: ${slot.startTime} - ${slot.endTime}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF007ACC) // Blue color for time
                                        )
                                    }
                                }
                            } else if (course.startTime.isNotEmpty() && course.endTime.isNotEmpty()) {
                                // Fallback to old way if timeSlots is empty but we have startTime/endTime
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Time: ${course.startTime} - ${course.endTime}",
                                    fontSize = 14.sp,
                                    color = Color(0xFF007ACC) // Blue color for time
                                )
                            }
                            
                            // Display credits
                            if (course.credits > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Credits: ${course.credits}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            // Add overlap warning if needed
            if (showOverlapWarning) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF3CD))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Warning: Course Overlap",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF856404)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "This course overlaps with existing courses at: $overlappingSlots",
                                color = Color(0xFF856404)
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Show schedule editor dialog when needed
    if (showScheduleEditor) {
        ScheduleEditorDialog(
            schedule = schedule,
            courseTitle = courseTitle,
            onScheduleChange = { newSchedule -> schedule = newSchedule },
            onDismiss = { showScheduleEditor = false },
            viewModel = viewModel
        )
    }
}

@Composable
fun Modifier.borderDecorator(width: androidx.compose.ui.unit.Dp, color: androidx.compose.ui.graphics.Color, shape: androidx.compose.ui.graphics.Shape) =
    this.then(
        Modifier
            .padding(width)
            .background(color, shape)
            .padding(width)
    ) 

// Helper function to convert day code to full name
private fun getDayName(dayCode: String): String {
    return when (dayCode) {
        "M" -> "Monday"
        "TU" -> "Tuesday"
        "W" -> "Wednesday"
        "TH" -> "Thursday"
        "F" -> "Friday"
        "SA" -> "Saturday"
        "SU" -> "Sunday"
        else -> dayCode
    }
} 