package com.example.happyplace.data

import android.util.Log
import androidx.datastore.core.DataStore
import com.example.happyplace.LocalTasksList
import com.example.happyplace.Periodicity
import com.example.happyplace.Task
import com.example.happyplace.copy
import com.example.happyplace.utils.isRecurrent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

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

    suspend fun saveNewTask(newTask: Task, overOldTask: Task? = null) {
        tasksListStore.updateData { tasksList ->

            val indexOld = tasksList.tasksList.indexOf(overOldTask)

            if(newTask.id == "")
                newTask.toBuilder()
                    .setId("${newTask.initialDate}_${System.currentTimeMillis()}")
                    .build()
            
            tasksList
                .toBuilder()
                .removeTasks(indexOld)
                .addAllTasks(createAllRepetitionsForTask(newTask))
                .build()
        }
    }

    private fun createAllRepetitionsForTask(task: Task): MutableIterable<Task> {
        val tasks = mutableListOf(task)
        if(task.isRecurrent()) {
            val initialZonedDateTime =
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.initialDate), ZoneId.systemDefault())
            var iteration = 1
            do {
                val currentZonedDateTime = initialZonedDateTime.addPeriods(
                    periodicity = task.periodicity,
                    numberOfPeriods = iteration
                )
                tasks.add(task.copy { initialDate = currentZonedDateTime!!.toInstant().toEpochMilli() })
                iteration++
            }
            while(currentZonedDateTime!!.year < 2100)
        }
        return tasks
    }

    suspend fun updateTask(task:Task, andFutureRepetitions:Boolean=false) {
        tasksListStore.updateData { tasksList ->
            val toRemove = tasksList.tasksList.filter {
                it.id == task.id && it.initialDate>=task.initialDate
            }
            tasksList.tasksList.removeAll(toRemove)

            tasksList
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

    suspend fun deleteAllTasks() {
        tasksListStore.updateData { localTasksList ->
            localTasksList.toBuilder()
                .clearTasks()
                .build()
        }
    }
}

private fun ZonedDateTime.addPeriods(periodicity: Periodicity, numberOfPeriods: Int): ZonedDateTime? {
    val n = periodicity.numberOfIntervals.toLong()
    return when(periodicity.intervalType) {
        Periodicity.IntervalType.DAY -> this.plusDays(n*numberOfPeriods)
        Periodicity.IntervalType.WEEK -> this.plusWeeks(n*numberOfPeriods)
        Periodicity.IntervalType.MONTH -> this.plusMonths(n*numberOfPeriods)
        Periodicity.IntervalType.YEAR -> this.plusYears(n*numberOfPeriods)
        else -> this
    }
}
