package com.zhangyizhou666.timetable4icu_ver2.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier

// Combined clickable modifier extension
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.combinedClickable(
    onClick: () -> Unit,
    onLongClick: () -> Unit
): Modifier = this.then(
    Modifier.combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
    )
) 