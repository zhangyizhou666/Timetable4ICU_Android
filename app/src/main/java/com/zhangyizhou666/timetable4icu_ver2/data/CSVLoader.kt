package com.zhangyizhou666.timetable4icu_ver2.data

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility class to load CSV data from assets
 */
object CSVLoader {
    
    /**
     * Load CSV data from assets
     * 
     * @param context Android context
     * @param fileName CSV file name in assets
     * @return CSV content as string
     */
    fun loadCSVFromAssets(context: Context, fileName: String): String {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
            
            reader.close()
            stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
} 