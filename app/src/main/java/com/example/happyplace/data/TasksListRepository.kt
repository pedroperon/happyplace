package com.example.happyplace.data

import android.util.Log
import androidx.datastore.core.DataStore
import com.example.happyplace.LocalTasksList
import com.example.happyplace.Periodicity
import com.example.happyplace.Task
import com.example.happyplace.copy
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

    suspend fun saveNewTask(task: Task) {
        tasksListStore.updateData { tasksList ->
            if(task.id == "")
                task.toBuilder()
                    .setId("${task.initialDate}_${System.currentTimeMillis()}")
                    .build()
            
            tasksList
                .toBuilder()
                .addAllTasks(createAllRepetitionsForTask(task))
                .build()
        }
    }

    private fun createAllRepetitionsForTask(task: Task): MutableIterable<Task> {
        val tasks = mutableListOf(task)
        if(task.hasPeriodicity() && task.periodicity!=null && task.periodicity.numberOfIntervals>0) {
            var nextDate =
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(task.initialDate), ZoneId.systemDefault())
            do {
                nextDate = nextDate.addPeriod(task.periodicity)
                tasks.add(task.copy { initialDate = nextDate.toInstant().toEpochMilli() })
            }
            while(nextDate.year < 2100)
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
}

private fun ZonedDateTime.addPeriod(periodicity: Periodicity): ZonedDateTime? {
    val n = periodicity.numberOfIntervals.toLong()
    return when(periodicity.intervalType) {
        Periodicity.IntervalType.DAY -> this.plusDays(n)
        Periodicity.IntervalType.WEEK -> this.plusWeeks(n)
        Periodicity.IntervalType.MONTH -> this.plusMonths(n)
        Periodicity.IntervalType.YEAR -> this.plusYears(n)
        else -> this
    }
}
