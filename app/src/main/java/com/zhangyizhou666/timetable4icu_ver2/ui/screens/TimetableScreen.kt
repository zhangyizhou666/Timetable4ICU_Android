package com.zhangyizhou666.timetable4icu_ver2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zhangyizhou666.timetable4icu_ver2.data.CSVLoader
import com.zhangyizhou666.timetable4icu_ver2.data.TimetableViewModel
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.DarkBlue
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.SeparatorColor
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.Silver
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.getColorFromName
import com.zhangyizhou666.timetable4icu_ver2.ui.theme.combinedClickable
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    navController: NavController,
    viewModel: TimetableViewModel
) {
    val context = LocalContext.current
    val courseData = viewModel.coursesData.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Dialog states
    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var newTimetableTitle by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("2025") }
    var selectedTerm by remember { mutableStateOf("Spring") }
    var timetableToDelete by remember { mutableStateOf("") }
    
    // Load saved preferences
    LaunchedEffect(Unit) {
        // In a real app, we would load from DataStore here
        // For now, we'll use default values
        viewModel.showSaturday = false
        viewModel.showEighthPeriod = false
        viewModel.showTimeDetails = true
        
        // Load CSV data
        val csvData = CSVLoader.loadCSVFromAssets(context, "courses_2025_all_terms.csv")
        // Filter courses for the selected year and term (default: 2025 Spring)
        viewModel.loadCoursesFromCSV("2025", "Spring", csvData)
        
        // Load timetable data if we have a saved ID
        if (viewModel.currentTimetableId.isNotEmpty()) {
            viewModel.loadTimetableData(viewModel.currentTimetableId)
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                viewModel = viewModel,
                drawerState = drawerState,
                scope = scope,
                context = context,
                onCreateTimetable = {
                    newTimetableTitle = ""
                    selectedYear = "2025"
                    selectedTerm = "Spring"
                    showCreateDialog = true
                },
                onDeleteTimetable = { id ->
                    timetableToDelete = id
                    showDeleteConfirmDialog = true
                }
            )
        }
    ) { 
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = viewModel.currentTimetableTitle.ifEmpty { "Timetable" },
                                color = Silver
                            )
                            val credits by viewModel.selectedCourseCredits.collectAsState()
                            Text(
                                text = "Credits: $credits",
                                color = Silver,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkBlue
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Silver
                            )
                        }
                    },
                    actions = {
                        // Debug button
                        IconButton(onClick = { 
                            // Debug action to print course data
                            val courseDataStr = viewModel.printCourseData()
                            Toast.makeText(context, "Loaded ${courseData.value.size} courses", Toast.LENGTH_SHORT).show()
                            
                            // Log to console for easier debugging
                            println("======= CSV COURSE DATA =======")
                            println(courseDataStr)
                            println("===============================")
                        }) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Debug",
                                tint = Silver
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                // Main content
                TimetableContent(navController, viewModel)
                
                // Dialogs
                if (showCreateDialog) {
                    CreateTimetableDialog(
                        newTimetableTitle = newTimetableTitle,
                        onTitleChange = { newTimetableTitle = it },
                        selectedYear = selectedYear,
                        onYearChange = { selectedYear = it },
                        selectedTerm = selectedTerm,
                        onTermChange = { selectedTerm = it },
                        onDismiss = { showCreateDialog = false },
                        onConfirm = {
                            if (newTimetableTitle.isNotEmpty()) {
                                // Create a new timetable
                                viewModel.createNewTimetable(newTimetableTitle, selectedYear, selectedTerm)
                                
                                // Load courses for the selected year and term
                                val csvData = CSVLoader.loadCSVFromAssets(context, "courses_2025_all_terms.csv")
                                viewModel.loadCoursesFromCSV(selectedYear, selectedTerm, csvData)
                                
                                showCreateDialog = false
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter a title for the timetable",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
                
                if (showDeleteConfirmDialog) {
                    DeleteTimetableDialog(
                        onDismiss = { showDeleteConfirmDialog = false },
                        onConfirm = {
                            viewModel.deleteTimetable(timetableToDelete)
                            showDeleteConfirmDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimetableContent(
    navController: NavController,
    viewModel: TimetableViewModel
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Day headers
        val days = if (viewModel.showSaturday) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        } else {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri")
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            // Time column spacer
            Spacer(modifier = Modifier.width(40.dp))
            
            // Day headers
            days.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Divider(color = SeparatorColor)
        
        // Time slots and timetable grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Time column
            Column(
                modifier = Modifier.width(40.dp)
            ) {
                val periods = if (viewModel.showEighthPeriod) {
                    listOf("1", "2", "3", "L", "4", "5", "6", "7", "8")
                } else {
                    listOf("1", "2", "3", "L", "4", "5", "6", "7")
                }
                
                periods.forEach { period ->
                    // Regular period display for all periods including lunch
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (viewModel.showTimeDetails) {
                                val periodNum = when (period) {
                                    "L" -> 3
                                    "4" -> 4
                                    "5" -> 5
                                    "6" -> 6
                                    "7" -> 7
                                    "8" -> 8
                                    else -> period.toIntOrNull()?.minus(1) ?: 0
                                }
                                val timeDetail = viewModel.getTimeDetails(0, periodNum) // Using Monday as reference
                                Text(
                                    text = timeDetail.first,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            Text(
                                text = period,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (viewModel.showTimeDetails) {
                                val periodNum = when (period) {
                                    "L" -> 3
                                    "4" -> 4
                                    "5" -> 5
                                    "6" -> 6
                                    "7" -> 7
                                    "8" -> 8
                                    else -> period.toIntOrNull()?.minus(1) ?: 0
                                }
                                val timeDetail = viewModel.getTimeDetails(0, periodNum) // Using Monday as reference
                                Text(
                                    text = timeDetail.second,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    Divider(color = SeparatorColor)
                }
            }
            
            // Timetable grid
            val dayCount = if (viewModel.showSaturday) 6 else 5
            val periodCount = if (viewModel.showEighthPeriod) 9 else 8
            
            // Create a container for the timetable grid with overlay
            Box(modifier = Modifier.weight(1f)) {
                // Background grid (days and periods)
                Row(modifier = Modifier.fillMaxSize()) {
                    for (dayIndex in 0 until dayCount) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .border(0.5.dp, SeparatorColor)
                        ) {
                            for (periodIndex in 0 until periodCount) {
                                val cellSize = 1f  // Keep all cells equal size
                                
                                // Regular period (including lunch)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(cellSize)
                                        .background(Color.Transparent)
                                        .clickable {
                                            // Navigate to appropriate edit screen
                                            if (periodIndex == 3) {
                                                // Navigate to lunch edit screen
                                                navController.navigate("lunch/$dayIndex/$periodIndex")
                                            } else {
                                                // Navigate to cell edit screen
                                                navController.navigate("edit/$dayIndex/$periodIndex")
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {}
                                
                                // Only show divider if needed
                                if (periodIndex < periodCount - 1) {
                                    Divider(color = SeparatorColor)
                                }
                            }
                        }
                    }
                }
                
                // Course overlay based on time
                Row(modifier = Modifier.fillMaxSize()) {
                    for (dayIndex in 0 until dayCount) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            for (periodIndex in 0 until periodCount) {
                                if (viewModel.shouldShowCellContent(dayIndex, periodIndex)) {
                                    val courseTitle = viewModel.array[dayIndex][periodIndex]
                                    val schedule = viewModel.arraySchedule[dayIndex][periodIndex]
                                    val room = viewModel.arrayRoom[dayIndex][periodIndex]
                                    val colorName = viewModel.arrayColor[dayIndex][periodIndex]
                                    
                                    // Get time details for this period (now using our enhanced getTimeDetails)
                                    val (startTime, endTime) = viewModel.getTimeDetails(dayIndex, periodIndex)
                                    
                                    // Course box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 1.dp)
                                            .height(calculateCellHeight(periodIndex).dp)
                                            .offset(y = calculateTopOffset(periodIndex).dp)
                                            .background(
                                                getColorFromName(colorName).copy(alpha = 0.9f),
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = getColorFromName(colorName).copy(alpha = 0.7f),
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                                            )
                                            .padding(4.dp)
                                            .combinedClickable(
                                                onClick = {
                                                    if (periodIndex == 3) {
                                                        navController.navigate("lunch/$dayIndex/$periodIndex")
                                                    } else {
                                                        navController.navigate("edit/$dayIndex/$periodIndex")
                                                    }
                                                },
                                                onLongClick = {
                                                    Toast.makeText(context, "$startTime - $endTime", Toast.LENGTH_LONG).show()
                                                    navController.navigate("detail/$dayIndex/$periodIndex")
                                                }
                                            ),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        Column {
                                            Text(
                                                text = courseTitle,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Start,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            
                                            if (room.isNotEmpty() && room != "NO DATA") {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = room,
                                                    fontSize = 10.sp,
                                                    textAlign = TextAlign.Start,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$startTime-$endTime",
                                                fontSize = 10.sp,
                                                color = Color.DarkGray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to convert time string (HH:MM) to minutes since midnight
private fun timeToMinutes(time: String): Int {
    val parts = time.split(":")
    if (parts.size != 2) return 0
    return parts[0].toIntOrNull()?.times(60)?.plus(parts[1].toIntOrNull() ?: 0) ?: 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(
    viewModel: TimetableViewModel,
    drawerState: androidx.compose.material3.DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
    onCreateTimetable: () -> Unit,
    onDeleteTimetable: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp)
    ) {
        // Drawer header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(DarkBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Timetable Settings",
                color = Silver,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Divider()
        
        // Display options section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Display Options",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show Saturday option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show Saturday",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = viewModel.showSaturday,
                    onCheckedChange = { viewModel.showSaturday = it }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show 8th period option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show 8th Period",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = viewModel.showEighthPeriod,
                    onCheckedChange = { viewModel.showEighthPeriod = it }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show time details option
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Show Time Details",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = viewModel.showTimeDetails,
                    onCheckedChange = { viewModel.showTimeDetails = it }
                )
            }
        }
        
        Divider()
        
        // Timetables section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Timetables",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onCreateTimetable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Timetable")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // List of timetables
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(viewModel.timetables) { timetable ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.loadTimetableData(timetable.id)
                                
                                // Load courses for this timetable's year and term
                                val csvData = CSVLoader.loadCSVFromAssets(context, "courses_2025_all_terms.csv")
                                viewModel.loadCoursesFromCSV(timetable.year, timetable.term, csvData)
                                
                                // Close the drawer
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = viewModel.currentTimetableId == timetable.id,
                            onClick = {
                                viewModel.loadTimetableData(timetable.id)
                                
                                // Load courses for this timetable's year and term
                                val csvData = CSVLoader.loadCSVFromAssets(context, "courses_2025_all_terms.csv")
                                viewModel.loadCoursesFromCSV(timetable.year, timetable.term, csvData)
                                
                                scope.launch { drawerState.close() }
                            }
                        )
                        
                        Text(
                            text = timetable.title,
                            modifier = Modifier.weight(1f),
                            fontWeight = if (viewModel.currentTimetableId == timetable.id) 
                                FontWeight.Bold else FontWeight.Normal
                        )
                        
                        if (viewModel.timetables.size > 1) {
                            IconButton(onClick = { onDeleteTimetable(timetable.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Timetable"
                                )
                            }
                        }
                    }
                    
                    // Add term and year info
                    Text(
                        text = "${timetable.term}, ${timetable.year}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 56.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun CreateTimetableDialog(
    newTimetableTitle: String,
    onTitleChange: (String) -> Unit,
    selectedYear: String,
    onYearChange: (String) -> Unit,
    selectedTerm: String,
    onTermChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Timetable") },
        text = {
            Column {
                OutlinedTextField(
                    value = newTimetableTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Timetable Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Year:", fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedYear == "2025",
                        onClick = { onYearChange("2025") }
                    )
                    Text("2025")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Term:", fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedTerm == "Winter",
                        onClick = { onTermChange("Winter") }
                    )
                    Text("Winter")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedTerm == "Spring",
                        onClick = { onTermChange("Spring") }
                    )
                    Text("Spring")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedTerm == "Summer",
                        onClick = { onTermChange("Summer") }
                    )
                    Text("Summer")
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedTerm == "Autumn",
                        onClick = { onTermChange("Autumn") }
                    )
                    Text("Autumn")
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeleteTimetableDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Timetable") },
        text = { Text("Are you sure you want to delete this timetable? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimeBasedCourseDisplay(
    viewModel: TimetableViewModel,
    dayIndex: Int,
    periodIndex: Int,
    courseTitle: String,
    schedule: String,
    room: String,
    colorName: String,
    modifier: Modifier = Modifier
) {
    // 删除整个函数体
}

// Calculate cell height based on period index
private fun calculateCellHeight(periodIndex: Int): Float {
    return when (periodIndex) {
        3 -> 70f  // Lunch period (70 minutes)
        else -> 75f // Regular period (75 minutes)
    }
}

// Calculate top offset based on period index
private fun calculateTopOffset(periodIndex: Int): Float {
    var offset = 0f
    for (i in 0 until periodIndex) {
        offset += calculateCellHeight(i)
        // Add 10dp for break between periods
        // Add break AFTER periods 1, 2, 4, 5, 6 (indices 0, 1, 4, 5, 6)
        if (i == 0 || i == 1 || i == 4 || i == 5 || i == 6) {
            offset += 10f
        }
        // Special case: Break after period 3 (index 2) but before lunch
        if (i == 2) {
            offset += 10f
        }
        // Special case: Break after lunch (index 3) but before period 4
        if (i == 3) {
            offset += 10f
        }
    }
    return offset
}