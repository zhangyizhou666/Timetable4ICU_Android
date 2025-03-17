package com.zhangyizhou666.timetable4icu_ver2.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhangyizhou666.timetable4icu_ver2.data.model.CellModel
import com.zhangyizhou666.timetable4icu_ver2.data.model.ColorModel
import com.zhangyizhou666.timetable4icu_ver2.data.model.CoursenoModel
import com.zhangyizhou666.timetable4icu_ver2.data.model.CourseTitleModel
import com.zhangyizhou666.timetable4icu_ver2.data.model.InstructorModel
import com.zhangyizhou666.timetable4icu_ver2.data.model.ModeModel
import com.zhangyizhou666.timetable4icu_ver2.data.model.RealmManager
import com.zhangyizhou666.timetable4icu_ver2.data.model.RoomModel
import com.zhangyizhou666.timetable4icu_ver2.data.model.ScheduleModel
import com.zhangyizhou666.timetable4icu_ver2.data.model.Task
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// Add Timetable data class
data class Timetable(
    val id: String,
    val title: String,
    val year: String,
    val term: String
)

// Class to store all timetable data
data class TimetableData(
    val array: Array<Array<String>> = Array(6) { Array(9) { "" } },
    val arrayInstructor: Array<Array<String>> = Array(6) { Array(9) { "" } },
    val arraySchedule: Array<Array<String>> = Array(6) { Array(9) { "" } },
    val arrayCourseno: Array<Array<String>> = Array(6) { Array(9) { "" } },
    val arrayRoom: Array<Array<String>> = Array(6) { Array(9) { "" } },
    val arrayMode: Array<Array<String>> = Array(6) { Array(9) { "" } },
    val arrayColor: Array<Array<String>> = Array(6) { Array(9) { "white" } }
) {
    // Need to override equals and hashCode because of arrays
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TimetableData

        if (!array.contentDeepEquals(other.array)) return false
        if (!arrayInstructor.contentDeepEquals(other.arrayInstructor)) return false
        if (!arraySchedule.contentDeepEquals(other.arraySchedule)) return false
        if (!arrayCourseno.contentDeepEquals(other.arrayCourseno)) return false
        if (!arrayRoom.contentDeepEquals(other.arrayRoom)) return false
        if (!arrayMode.contentDeepEquals(other.arrayMode)) return false
        if (!arrayColor.contentDeepEquals(other.arrayColor)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = array.contentDeepHashCode()
        result = 31 * result + arrayInstructor.contentDeepHashCode()
        result = 31 * result + arraySchedule.contentDeepHashCode()
        result = 31 * result + arrayCourseno.contentDeepHashCode()
        result = 31 * result + arrayRoom.contentDeepHashCode()
        result = 31 * result + arrayMode.contentDeepHashCode()
        result = 31 * result + arrayColor.contentDeepHashCode()
        return result
    }
}

// In-memory task data class
data class TaskData(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val details: String = "",
    val isDone: Boolean = false,
    val dueDate: Long = 0,
    val courseTitle: String = ""
)

class TimetableViewModel : ViewModel() {
    
    // Current cell arrays for the active timetable
    var array: Array<Array<String>> = Array(6) { Array(9) { "" } }
    var arrayInstructor: Array<Array<String>> = Array(6) { Array(9) { "" } }
    var arraySchedule: Array<Array<String>> = Array(6) { Array(9) { "" } }
    var arrayCourseno: Array<Array<String>> = Array(6) { Array(9) { "" } }
    var arrayRoom: Array<Array<String>> = Array(6) { Array(9) { "" } }
    var arrayMode: Array<Array<String>> = Array(6) { Array(9) { "" } }
    var arrayColor: Array<Array<String>> = Array(6) { Array(9) { "white" } }
    
    // Storage for all timetable data
    private val timetableDatas = mutableMapOf<String, TimetableData>()
    
    // Reference to SharedPreferences for persistence
    private var sharedPreferences: SharedPreferences? = null
    private val gson = Gson()
    
    // Display settings
    var showSaturday by mutableStateOf(false)
    var showEighthPeriod by mutableStateOf(false)
    var showTimeDetails by mutableStateOf(true)
    
    // State for the current timetable
    private val _currentTimetableId = mutableStateOf("")
    val currentTimetableId: String get() = _currentTimetableId.value
    
