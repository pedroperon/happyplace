package com.example.happyplace.data

import android.util.Log
import androidx.datastore.core.DataStore
import com.example.happyplace.LocalTasksList
import com.example.happyplace.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

class TasksListRepository (private val tasksListStore: DataStore<LocalTasksList>) {
    val tasksListFlow: Flow<LocalTasksList> = tasksListStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(
                    "TasksListRepo",
                    "Error reading tasks list.", exception
                )
                emit(LocalTasksList.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun fetchInitialTasksList() = tasksListStore.data.first()

    suspend fun saveTask(task: Task) {
        tasksListStore.updateData { tasksList ->
            tasksList
                .toBuilder()
                .addTasks(task)
                .build()
        }
    }

    suspend fun deleteTask(task: Task) {
        tasksListStore.updateData { tasksList ->
            val index = tasksList.tasksList.indexOf(task)
            if(index<0)
                tasksList
            else
                tasksList
                .toBuilder()
                .removeTasks(index)
                .build()
        }
    }
}
