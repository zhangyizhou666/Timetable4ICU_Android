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
    val arrayColor: Array<Array<String>> = Array(6) { Array(9) { "white" } },
    val arrayStartTime: Array<Array<String>> = Array(6) { Array(9) { "" } },
    val arrayEndTime: Array<Array<String>> = Array(6) { Array(9) { "" } }
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
        if (!arrayStartTime.contentDeepEquals(other.arrayStartTime)) return false
        if (!arrayEndTime.contentDeepEquals(other.arrayEndTime)) return false

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
        result = 31 * result + arrayStartTime.contentDeepHashCode()
        result = 31 * result + arrayEndTime.contentDeepHashCode()
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

// Time slot data class to store standardized time information
data class TimeSlot(
    val day: String,        // Short day code: M, TU, W, TH, F, SA, SU
    val startTime: String,  // Format: HH:MM (24-hour)
    val endTime: String     // Format: HH:MM (24-hour)
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
    var arrayStartTime: Array<Array<String>> = Array(6) { Array(9) { "" } }
    var arrayEndTime: Array<Array<String>> = Array(6) { Array(9) { "" } }
    
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
        "1" to Pair("8:45", "10:00"),
        "2" to Pair("10:10", "11:25"),
        "3" to Pair("11:35", "12:50"),
        "L" to Pair("12:50", "14:00"),
        "4" to Pair("14:00", "15:15"),
        "5" to Pair("15:25", "16:40"),
        "6" to Pair("16:50", "18:05"),
        "7" to Pair("18:15", "19:30"),
        "8" to Pair("19:40", "20:55")
    )
    
    // Lunch time details (now included directly in timeDetailWithEighth)
    val lunchTimeDetails = mapOf(
        "L" to Pair("12:50", "14:00")
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
        val color: String = "white",
        val credits: Int = 0,
        val startTime: String = "", // Time format: HH:MM (24-hour)
        val endTime: String = "",    // Time format: HH:MM (24-hour)
        val timeSlots: List<TimeSlot> = emptyList()
    )
    
    // List of timetables
    private val _timetables = mutableStateListOf<Timetable>()
    val timetables: List<Timetable> = _timetables
    
    // Tasks list - in memory storage
    private val _tasks = mutableStateListOf<TaskData>()
    val tasks: List<TaskData> = _tasks
    
    private val _showRoomName = MutableStateFlow(false)
    val showRoomName: StateFlow<Boolean> = _showRoomName
    
    private val _selectedCourseCredits = MutableStateFlow(0)
    val selectedCourseCredits: StateFlow<Int> = _selectedCourseCredits
    
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
                        arrayColor = dataMap["arrayColor"] ?: Array(6) { Array(9) { "white" } },
                        arrayStartTime = dataMap["arrayStartTime"] ?: Array(6) { Array(9) { "" } },
                        arrayEndTime = dataMap["arrayEndTime"] ?: Array(6) { Array(9) { "" } }
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
            arrayColor = arrayColor.map { it.clone() }.toTypedArray(),
            arrayStartTime = arrayStartTime.map { it.clone() }.toTypedArray(),
            arrayEndTime = arrayEndTime.map { it.clone() }.toTypedArray()
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
            "arrayColor" to currentData.arrayColor,
            "arrayStartTime" to currentData.arrayStartTime,
            "arrayEndTime" to currentData.arrayEndTime
        )
        
        // Save to SharedPreferences
        sharedPreferences?.edit()?.apply {
            putString("timetable_data_$currentTimetableId", gson.toJson(dataMap))
            apply()
        }
    }
    
    // Parse schedule_time field with new ICS-like format: "M/11:35-12:50,TU/15:25-16:40,W/11:35-12:50,F/11:35-12:50"
    private fun parseICSLikeScheduleTime(scheduleTime: String): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        
        // If empty or NO DATA, return empty list
        if (scheduleTime.isEmpty() || scheduleTime == "NO DATA") return timeSlots
        
        // Split by comma to get individual time slots
        val slotEntries = scheduleTime.split(",")
        
        for (slotEntry in slotEntries) {
            val parts = slotEntry.trim().split("/")
            if (parts.size == 2) {
                val day = parts[0].trim()
                val timeRange = parts[1].trim()
                
                val timeParts = timeRange.split("-")
                if (timeParts.size == 2) {
                    val startTime = timeParts[0].trim()
                    val endTime = timeParts[1].trim()
                    
                    // Add the time slot
                    timeSlots.add(TimeSlot(day, startTime, endTime))
                }
            }
        }
        
        return timeSlots
    }
    
    // Parse schedule_time field with format like: "Monday 11:35-12:50; Tuesday 15:25-16:40"
    private fun parseScheduleTimeField(scheduleTime: String): List<TimeSlot> {
        val timeSlots = mutableListOf<TimeSlot>()
        
        // Split by semicolon to get day-specific time slots
        val daySchedules = scheduleTime.split(";")
        
        for (daySchedule in daySchedules) {
            val trimmed = daySchedule.trim()
            if (trimmed.isEmpty()) continue
            
            // Pattern: "DayName HH:MM-HH:MM"
            val regex = """(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\s+(\d{1,2}:\d{2})-(\d{1,2}:\d{2})""".toRegex()
            val matchResult = regex.find(trimmed)
            
            if (matchResult != null) {
                val (dayName, startTime, endTime) = matchResult.destructured
                
                // Convert day name to short code
                val day = when (dayName) {
                    "Monday" -> "M"
                    "Tuesday" -> "TU"
                    "Wednesday" -> "W"
                    "Thursday" -> "TH"
                    "Friday" -> "F"
                    "Saturday" -> "SA"
                    "Sunday" -> "SU"
                    else -> ""
                }
                
                if (day.isNotEmpty()) {
                    timeSlots.add(TimeSlot(day, startTime, endTime))
                }
            }
        }
        
        return timeSlots
    }
    
    // Derive time slots from schedule string format like "3/M,5/TU,3/W,3/F"
    private fun deriveTimeSlotsFromSchedule(schedule: String): List<TimeSlot> {
        if (schedule == "NO DATA" || schedule.isEmpty()) return emptyList()
        
        val timeSlots = mutableListOf<TimeSlot>()
        val scheduleParts = schedule.split(",")
        
        for (part in scheduleParts) {
            val cleanPart = cleanScheduleString(part.trim())
            val parts = cleanPart.split("/")
            
            if (parts.size == 2) {
                val periodStr = parts[0]
                val day = parts[1]
                
                // Get time slot for this period
                val (startTime, endTime) = if (periodStr == "L") {
                    lunchTimeDetails["L"] ?: Pair("12:50", "14:00")
                } else {
                    timeDetailWithEighth[periodStr] ?: Pair("", "")
                }
                
                if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
                    timeSlots.add(TimeSlot(day, startTime, endTime))
                }
            }
        }
        
        return timeSlots
    }
    
    // Generate schedule string from time slots
    private fun generateScheduleFromTimeSlots(timeSlots: List<TimeSlot>): String {
        val scheduleParts = mutableListOf<String>()
        
        for (slot in timeSlots) {
            // Find closest period for this time
            val period = findClosestPeriod(slot.startTime, slot.endTime)
            if (period.isNotEmpty()) {
                scheduleParts.add("$period/${slot.day}")
            }
        }
        
        return scheduleParts.joinToString(",")
    }
    
    // Find the closest period based on start and end times
    private fun findClosestPeriod(startTime: String, endTime: String): String {
        // Check lunch period first
        val lunchTime = lunchTimeDetails["L"] ?: Pair("12:50", "14:00")
        if (startTime == lunchTime.first && endTime == lunchTime.second) {
            return "L"
        }
        
        // Check regular periods
        for ((period, timeRange) in timeDetailWithEighth) {
            if (startTime == timeRange.first && endTime == timeRange.second) {
                return period
            }
        }
        
        // If no exact match, find the closest
        val startMinutes = timeToMinutes(startTime)
        val endMinutes = timeToMinutes(endTime)
        
        var closestPeriod = ""
        var minDifference = Int.MAX_VALUE
        
        // Check regular periods
        for ((period, timeRange) in timeDetailWithEighth) {
            val periodStartMinutes = timeToMinutes(timeRange.first)
            val periodEndMinutes = timeToMinutes(timeRange.second)
            
            val startDiff = Math.abs(startMinutes - periodStartMinutes)
            val endDiff = Math.abs(endMinutes - periodEndMinutes)
            val totalDiff = startDiff + endDiff
            
            if (totalDiff < minDifference) {
                minDifference = totalDiff
                closestPeriod = period
            }
        }
        
        return closestPeriod
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
        
        // Calculate total credits after loading data
        calculateTotalCredits()
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
                arrayStartTime[i][j] = data.arrayStartTime[i][j]
                arrayEndTime[i][j] = data.arrayEndTime[i][j]
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
                arrayStartTime[i][j] = ""
                arrayEndTime[i][j] = ""
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
        colorName: String,
        startTime: String = "",
        endTime: String = ""
    ) {
        // First update the primary cell
        array[dayIndex][periodIndex] = courseTitle
        arrayInstructor[dayIndex][periodIndex] = instructor
        arraySchedule[dayIndex][periodIndex] = schedule
        arrayCourseno[dayIndex][periodIndex] = courseno
        arrayRoom[dayIndex][periodIndex] = room
        arrayMode[dayIndex][periodIndex] = mode
        arrayColor[dayIndex][periodIndex] = colorName
        arrayStartTime[dayIndex][periodIndex] = startTime
        arrayEndTime[dayIndex][periodIndex] = endTime
        
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
                    arrayStartTime[cellDayIndex][periodNum] = startTime
                    arrayEndTime[cellDayIndex][periodNum] = endTime
                }
            }
        }
        
        // Save the updated timetable data
        saveCurrentTimetableData()
        
        // Recalculate total credits
        calculateTotalCredits()
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
                        arrayStartTime[dayIndex][periodIndex] = ""
                        arrayEndTime[dayIndex][periodIndex] = ""
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
    
    // Delete a course from all cells where it appears
    fun deleteCourse(courseTitle: String) {
        println("Deleting entire course: $courseTitle")
        
        // Iterate through all cells
        for (i in array.indices) {
            for (j in array[i].indices) {
                if (array[i][j] == courseTitle) {
                    // Clear all data for this cell
                    array[i][j] = ""
                    arrayInstructor[i][j] = ""
                    arraySchedule[i][j] = ""
                    arrayRoom[i][j] = ""
                    arrayCourseno[i][j] = ""
                    arrayMode[i][j] = ""
                    arrayColor[i][j] = "white"
                    arrayStartTime[i][j] = ""
                    arrayEndTime[i][j] = ""
                }
            }
        }
        
        // Save changes
        saveTimetableData()
        
        // Recalculate credits after deletion
        calculateTotalCredits()
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
    fun checkCourseOverlap(courseTitle: String, schedule: String, startTime: String, endTime: String): Pair<Boolean, String> {
        // Update to use the new timeSlots parameter instead
        return checkCourseOverlapWithTimeSlots(courseTitle, schedule, parseTimeSlots(schedule, startTime, endTime))
    }
    
    // Check for course overlap using time slots
    private fun checkCourseOverlapWithTimeSlots(courseTitle: String, schedule: String, timeSlots: List<TimeSlot>): Pair<Boolean, String> {
        if (schedule.isEmpty() && timeSlots.isEmpty()) {
            return Pair(false, "")
        }
        
        val overlappingCourses = mutableSetOf<String>()
        
        // First check using time slots for more accurate overlap detection
        if (timeSlots.isNotEmpty()) {
            // Check each time slot for overlap with existing courses
            for (timeSlot in timeSlots) {
                val day = timeSlot.day
                val startTime = timeSlot.startTime
                val endTime = timeSlot.endTime
                
                // Convert day to index
                val dayIndex = when (day) {
                    "M" -> 0
                    "TU" -> 1
                    "W" -> 2
                    "TH" -> 3
                    "F" -> 4
                    "SA" -> 5
                    else -> -1
                }
                
                if (dayIndex == -1) continue
                
                // Check all cells in this day for overlapping courses
                for (periodIndex in 0 until 9) {
                    val existingCourse = array[dayIndex][periodIndex]
                    
                    // Skip if no course or it's the same course we're checking
                    if (existingCourse.isEmpty() || existingCourse == courseTitle) {
                        continue
                    }
                    
                    // Find the course data to check its time slots
                    val existingCourseData = coursesData.value.find { it.courseTitle == existingCourse }
                    
                    if (existingCourseData != null && existingCourseData.timeSlots.isNotEmpty()) {
                        // Check if any time slot overlaps with the current time slot
                        val hasOverlap = existingCourseData.timeSlots.any { existingSlot ->
                            existingSlot.day == day && 
                            timeRangesOverlap(startTime, endTime, existingSlot.startTime, existingSlot.endTime)
                        }
                        
                        if (hasOverlap) {
                            overlappingCourses.add(existingCourse)
                            // Once we found an overlap, no need to check other periods
                            // We can't break out of the loop from here, so we'll check other periods
                        }
                    }
                }
            }
        }
        
        // If no overlaps found by time slots, fall back to schedule-based detection
        if (overlappingCourses.isEmpty() && schedule.isNotEmpty()) {
            val scheduleParts = schedule.split(",")
            
            for (part in scheduleParts) {
                val cleanPart = cleanScheduleString(part.trim())
                val parts = cleanPart.split("/")
                
                if (parts.size == 2) {
                    val periodStr = parts[0]
                    val dayStr = parts[1]
                    
                    var dayIndex = -1
                    
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
                    
                    // Convert period string to index for schedule-based overlap
                    var periodIndex = -1
                    if (periodStr == "L") {
                        periodIndex = 3  // Lunch period
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
                        // Found an overlap - add the entire course
                        overlappingCourses.add(existingCourse)
                    }
                }
            }
        }
        
        return Pair(overlappingCourses.isNotEmpty(), overlappingCourses.joinToString(", "))
    }
    
    // Parse time slots from schedule, startTime, and endTime
    private fun parseTimeSlots(schedule: String, startTime: String, endTime: String): List<TimeSlot> {
        if (schedule.isEmpty()) return emptyList()
        
        // If we have explicit start and end times, use them with the schedule
        if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
            return schedule.split(",").mapNotNull { part ->
                val cleanPart = cleanScheduleString(part.trim())
                val parts = cleanPart.split("/")
                
                if (parts.size == 2) {
                    val day = parts[1]
                    TimeSlot(day, startTime, endTime)
                } else {
                    null
                }
            }
        }
        
        // Otherwise derive time slots from the schedule
        return deriveTimeSlotsFromSchedule(schedule)
    }
    
    // Calculate total credits for all selected courses
    private fun calculateTotalCredits() {
        var totalCredits = 0
        val courseCodesInTimetable = mutableSetOf<String>()
        
        // Loop through all cells to find selected courses
        for (i in 0 until 6) {  // Days (including Saturday)
            for (j in 0 until 9) {  // Periods (including lunch)
                val title = array[i][j]
                if (title.isNotEmpty()) {
                    // Extract course code from title (usually before the first space)
                    val courseCode = if (title.contains(" ")) title.split(" ")[0] else title
                    courseCodesInTimetable.add(courseCode)
                }
            }
        }
        
        // Sum up credits for each unique course
        courseCodesInTimetable.forEach { courseCode ->
            // Find the course in our loaded courses data
            _coursesData.value.find { 
                it.courseTitle.startsWith(courseCode) || it.courseno == courseCode 
            }?.let { course ->
                totalCredits += course.credits
            }
        }
        
        _selectedCourseCredits.value = totalCredits
    }
    
    // Update course title in a cell
    fun updateCourseTitle(dayIndex: Int, periodIndex: Int, title: String) {
        array[dayIndex][periodIndex] = title
        saveCurrentTimetableData()
    }
    
    // Public method to manually recalculate credits
    fun recalculateCredits() {
        calculateTotalCredits()
    }
    
    // Get all tasks
    fun getAllTasks(): List<TaskData> {
        return _tasks.toList()
    }
    
    // Update task completion status
    fun updateTask(taskId: String, isDone: Boolean) {
        val taskIndex = _tasks.indexOfFirst { it.id == taskId }
        if (taskIndex >= 0) {
            val updatedTask = _tasks[taskIndex].copy(isDone = isDone)
            _tasks[taskIndex] = updatedTask
            saveTasksToSharedPreferences()
        }
    }
    
    // Delete a task
    fun deleteTask(taskId: String) {
        val taskIndex = _tasks.indexOfFirst { it.id == taskId }
        if (taskIndex >= 0) {
            _tasks.removeAt(taskIndex)
            saveTasksToSharedPreferences()
        }
    }
    
    // Add a new task
    fun addTask(title: String, details: String, dueDate: Long, courseTitle: String) {
        val newTask = TaskData(
            title = title,
            details = details,
            dueDate = dueDate,
            courseTitle = courseTitle
        )
        _tasks.add(newTask)
        saveTasksToSharedPreferences()
    }
    
    // Save tasks to SharedPreferences
    private fun saveTasksToSharedPreferences() {
        sharedPreferences?.edit()?.apply {
            putString("tasks", gson.toJson(_tasks))
            apply()
        }
    }
    
    // Public method to save current timetable data
    fun saveTimetableData() {
        saveCurrentTimetableData()
    }
    
    // Debug method to print course data
    fun printCourseData(): String {
        val sb = StringBuilder()
        _coursesData.value.forEach { course ->
            sb.append("${course.courseTitle}, ${course.instructor}, ${course.schedule}, ${course.courseno}, ${course.room}, ${course.mode}, ${course.color}, ${course.credits}\n")
        }
        return sb.toString()
    }
    
    // Get the title of a conflicting course from the given slots
    fun getConflictingCourseTitle(slots: String): String {
        val slotList = slots.split(", ")
        if (slotList.isEmpty()) return ""
        
        // Get the first slot's indices
        val (dayIndex, periodIndex) = convertDayToIndices(slotList[0])
        
        // Return the course title at those indices
        return array[dayIndex][periodIndex]
    }
    
    // Convert a day string (e.g., "Mon 1") to day and period indices
    private fun convertDayToIndices(dayString: String): Pair<Int, Int> {
        val parts = dayString.split(" ")
        if (parts.size != 2) return Pair(0, 0)
        
        val day = parts[0]
        val period = parts[1].toIntOrNull() ?: 1
        
        val dayIndex = when (day) {
            "Mon" -> 0
            "Tue" -> 1
            "Wed" -> 2
            "Thu" -> 3
            "Fri" -> 4
            else -> 0
        }
        
        return Pair(dayIndex, period - 1)
    }
    
    // Convert time string (HH:MM) to minutes since midnight
    private fun timeToMinutes(time: String): Int {
        try {
            val parts = time.split(":")
            if (parts.size == 2) {
                val hours = parts[0].toIntOrNull() ?: 0
                val minutes = parts[1].toIntOrNull() ?: 0
                return hours * 60 + minutes
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }
    
    // Check if two time ranges overlap
    private fun timeRangesOverlap(start1: String, end1: String, start2: String, end2: String): Boolean {
        val start1Mins = timeToMinutes(start1)
        val end1Mins = timeToMinutes(end1)
        val start2Mins = timeToMinutes(start2)
        val end2Mins = timeToMinutes(end2)
        
        // Check for overlap
        return !(end1Mins <= start2Mins || start1Mins >= end2Mins)
    }
    
    // Filter courses for a specific time slot based on time overlap
    fun filterCoursesForTimeSlot(dayIndex: Int, periodIndex: Int): List<CourseData> {
        val dayStr = when (dayIndex) {
            0 -> "M"
            1 -> "TU"
            2 -> "W"
            3 -> "TH"
            4 -> "F"
            5 -> "SA"
            else -> ""
        }
        
        if (dayStr.isEmpty()) return emptyList()
        
        // Get the time range for this period
        val periodNum = periodIndex + 1
        val periodStr = if (periodIndex == 3) "L" else periodNum.toString()
        val timeRange = if (periodStr == "L") {
            lunchTimeDetails["L"]
        } else {
            timeDetailWithEighth[periodStr]
        } ?: Pair("", "")
        
        val slotStartTime = timeRange.first
        val slotEndTime = timeRange.second
        
        // Debug output to see what we're looking for
        println("Filtering courses for day: $dayStr, period: $periodStr, time: $slotStartTime-$slotEndTime")
        
        // If we couldn't determine the time range, fall back to schedule-based filtering
        if (slotStartTime.isEmpty() || slotEndTime.isEmpty()) {
            return filterCoursesForCell(convertIndicesToDay(dayIndex, periodIndex))
        }
        
        // Filter courses that overlap with this time slot and occur on this day
        val filteredCourses = coursesData.value.filter { courseData ->
            // 1. Check if this course occurs on the selected day and has overlapping time
            val hasOverlappingTimeSlot = courseData.timeSlots.any { timeSlot ->
                timeSlot.day == dayStr && 
                timeRangesOverlap(slotStartTime, slotEndTime, timeSlot.startTime, timeSlot.endTime)
            }
            
            if (hasOverlappingTimeSlot) {
                println("  OVERLAPS! Course: ${courseData.courseTitle}")
                true
            } else {
                // 2. Fall back to schedule-based filtering if no time slots or no overlap found
                val cellName = convertIndicesToDay(dayIndex, periodIndex)
                val schedule = courseData.schedule
                if (schedule.isEmpty()) {
                    false
                } else {
                    val scheduleParts = schedule.split(",")
                    scheduleParts.any { part ->
                        val cleanPart = cleanScheduleString(part.trim())
                        val parts = cleanPart.split("/")
                        
                        if (parts.size == 2) {
                            val periodStrPart = parts[0]
                            val dayStrPart = parts[1]
                            
                            var periodPart = ""
                            if (cellName.startsWith(dayStrPart)) {
                                periodPart = cellName.removePrefix(dayStrPart)
                                periodStrPart == periodPart
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    }
                }
            }
        }
        
        // Debug output to see filtered courses
        println("Found ${filteredCourses.size} courses for day: $dayStr, period: $periodStr")
        
        return filteredCourses
    }
    
    // Function to get time details for a specific period
    fun getTimeDetails(dayIndex: Int, periodIndex: Int): Pair<String, String> {
        // First check if we have actual time data in the arrays
        val startTime = arrayStartTime[dayIndex][periodIndex]
        val endTime = arrayEndTime[dayIndex][periodIndex]
        
        // If we have actual time data, use it
        if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
            return Pair(startTime, endTime)
        }
        
        // Otherwise, fall back to the default time mapping
        val periodKey = when (periodIndex) {
            0 -> "1"
            1 -> "2"
            2 -> "3"
            3 -> "L"  // Lunch period
            4 -> "4"
            5 -> "5"
            6 -> "6"
            7 -> "7"
            8 -> "8"
            else -> ""
        }
        
        return timeDetailWithEighth[periodKey] ?: Pair("", "")
    }
    
    // Update cell with course data (now includes updating time arrays)
    fun updateCell(dayIndex: Int, periodIndex: Int, courseData: CourseData) {
        val scheduleParts = courseData.schedule.split(",")
        
        // First, find all day/period combinations for this course
        val courseCells = mutableListOf<Pair<Int, Int>>() // List of (dayIndex, periodIndex) pairs
        
        // For each part of the schedule string, find all matching time slots
        for (part in scheduleParts) {
            val cleanPart = cleanScheduleString(part.trim())
            val parts = cleanPart.split("/")
            
            if (parts.size == 2) {
                val periodStr = parts[0]
                val dayStr = parts[1]
                
                // Convert day string to index
                val currentDayIndex = when (dayStr) {
                    "M" -> 0
                    "TU" -> 1
                    "W" -> 2
                    "TH" -> 3
                    "F" -> 4
                    "SA" -> 5
                    else -> -1
                }
                
                // Convert period to index
                val currentPeriodIndex = when (periodStr) {
                    "1" -> 0
                    "2" -> 1
                    "3" -> 2
                    "L" -> 3
                    "4" -> 4
                    "5" -> 5
                    "6" -> 6
                    "7" -> 7
                    "8" -> 8
                    else -> -1
                }
                
                if (currentDayIndex >= 0 && currentPeriodIndex >= 0) {
                    courseCells.add(Pair(currentDayIndex, currentPeriodIndex))
                }
            }
        }
        
        // Now determine the start and end times for each day
        val timesByDay = mutableMapOf<Int, Pair<String, String>>() // Map of dayIndex to (startTime, endTime)
        
        for (dayIndex in 0..5) {
            // Get all cells for this day
            val dayCells = courseCells.filter { it.first == dayIndex }.map { it.second }.sorted()
            if (dayCells.isEmpty()) continue
            
            // Find start and end times for this day by checking time slots
            var dayStartTime = ""
            var dayEndTime = ""
            
            // First try to find a time slot specific to this day
            val dayStr = when (dayIndex) {
                0 -> "M"
                1 -> "TU"
                2 -> "W"
                3 -> "TH"
                4 -> "F"
                5 -> "SA"
                else -> ""
            }
            
            val timeSlot = courseData.timeSlots.find { it.day == dayStr }
            if (timeSlot != null) {
                dayStartTime = timeSlot.startTime
                dayEndTime = timeSlot.endTime
            } else {
                // If no day-specific time slot, use the general start/end times
                dayStartTime = courseData.startTime
                dayEndTime = courseData.endTime
            }
            
            // Store for later use
            if (dayStartTime.isNotEmpty() && dayEndTime.isNotEmpty()) {
                timesByDay[dayIndex] = Pair(dayStartTime, dayEndTime)
            }
        }
        
        // Now update all cells for this course
        for ((cellDayIndex, cellPeriodIndex) in courseCells) {
            // Update cell data
            array[cellDayIndex][cellPeriodIndex] = courseData.courseTitle
            arrayInstructor[cellDayIndex][cellPeriodIndex] = courseData.instructor
            arraySchedule[cellDayIndex][cellPeriodIndex] = courseData.schedule
            arrayCourseno[cellDayIndex][cellPeriodIndex] = courseData.courseno
            arrayRoom[cellDayIndex][cellPeriodIndex] = courseData.room
            arrayMode[cellDayIndex][cellPeriodIndex] = courseData.mode
            arrayColor[cellDayIndex][cellPeriodIndex] = courseData.color
            
            // Get time for this day
            val (startTime, endTime) = timesByDay[cellDayIndex] ?: Pair("", "")
            if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
                arrayStartTime[cellDayIndex][cellPeriodIndex] = startTime
                arrayEndTime[cellDayIndex][cellPeriodIndex] = endTime
            }
        }
        
        // Save changes to the current timetable
        saveCurrentTimetableData()
        
        // Recalculate total credits
        calculateTotalCredits()
    }
    
    // Load data from CSV file
    fun loadCoursesFromCSV(year: String, term: String, csvData: String) {
        val coursesList = mutableListOf<CourseData>()
        
        // Debug flag for Japanese courses
        val debugJapanese = true
        
        // Skip header line and parse CSV data
        csvData.trim().lines().drop(1).forEach { line ->
            try {
                val fields = line.split(";")
                if (fields.size >= 11) {  // Make sure we have at least 11 fields including schedule_time
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
                        val credits = fields[9].trim().toIntOrNull() ?: 0
                        
                        // Debug Japanese courses
                        val isJapaneseCourse = title.startsWith("日本語")
                        if (isJapaneseCourse && debugJapanese) {
                            println("Found Japanese course: $title")
                            println("Schedule: $schedule")
                        }
                        
                        // Process schedule_time field (could have multiple time slots)
                        val scheduleTime = fields[10].trim().replace("\"", "")
                        
                        // Try to parse schedule_time using both formats
                        var timeSlots = if (scheduleTime.contains("/")) {
                            // This is likely the new ICS-like format
                            parseICSLikeScheduleTime(scheduleTime)
                        } else if (scheduleTime != "NO DATA" && scheduleTime.isNotEmpty()) {
                            // This is likely the traditional format with day names
                            parseScheduleTimeField(scheduleTime)
                        } else {
                            // If no time data, use default based on schedule
                            deriveTimeSlotsFromSchedule(schedule)
                        }
                        
                        // Special handling for Japanese courses to ensure correct time slots
                        if (isJapaneseCourse) {
                            // Ensure we have the correct time slots for Japanese courses
                            val fixedTimeSlots = mutableListOf<TimeSlot>()
                            
                            // Check if this is J1-J4 course that follows special pattern
                            if (title.matches(Regex("日本語J[1-4].*"))) {
                                // These courses follow a specific pattern:
                                // Monday-Wednesday: 8:45-11:25
                                // Friday: 8:45-12:50
                                
                                // First add Monday-Wednesday slots
                                fixedTimeSlots.add(TimeSlot("M", "08:45", "11:25"))
                                fixedTimeSlots.add(TimeSlot("TU", "08:45", "11:25"))
                                fixedTimeSlots.add(TimeSlot("W", "08:45", "11:25"))
                                
                                // Add Friday with longer time
                                fixedTimeSlots.add(TimeSlot("F", "08:45", "12:50"))
                                
                                timeSlots = fixedTimeSlots
                                
                                if (debugJapanese) {
                                    println("Applied fixed time slots for Japanese course:")
                                    timeSlots.forEach { 
                                        println("  ${it.day}: ${it.startTime}-${it.endTime}") 
                                    }
                                }
                            }
                        }
                        
                        // If no schedule info and no time slots, skip this course
                        val skipCourse = schedule == "NO DATA" && timeSlots.isEmpty()
                        if (!skipCourse) {
                            // If we have time slots but no schedule, generate a schedule
                            val finalSchedule = if (schedule == "NO DATA" && timeSlots.isNotEmpty()) {
                                generateScheduleFromTimeSlots(timeSlots)
                            } else {
                                schedule
                            }
                            
                            // Get start and end times from the first time slot, if available
                            val startTime = if (timeSlots.isNotEmpty()) timeSlots[0].startTime else ""
                            val endTime = if (timeSlots.isNotEmpty()) timeSlots[0].endTime else ""
                            
                            // Create a course entry
                            val courseData = CourseData(
                                courseTitle = title + if (lang.isNotEmpty()) " $lang" else "",
                                instructor = instructor,
                                schedule = finalSchedule,
                                courseno = courseno,
                                room = room,
                                mode = mode,
                                color = "white",
                                credits = credits,
                                startTime = startTime,
                                endTime = endTime,
                                timeSlots = timeSlots
                            )
                            coursesList.add(courseData)
                            
                            // Debug output for the first few courses
                            if (coursesList.size <= 5 || isJapaneseCourse) {
                                println("Added course: $title, schedule: $finalSchedule, time slots: ${timeSlots.size}")
                                timeSlots.forEach { slot ->
                                    println("  - ${slot.day} ${slot.startTime}-${slot.endTime}")
                                }
                                println("  Start time: $startTime, End time: $endTime")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Skip malformed entries
                e.printStackTrace()
                println("Error parsing line: $line")
                println("Exception: ${e.message}")
            }
        }
        
        _coursesData.update { coursesList }
        println("Loaded ${coursesList.size} courses for $year $term")
    }
} 