    // State for the current timetable title
    private val _currentTimetableTitle = mutableStateOf("")
    val currentTimetableTitle: String get() = _currentTimetableTitle.value
    
    // State for courses data
    private val _coursesData = MutableStateFlow<List<CourseData>>(emptyList())
    val coursesData: StateFlow<List<CourseData>> = _coursesData.asStateFlow()
    
    // Cell name mapping for Realm
    private val cellNameForRealm = arrayOf(
        arrayOf("M1", "M2", "M3", "ML", "M4", "M5", "M6", "M7", "M8"), 
        arrayOf("TU1", "TU2", "TU3", "TUL", "TU4", "TU5", "TU6", "TU7", "TU8"), 
        arrayOf("W1", "W2", "W3", "WL", "W4", "W5", "W6", "W7", "W8"), 
        arrayOf("TH1", "TH2", "TH3", "THL", "TH4", "TH5", "TH6", "TH7", "TH8"), 
        arrayOf("F1", "F2", "F3", "FL", "F4", "F5", "F6", "F7", "F8"), 
        arrayOf("SA1", "SA2", "SA3", "SAL", "SA4", "SA5", "SA6", "SA7", "SA8")
    )
    
    // Time details mapping
    val timeDetailWithEighth = mapOf(
        "1" to Pair("8:50", "10:00"),
        "2" to Pair("10:10", "11:20"),
        "3" to Pair("11:30", "12:40"),
        "4" to Pair("13:50", "15:00"),
        "5" to Pair("15:10", "16:20"),
        "6" to Pair("16:30", "17:40"),
        "7" to Pair("17:50", "19:00"),
        "8" to Pair("19:10", "20:20")
    )
    
    // Initialize Realm
    private val realm: Realm by lazy { RealmManager.getRealmInstance() }
    
    // Data class for course information
    data class CourseData(
        val courseTitle: String,
        val instructor: String,
        val schedule: String,
        val courseno: String,
        val room: String,
        val mode: String,
        val color: String = "white"
    )
    
    // List of timetables
    private val _timetables = mutableStateListOf<Timetable>()
    val timetables: List<Timetable> = _timetables
    
    // Tasks list - in memory storage
    private val _tasks = mutableStateListOf<TaskData>()
    val tasks: List<TaskData> = _tasks
    
    init {
        // Add a default timetable if none exists
        if (_timetables.isEmpty()) {
            _timetables.add(Timetable(
                id = UUID.randomUUID().toString(),
                title = "Default Timetable",
                year = "2024",
                term = "Spring"
            ))
            _currentTimetableId.value = _timetables[0].id
            _currentTimetableTitle.value = _timetables[0].title
        }
    }
    
    // Initialize with context for SharedPreferences
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("timetable_prefs", Context.MODE_PRIVATE)
        
        // Load saved display settings
        showSaturday = sharedPreferences?.getBoolean("show_saturday", false) ?: false
        showEighthPeriod = sharedPreferences?.getBoolean("show_eighth_period", false) ?: false
        showTimeDetails = sharedPreferences?.getBoolean("show_time_details", true) ?: true
        
        // Load saved timetables
        val timetablesJson = sharedPreferences?.getString("timetables", null)
        if (timetablesJson != null) {
            val type = object : TypeToken<List<Timetable>>() {}.type
            val savedTimetables: List<Timetable> = gson.fromJson(timetablesJson, type)
            _timetables.clear()
            _timetables.addAll(savedTimetables)
        }
        
        // Load saved current timetable
        val savedCurrentId = sharedPreferences?.getString("current_timetable_id", "")
        if (savedCurrentId != null && savedCurrentId.isNotEmpty() && _timetables.any { it.id == savedCurrentId }) {
            _currentTimetableId.value = savedCurrentId
            _currentTimetableTitle.value = _timetables.first { it.id == savedCurrentId }.title
        } else if (_timetables.isNotEmpty()) {
            _currentTimetableId.value = _timetables[0].id
            _currentTimetableTitle.value = _timetables[0].title
        }
        
