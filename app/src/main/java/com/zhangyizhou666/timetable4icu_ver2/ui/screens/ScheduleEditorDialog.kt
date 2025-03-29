package com.zhangyizhou666.timetable4icu_ver2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zhangyizhou666.timetable4icu_ver2.data.TimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditorDialog(
    schedule: String,
    courseTitle: String,
    onScheduleChange: (String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: TimetableViewModel
) {
    var currentSchedule by remember { mutableStateOf(schedule) }
    var selectedDay by remember { mutableStateOf("M") }
    var selectedTime by remember { mutableStateOf("1") }
    var showConflictAlert by remember { mutableStateOf(false) }
    var showMissingAlert by remember { mutableStateOf(false) }
    var showOverlapWarning by remember { mutableStateOf(false) }
    var overlapCourseNames by remember { mutableStateOf("") }
    
    val days = listOf("M", "TU", "W", "TH", "F", "SA")
    val times = listOf("1", "2", "3", "4", "5", "6", "7", "8")
    
    var dayExpanded by remember { mutableStateOf(false) }
    var timeExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Edit Schedule",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Current Schedule: ${currentSchedule.ifEmpty { "None" }}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time Dropdown
                ExposedDropdownMenuBox(
                    expanded = timeExpanded,
                    onExpandedChange = { timeExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = selectedTime,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Period") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = timeExpanded,
                        onDismissRequest = { timeExpanded = false }
                    ) {
                        times.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time) },
                                onClick = {
                                    selectedTime = time
                                    timeExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Text(
                    text = "/",
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                // Day Dropdown
                ExposedDropdownMenuBox(
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    TextField(
                        value = selectedDay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        days.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = {
                                    selectedDay = day
                                    dayExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        val scheduleToAdd = "$selectedTime/$selectedDay"
                        val normalizedSchedule = viewModel.cleanScheduleString(scheduleToAdd)
                        
                        // Check if this schedule already exists
                        if (currentSchedule.split(",").any { 
                            viewModel.cleanScheduleString(it.trim()) == normalizedSchedule 
                        }) {
                            showConflictAlert = true
                        } else {
                            // Check for overlaps with other courses
                            val result = viewModel.checkCourseOverlap(courseTitle, scheduleToAdd, "", "")
                            val hasOverlap = result.first
                            val overlapCourses = result.second
                            
                            // Even if there's an overlap, we'll handle it at the EditCellScreen level
                            // This just shows a warning that there might be conflicts
                            if (hasOverlap) {
                                showOverlapWarning = true
                                overlapCourseNames = overlapCourses
                            }
                            
                            // Add to schedule
                            currentSchedule = if (currentSchedule.isEmpty()) {
                                scheduleToAdd
                            } else {
                                "$currentSchedule,$scheduleToAdd"
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
                
                OutlinedButton(
                    onClick = {
                        val scheduleToRemove = "$selectedTime/$selectedDay"
                        val normalizedSchedule = viewModel.cleanScheduleString(scheduleToRemove)
                        
                        // Check if this schedule exists in the current schedule
                        val scheduleParts = currentSchedule.split(",")
                        val exists = scheduleParts.any { 
                            viewModel.cleanScheduleString(it.trim()) == normalizedSchedule 
                        }
                        
                        if (exists) {
                            // Remove from schedule
                            val newSchedule = scheduleParts
                                .filterNot { viewModel.cleanScheduleString(it.trim()) == normalizedSchedule }
                                .joinToString(",")
                            
                            currentSchedule = newSchedule
                        } else {
                            showMissingAlert = true
                        }
                    }
                ) {
                    Text("Remove")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        onScheduleChange(currentSchedule)
                        onDismiss()
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
    
    if (showConflictAlert) {
        AlertDialog(
            onDismissRequest = { showConflictAlert = false },
            title = { Text("Schedule Conflict") },
            text = { Text("This time slot is already in the schedule.") },
            confirmButton = {
                Button(onClick = { showConflictAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    if (showMissingAlert) {
        AlertDialog(
            onDismissRequest = { showMissingAlert = false },
            title = { Text("Not Found") },
            text = { Text("This time slot is not in the current schedule.") },
            confirmButton = {
                Button(onClick = { showMissingAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
    
    if (showOverlapWarning) {
        AlertDialog(
            onDismissRequest = { showOverlapWarning = false },
            title = { Text("Potential Conflict") },
            text = { Text("This schedule may conflict with: $overlapCourseNames\n\nConflicts will be resolved when you save.") },
            confirmButton = {
                Button(onClick = { showOverlapWarning = false }) {
                    Text("OK")
                }
            }
        )
    }
} 