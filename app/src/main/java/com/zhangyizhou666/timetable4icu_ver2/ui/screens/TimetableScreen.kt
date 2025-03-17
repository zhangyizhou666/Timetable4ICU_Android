package com.zhangyizhou666.timetable4icu_ver2.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    var selectedYear by remember { mutableStateOf("2024") }
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
        val csvData = CSVLoader.loadCSVFromAssets(context, "courses_data.csv")
        // Filter courses for 2024 Spring term
        viewModel.loadCoursesFromCSV("2024", "Spring", csvData)
        
        // Load timetable data if we have a saved ID
        if (viewModel.currentTimetableId.isNotEmpty()) {
            viewModel.loadTimetableData(viewModel.currentTimetableId)
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timetables",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Add new timetable button
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Timetable"
                            )
                        }
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
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = viewModel.currentTimetableId == timetable.id,
                                    onClick = {
                                        viewModel.loadTimetableData(timetable.id)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                                
                                Text(
                                    text = timetable.title,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (viewModel.currentTimetableId == timetable.id) 
                                        FontWeight.Bold else FontWeight.Normal
                                )
                                
                                // Only show delete if there's more than one timetable
                                if (viewModel.timetables.size > 1) {
                                    IconButton(onClick = {
                                        timetableToDelete = timetable.id
                                        showDeleteConfirmDialog = true
                                    }) {
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
                                    .padding(start = 40.dp, bottom = 8.dp),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            
                            Divider()
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = viewModel.currentTimetableTitle.ifEmpty { "Timetable" },
                            color = Silver
                        ) 
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
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                        modifier = Modifier
                            .width(40.dp)
                    ) {
                        val periods = if (viewModel.showEighthPeriod) {
                            listOf("1", "2", "3", "L", "4", "5", "6", "7", "8")
                        } else {
                            listOf("1", "2", "3", "L", "4", "5", "6", "7")
                        }
                        
                        periods.forEach { period ->
                            if (period == "L") {
                                // Lunch period
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp)
                                        .background(Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "L",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                // Regular period
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
                                            val timeDetail = viewModel.timeDetailWithEighth[period]
                                            Text(
                                                text = timeDetail?.first ?: "",
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
                                            val timeDetail = viewModel.timeDetailWithEighth[period]
                                            Text(
                                                text = timeDetail?.second ?: "",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Divider(color = SeparatorColor)
                        }
                    }
                    
                    // Timetable grid
                    val dayCount = if (viewModel.showSaturday) 6 else 5
                    val periodCount = if (viewModel.showEighthPeriod) 9 else 8
                    
                    for (dayIndex in 0 until dayCount) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .border(0.5.dp, SeparatorColor)
                        ) {
                            for (periodIndex in 0 until periodCount) {
                                val cellSize = viewModel.calculateCellSize(dayIndex, periodIndex)
                                
                                // Skip cells that are continuations of previous cells
                                if (cellSize == 0f) continue
                                
                                if (periodIndex == 3) {
                                    // Lunch period
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(30.dp)
                                            .background(
                                                if (viewModel.array[dayIndex][periodIndex].isNotEmpty()) {
                                                    getColorFromName(viewModel.arrayColor[dayIndex][periodIndex])
                                                } else {
                                                    Color.Gray
                                                }
                                            )
                                            .clickable {
                                                // Navigate to lunch edit screen
                                                navController.navigate("lunch/$dayIndex/$periodIndex")
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = viewModel.array[dayIndex][periodIndex],
                                            color = Color.Black,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    // Regular period
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(cellSize)
                                            .background(getColorFromName(viewModel.arrayColor[dayIndex][periodIndex]))
                                            .combinedClickable(
                                                onClick = {
                                                    // Navigate to cell edit screen
                                                    navController.navigate("edit/$dayIndex/$periodIndex")
                                                },
                                                onLongClick = {
                                                    // Only navigate to details if cell has content
                                                    if (viewModel.array[dayIndex][periodIndex].isNotEmpty()) {
                                                        navController.navigate("detail/$dayIndex/$periodIndex")
                                                    }
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (viewModel.shouldShowCellContent(dayIndex, periodIndex)) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(4.dp)
                                            ) {
                                                Text(
                                                    text = viewModel.array[dayIndex][periodIndex],
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                                
                                                if (viewModel.arrayRoom[dayIndex][periodIndex].isNotEmpty() && 
                                                    viewModel.arrayRoom[dayIndex][periodIndex] != "NO DATA") {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = viewModel.arrayRoom[dayIndex][periodIndex],
                                                        fontSize = 10.sp,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Only show divider if needed
                                if (viewModel.shouldShowDivider(dayIndex, periodIndex)) {
                                    Divider(color = SeparatorColor)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Create new timetable dialog
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New Timetable") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTimetableTitle,
                            onValueChange = { newTimetableTitle = it },
                            label = { Text("Timetable Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Term")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (term in listOf("Spring", "Autumn", "Winter")) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = selectedTerm == term,
                                        onClick = { selectedTerm = term }
                                    )
                                    Text(term)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Year")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (year in listOf("2024", "2023", "2022", "2021", "2020")) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = selectedYear == year,
                                        onClick = { selectedYear = year }
                                    )
                                    Text(year)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newTimetableTitle.isNotEmpty()) {
                                viewModel.createNewTimetable(newTimetableTitle, selectedYear, selectedTerm)
                                newTimetableTitle = ""
                                showCreateDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCreateDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Delete confirmation dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Delete Timetable") },
                text = { Text("Are you sure you want to delete this timetable? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTimetable(timetableToDelete)
                            showDeleteConfirmDialog = false
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}