        // Load timetable data for all timetables
        for (timetable in _timetables) {
            val dataJson = sharedPreferences?.getString("timetable_data_${timetable.id}", null)
            if (dataJson != null) {
                try {
                    val type = object : TypeToken<Map<String, Array<Array<String>>>>() {}.type
                    val dataMap: Map<String, Array<Array<String>>> = gson.fromJson(dataJson, type)
                    
                    val data = TimetableData(
                        array = dataMap["array"] ?: Array(6) { Array(9) { "" } },
                        arrayInstructor = dataMap["arrayInstructor"] ?: Array(6) { Array(9) { "" } },
                        arraySchedule = dataMap["arraySchedule"] ?: Array(6) { Array(9) { "" } },
                        arrayCourseno = dataMap["arrayCourseno"] ?: Array(6) { Array(9) { "" } },
                        arrayRoom = dataMap["arrayRoom"] ?: Array(6) { Array(9) { "" } },
                        arrayMode = dataMap["arrayMode"] ?: Array(6) { Array(9) { "" } },
                        arrayColor = dataMap["arrayColor"] ?: Array(6) { Array(9) { "white" } }
                    )
                    
                    timetableDatas[timetable.id] = data
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        // Load tasks
        val tasksJson = sharedPreferences?.getString("tasks", null)
        if (tasksJson != null) {
            try {
                val type = object : TypeToken<List<TaskData>>() {}.type
                val savedTasks: List<TaskData> = gson.fromJson(tasksJson, type)
                _tasks.clear()
                _tasks.addAll(savedTasks)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Load the current timetable data
        loadTimetableData(currentTimetableId)
    }
    
    // Save display settings
    private fun saveDisplaySettings() {
        sharedPreferences?.edit()?.apply {
            putBoolean("show_saturday", showSaturday)
            putBoolean("show_eighth_period", showEighthPeriod)
            putBoolean("show_time_details", showTimeDetails)
            apply()
        }
    }
    
    // Save timetables list
    private fun saveTimetablesList() {
        sharedPreferences?.edit()?.apply {
            putString("timetables", gson.toJson(_timetables))
            putString("current_timetable_id", currentTimetableId)
            apply()
        }
    }
    
    // Save current timetable data
    private fun saveCurrentTimetableData() {
        if (currentTimetableId.isEmpty()) return
        
        // Create a data object from current arrays
        val currentData = TimetableData(
            array = array.map { it.clone() }.toTypedArray(),
            arrayInstructor = arrayInstructor.map { it.clone() }.toTypedArray(),
            arraySchedule = arraySchedule.map { it.clone() }.toTypedArray(),
            arrayCourseno = arrayCourseno.map { it.clone() }.toTypedArray(),
            arrayRoom = arrayRoom.map { it.clone() }.toTypedArray(),
            arrayMode = arrayMode.map { it.clone() }.toTypedArray(),
            arrayColor = arrayColor.map { it.clone() }.toTypedArray()
        )
        
        // Store it in our map
        timetableDatas[currentTimetableId] = currentData
        
        // Create a map for JSON serialization
        val dataMap = mapOf(
            "array" to currentData.array,
            "arrayInstructor" to currentData.arrayInstructor,
            "arraySchedule" to currentData.arraySchedule,
            "arrayCourseno" to currentData.arrayCourseno,
            "arrayRoom" to currentData.arrayRoom,
            "arrayMode" to currentData.arrayMode,
            "arrayColor" to currentData.arrayColor
        )
        
        // Save to SharedPreferences
        sharedPreferences?.edit()?.apply {
            putString("timetable_data_$currentTimetableId", gson.toJson(dataMap))
            apply()
        }
    }
    
    // Load data from CSV file
    fun loadCoursesFromCSV(year: String, term: String, csvData: String) {
        val coursesList = mutableListOf<CourseData>()
        
        // Skip header line and parse CSV data
        csvData.trim().lines().drop(1).forEach { line ->
            try {
                val fields = line.split(";")
                if (fields.size >= 9) {
                    // Normalize year and term to handle potential whitespace or formatting issues
                    val csvYear = fields[7].trim()
                    val csvTerm = fields[8].trim()
                    
                    if (csvYear == year && csvTerm.equals(term, ignoreCase = true)) {
                        var lang = ""
                        if (fields.size > 6 && fields[6] != "NO DATA") {
                            lang = "(${fields[6]})"
                        }
                        
                        val title = fields[0].trim()
                        val instructor = fields[1].trim()
                        val schedule = fields[2].trim()
                        val courseno = fields[3].trim()
                        val room = fields[4].trim()
                        val mode = fields[5].trim()
                        
                        // Skip entries with no schedule information
                        if (schedule != "NO DATA") {
                            val courseData = CourseData(
                                courseTitle = title + if (lang.isNotEmpty()) " $lang" else "",
                                instructor = instructor,
                                schedule = schedule,
                                courseno = courseno,
                                room = room,
                                mode = mode,
                                color = "white"
                            )
                            coursesList.add(courseData)
                        }
                    }
                }
            } catch (e: Exception) {
                // Skip malformed entries
                e.printStackTrace()
            }
        }
        
        _coursesData.update { coursesList }
    }
    
    // Create a new timetable
    fun createNewTimetable(title: String, year: String, term: String) {
        // Save current timetable data first
        saveCurrentTimetableData()
        
        val newId = UUID.randomUUID().toString()
        val newTimetable = Timetable(
            id = newId,
            title = title,
            year = year,
            term = term
        )
        _timetables.add(newTimetable)
        
        // Set the new timetable as the current one and clear its data
        _currentTimetableId.value = newId
        _currentTimetableTitle.value = title
        
        // Initialize empty data for the new timetable
        clearArrays()
        
        // Save the updated timetables list
        saveTimetablesList()
    }
    
    // Load timetable data
    fun loadTimetableData(id: String) {
        // First save the current timetable data
        saveCurrentTimetableData()
        
        // Find the timetable
        val timetable = _timetables.find { it.id == id } ?: return
        
        // Set as current timetable
        _currentTimetableId.value = id
        _currentTimetableTitle.value = timetable.title
        
        // Load data for this timetable if it exists
        val data = timetableDatas[id]
        if (data != null) {
            // Copy data to our working arrays
            copyTimetableDataToArrays(data)
        } else {
            // No data yet, initialize empty arrays
            clearArrays()
        }
        
        // Save current timetable ID
        saveTimetablesList()
    }
    
    // Delete a timetable
    fun deleteTimetable(id: String) {
        // Don't delete if it's the only timetable
        if (_timetables.size <= 1) return
        
        // Remove from timetable datas map
        timetableDatas.remove(id)
        
        // Remove from SharedPreferences
        sharedPreferences?.edit()?.apply {
            remove("timetable_data_$id")
            apply()
        }
        
        // Find the timetable to remove
        val index = _timetables.indexOfFirst { it.id == id }
        if (index != -1) {
            _timetables.removeAt(index)
            
            // If we deleted the current timetable, load the first available one
            if (_currentTimetableId.value == id) {
                loadTimetableData(_timetables[0].id)
            }
        }
        
        // Save the updated timetables list
        saveTimetablesList()
    }
    
    // Copy data from TimetableData to working arrays
    private fun copyTimetableDataToArrays(data: TimetableData) {
        for (i in 0 until 6) {
            for (j in 0 until 9) {
                array[i][j] = data.array[i][j]
                arrayInstructor[i][j] = data.arrayInstructor[i][j]
                arraySchedule[i][j] = data.arraySchedule[i][j]
                arrayCourseno[i][j] = data.arrayCourseno[i][j] 
                arrayRoom[i][j] = data.arrayRoom[i][j]
                arrayMode[i][j] = data.arrayMode[i][j]
                arrayColor[i][j] = data.arrayColor[i][j]
            }
        }
    }
    
    // Clear all arrays
    private fun clearArrays() {
        for (i in 0 until 6) {
            for (j in 0 until 9) {
                array[i][j] = ""
                arrayInstructor[i][j] = ""
                arraySchedule[i][j] = ""
                arrayCourseno[i][j] = ""
                arrayRoom[i][j] = ""
                arrayMode[i][j] = ""
                arrayColor[i][j] = "white"
            }
        }
    }
    
    // Get all timetables
    fun getAllTimetables(): List<Pair<String, String>> {
        // Convert our in-memory timetables to the expected format
        return _timetables.map { Pair(it.id, it.title) }
    }
    
    // Update display settings
    fun updateDisplaySettings(
        showSaturday: Boolean? = null,
        showEighthPeriod: Boolean? = null,
        showTimeDetails: Boolean? = null
    ) {
        showSaturday?.let { this.showSaturday = it }
        showEighthPeriod?.let { this.showEighthPeriod = it }
        showTimeDetails?.let { this.showTimeDetails = it }
        
        // Save settings
        saveDisplaySettings()
    }
    
    // Calculate cell size based on content
    fun calculateCellSize(dayIndex: Int, periodIndex: Int): Float {
        if (array[dayIndex][periodIndex].isEmpty()) {
            return 1f
        }
        
        // Skip cells that are continuations of previous cells
        if (periodIndex > 0 && array[dayIndex][periodIndex] == array[dayIndex][periodIndex-1]) {
            return 0f
        }
        
        // Calculate how many periods this cell spans
        var cellSize = 1f
        var nextIndex = periodIndex + 1
        
        while (nextIndex < 9 && array[dayIndex][nextIndex] == array[dayIndex][periodIndex]) {
            cellSize += 1f
            nextIndex++
        }
        
        return cellSize
    }
    
    // Determine if a cell's content should be shown
    fun shouldShowCellContent(dayIndex: Int, periodIndex: Int): Boolean {
        if (array[dayIndex][periodIndex].isEmpty()) {
            return false
        }
        
        // If this is a continuation cell, don't show content
        if (periodIndex > 0 && array[dayIndex][periodIndex] == array[dayIndex][periodIndex-1]) {
            return false
        }
        
        return true
    }
    
    // Determine if a divider should be shown after a cell
    fun shouldShowDivider(dayIndex: Int, periodIndex: Int): Boolean {
        if (periodIndex == 8) {
            return true // Always show divider at bottom
        }
        
        // Don't show divider if the next cell is a continuation
        if (array[dayIndex][periodIndex].isNotEmpty() && 
            array[dayIndex][periodIndex] == array[dayIndex][periodIndex+1]) {
            return false
        }
        
        return true
    }
    
    // Process course schedule string to clean it up
    fun cleanScheduleString(schedule: String): String {
        return schedule.replace(Regex("[*()]"), "")
    }
    
    // Update all cells for a course based on its schedule
    fun updateMultipleCells(
        dayIndex: Int, 
        periodIndex: Int, 
        courseTitle: String,
        instructor: String,
        schedule: String,
        courseno: String,
        room: String,
        mode: String,
        colorName: String
    ) {
        // First update the primary cell
        array[dayIndex][periodIndex] = courseTitle
        arrayInstructor[dayIndex][periodIndex] = instructor
        arraySchedule[dayIndex][periodIndex] = schedule
        arrayCourseno[dayIndex][periodIndex] = courseno
        arrayRoom[dayIndex][periodIndex] = room
        arrayMode[dayIndex][periodIndex] = mode
        arrayColor[dayIndex][periodIndex] = colorName
        
        // First, clear any cells from previous schedule that are no longer in the new schedule
        if (schedule.isNotEmpty()) {
            // Get all cells that had this course before but might not be part of the schedule anymore
            clearCellsNotInSchedule(courseTitle, schedule)
        }
        
        // Now update all cells specified in the schedule
        if (schedule.isNotEmpty()) {
            val scheduleParts = schedule.split(",")
            
            scheduleParts.forEach { part ->
                val cleanPart = cleanScheduleString(part.trim())
                val parts = cleanPart.split("/")
                
                if (parts.size == 2) {
                    val periodStr = parts[0]
                    val dayStr = parts[1]
                    
                    var periodNum = periodStr.toIntOrNull() ?: return@forEach
                    // Adjust period index for lunch
                    if (periodNum < 4) {
                        periodNum -= 1
                    }
                    
                    val cellDayIndex = when (dayStr) {
                        "M" -> 0
                        "TU" -> 1
                        "W" -> 2
                        "TH" -> 3
                        "F" -> 4
                        "SA" -> 5
                        else -> return@forEach
                    }
                    
                    // Update this cell with the course info
                    array[cellDayIndex][periodNum] = courseTitle
                    arrayInstructor[cellDayIndex][periodNum] = instructor
                    arraySchedule[cellDayIndex][periodNum] = schedule
                    arrayCourseno[cellDayIndex][periodNum] = courseno
                    arrayRoom[cellDayIndex][periodNum] = room
                    arrayMode[cellDayIndex][periodNum] = mode
                    arrayColor[cellDayIndex][periodNum] = colorName
                }
            }
        }
        
        // Save the updated timetable data
        saveCurrentTimetableData()
    }
    
    // Clear cells that had this course but are not in the new schedule
    private fun clearCellsNotInSchedule(courseTitle: String, newSchedule: String) {
        // Find all cells that have this course
        for (dayIndex in 0..5) {
            for (periodIndex in 0..8) {
                if (array[dayIndex][periodIndex] == courseTitle) {
                    // Check if this cell is in the new schedule
                    val cellName = convertIndicesToDay(dayIndex, periodIndex)
                    val cellInSchedule = isScheduleInCellName(cellName, newSchedule)
                    
                    if (!cellInSchedule) {
                        // Clear this cell
                        array[dayIndex][periodIndex] = ""
                        arrayInstructor[dayIndex][periodIndex] = ""
                        arraySchedule[dayIndex][periodIndex] = ""
                        arrayCourseno[dayIndex][periodIndex] = ""
                        arrayRoom[dayIndex][periodIndex] = ""
                        arrayMode[dayIndex][periodIndex] = ""
                        arrayColor[dayIndex][periodIndex] = "white"
                    }
                }
            }
        }
    }
    
    // Helper method to check if a cell is part of a schedule
    private fun isScheduleInCellName(cellName: String, schedule: String): Boolean {
        // Parse cellName to get day and period
        var dayType = ""
        var periodNum = ""
        
        when {
            cellName.startsWith("M") -> {
                dayType = "M"
                periodNum = cellName.removePrefix("M")
            }
            cellName.startsWith("TU") -> {
                dayType = "TU"
                periodNum = cellName.removePrefix("TU")
            }
            cellName.startsWith("W") -> {
                dayType = "W"
                periodNum = cellName.removePrefix("W")
            }
            cellName.startsWith("TH") -> {
                dayType = "TH"
                periodNum = cellName.removePrefix("TH")
            }
            cellName.startsWith("F") -> {
                dayType = "F"
                periodNum = cellName.removePrefix("F")
            }
            cellName.startsWith("SA") -> {
                dayType = "SA"
                periodNum = cellName.removePrefix("SA")
            }
        }
        
        if (periodNum == "L") periodNum = "L"
        
        // Check if this day/period is in the schedule
        val scheduleParts = schedule.split(",")
        return scheduleParts.any { part ->
            val cleanPart = cleanScheduleString(part.trim())
            cleanPart == "$periodNum/$dayType"
        }
    }
    
    // Convert indices to day string
    fun convertIndicesToDay(horizontal: Int, vertical: Int): String {
        return cellNameForRealm[horizontal][vertical]
    }
    
    // Delete a course completely from all cells
    fun deleteCourse(courseTitle: String) {
        // Find and clear all cells with this course
        for (dayIndex in 0..5) {
            for (periodIndex in 0..8) {
                if (array[dayIndex][periodIndex] == courseTitle) {
                    // Clear this cell
                    array[dayIndex][periodIndex] = ""
                    arrayInstructor[dayIndex][periodIndex] = ""
                    arraySchedule[dayIndex][periodIndex] = ""
                    arrayCourseno[dayIndex][periodIndex] = ""
                    arrayRoom[dayIndex][periodIndex] = ""
                    arrayMode[dayIndex][periodIndex] = ""
                    arrayColor[dayIndex][periodIndex] = "white"
                }
            }
        }
        
        // Save the updated timetable data
        saveCurrentTimetableData()
    }
    
    // Filter courses for a specific cell
    fun filterCoursesForCell(cellName: String): List<CourseData> {
        return coursesData.value.filter { courseData ->
            val schedule = courseData.schedule
            if (schedule.isEmpty()) {
                false
            } else {
                val scheduleParts = schedule.split(",")
                scheduleParts.any { part ->
                    val cleanPart = cleanScheduleString(part.trim())
                    val parts = cleanPart.split("/")
                    
                    if (parts.size == 2) {
                        val periodStr = parts[0]
                        val dayStr = parts[1]
                        
                        var periodPart = ""
                        var dayPart = ""
                        
                        when {
                            cellName.startsWith("M") -> {
                                dayPart = "M"
                                periodPart = cellName.removePrefix("M")
                            }
                            cellName.startsWith("TU") -> {
                                dayPart = "TU"
                                periodPart = cellName.removePrefix("TU")
                            }
                            cellName.startsWith("W") -> {
                                dayPart = "W"
                                periodPart = cellName.removePrefix("W")
                            }
                            cellName.startsWith("TH") -> {
                                dayPart = "TH"
                                periodPart = cellName.removePrefix("TH")
                            }
                            cellName.startsWith("F") -> {
                                dayPart = "F"
                                periodPart = cellName.removePrefix("F")
                            }
                            cellName.startsWith("SA") -> {
                                dayPart = "SA"
                                periodPart = cellName.removePrefix("SA")
                            }
                        }
                        
                        // Convert lunch period
                        if (periodPart == "L") {
                            periodPart = "L"
                        }
                        
                        // Check if this cell matches the schedule
                        periodStr == periodPart && dayStr == dayPart
                    } else {
                        false
                    }
                }
            }
        }
    }
    
    // Check for course overlap
    fun checkCourseOverlap(courseTitle: String, schedule: String): Pair<Boolean, String> {
        if (schedule.isEmpty()) {
            return Pair(false, "")
        }
        
        val scheduleParts = schedule.split(",")
        val overlappingSlots = mutableListOf<String>()
        
        for (part in scheduleParts) {
            val cleanPart = cleanScheduleString(part.trim())
            val parts = cleanPart.split("/")
            
            if (parts.size == 2) {
                val periodStr = parts[0]
                val dayStr = parts[1]
                
                var dayIndex = -1
                var periodIndex = -1
                
                // Convert day string to index
                dayIndex = when (dayStr) {
                    "M" -> 0
                    "TU" -> 1
                    "W" -> 2
                    "TH" -> 3
                    "F" -> 4
                    "SA" -> 5
                    else -> -1
                }
                
                if (dayIndex == -1) continue
                
                // Convert period string to index
                if (periodStr == "L") {
                    periodIndex = 3
                } else {
                    val periodNum = periodStr.toIntOrNull() ?: continue
                    // Adjust period index for lunch
                    if (periodNum < 4) {
                        periodIndex = periodNum - 1
                    } else {
                        periodIndex = periodNum
                    }
                }
                
                if (periodIndex < 0 || periodIndex > 8) continue
                
                // Check if there's already a course in this slot
                val existingCourse = array[dayIndex][periodIndex]
                if (existingCourse.isNotEmpty() && existingCourse != courseTitle) {
                    // Found an overlap
                    overlappingSlots.add("$periodStr/$dayStr")
                }
            }
        }
        
        return Pair(overlappingSlots.isNotEmpty(), overlappingSlots.joinToString(", "))
    }
    
    // Save the current timetable data
    fun saveTimetableData(id: String) {
        // Save current data to our storage
        saveCurrentTimetableData()
    }
    
    // Clean up resources
    override fun onCleared() {
        super.onCleared()
        RealmManager.closeRealm()
    }
    
    // Debug method to print course data
    fun printCourseData(): String {
        val sb = StringBuilder()
        sb.appendLine("Total courses: ${coursesData.value.size}")
        
        coursesData.value.take(10).forEach { course ->
            sb.appendLine("Course: ${course.courseTitle}")
            sb.appendLine("  Schedule: ${course.schedule}")
            sb.appendLine("  Instructor: ${course.instructor}")
            sb.appendLine("  Room: ${course.room}")
            sb.appendLine("  Course No: ${course.courseno}")
            sb.appendLine("  Mode: ${course.mode}")
            sb.appendLine("-------------------")
        }
        
        return sb.toString()
    }
    
    // Save tasks to SharedPreferences
    private fun saveTasks() {
        sharedPreferences?.edit()?.apply {
            putString("tasks", gson.toJson(_tasks))
            apply()
        }
    }
    
    // Task management methods
    fun addTask(title: String, details: String, courseTitle: String, dueDate: Long = 0) {
        val newTask = TaskData(
            id = UUID.randomUUID().toString(),
            title = title,
            details = details,
            isDone = false,
            dueDate = dueDate,
            courseTitle = courseTitle
        )
        _tasks.add(newTask)
        saveTasks()
    }
    
    fun updateTask(id: String, isDone: Boolean) {
        val index = _tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            val task = _tasks[index]
            _tasks[index] = task.copy(isDone = isDone)
            saveTasks()
        }
    }
    
    fun deleteTask(id: String) {
        val index = _tasks.indexOfFirst { it.id == id }
        if (index != -1) {
            _tasks.removeAt(index)
            saveTasks()
        }
    }
    
    fun getAllTasks(): List<TaskData> {
        return _tasks
    }
} 