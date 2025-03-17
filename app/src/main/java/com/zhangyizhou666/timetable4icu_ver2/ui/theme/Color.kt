package com.zhangyizhou666.timetable4icu_ver2.ui.theme

import androidx.compose.ui.graphics.Color

// Base colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Timetable colors (matching iOS app)
val DarkBlue = Color(0xFF1F3C88)
val Silver = Color(0xFFEEEEF6)
val SeparatorColor = Color(0xFFCCCCD2)

// Course colors
val RosePink = Color(0xFFFCD0E3)
val LightBlue = Color(0xFFCFE3F6)
val LightOrange = Color(0xFFFEDCB3)
val LightGreen = Color(0xFFD8EEBE)
val LightPurple = Color(0xFFD6CEED)
val LightYellow = Color(0xFFF8EAB3)
val CreamWhite = Color(0xFFF6F6F6)

// Function to get color from name (used for Realm storage)
fun getColorFromName(colorName: String): Color {
    return when (colorName) {
        "rosePink" -> RosePink
        "lightBlue" -> LightBlue
        "lightOrange" -> LightOrange
        "lightGreen" -> LightGreen 
        "lightPurple" -> LightPurple
        "lightYellow" -> LightYellow
        "white" -> CreamWhite
        else -> CreamWhite
    }
}

// Function to get color name from index
fun getColorNameFromIndex(index: Int): String {
    return when (index) {
        0 -> "white"
        1 -> "rosePink"
        2 -> "lightBlue"
        3 -> "lightOrange"
        4 -> "lightGreen"
        5 -> "lightPurple"
        6 -> "lightYellow"
        else -> "white"
    }
}

// Function to get all course colors as a list
fun getAllCourseColors(): List<Pair<String, Color>> {
    return listOf(
        Pair("white", CreamWhite),
        Pair("rosePink", RosePink),
        Pair("lightBlue", LightBlue),
        Pair("lightOrange", LightOrange),
        Pair("lightGreen", LightGreen),
        Pair("lightPurple", LightPurple),
        Pair("lightYellow", LightYellow)
    )
}