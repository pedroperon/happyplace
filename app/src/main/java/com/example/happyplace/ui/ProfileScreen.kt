package com.example.happyplace.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import com.example.happyplace.model.TasksCalendarViewModel

@Composable
fun ProfileScreen(tasksCalendarViewModel: TasksCalendarViewModel) {
    Box(Modifier.fillMaxSize().wrapContentSize()) {

        // debug, erase all tasks
        Button(onClick = { tasksCalendarViewModel.eraseAllTasks() }) {
            Text(text = "Erase all tasks")
        }
    }
}
