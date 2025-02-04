package com.example.happyplace

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.app.job.JobService.NOTIFICATION_SERVICE
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.util.Log
import androidx.core.app.NotificationCompat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.random.Random

const val NOTIFICATION_CHANNEL_ID = "HappyPlaceLocalNotifications"
const val NOTIFICATION_CHANNEL_NAME = "TasksReminder"
const val TASK_NAME_KEY = "TASK_NAME"
const val TASK_DATE_STRING_KEY = "TASK_DATE_FORMAT_STRING"
const val TASK_DATE_MILLI_KEY = "TASK_DATE_MILLI"
const val TASK_DESCRIPTION_TEXT_KEY = "TASK_DESCRIPTION"

class TaskReminderJobService() : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        val title = "Reminder: ${params?.extras?.getString(TASK_NAME_KEY) ?: ""}"
        val dateString = params?.extras?.getString(TASK_DATE_STRING_KEY) ?: ""
        val longDescription = params?.extras?.getString(TASK_DESCRIPTION_TEXT_KEY)

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID) //context!!
            .setContentTitle(title)
            .setContentText(dateString)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(longDescription)
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            //.setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setWhen(params?.extras?.getLong(TASK_DATE_MILLI_KEY) ?: 0L)
            .build()

        (this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(Random.nextInt(), notification)

        jobFinished(params, false)
        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        return true
    }
}

class TaskReminderNotificationsHandler(private val context: Context) {

    private val notificationManager: NotificationManager = context.getSystemService(
        NOTIFICATION_SERVICE
    ) as NotificationManager

    init {
        createNotificationChannel()
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NOTIFICATION_CHANNEL_NAME
            val descriptionText = "Upcoming scheduled tasks"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleTasksNotifications(tasks: List<Task>, pendingIntent: PendingIntent) {

        val notificationScheduler =
            context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        for (task in tasks) {
            val delay = task.initialDate - System.currentTimeMillis()
            if(delay<0) continue

            val taskLocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(task.initialDate), ZoneId.systemDefault())

            val notificationDetailBundle = PersistableBundle()
            notificationDetailBundle.putString(TASK_NAME_KEY, task.name)
            notificationDetailBundle.putLong(TASK_DATE_MILLI_KEY, task.initialDate)
            notificationDetailBundle.putString(TASK_DATE_STRING_KEY,
                taskLocalDateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.MEDIUM))
            )
            notificationDetailBundle.putString(TASK_DESCRIPTION_TEXT_KEY,
                "You have un upcoming task for ${taskLocalDateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.MEDIUM))}. " +
                        "Tap for further details"
            )

            val notificationJob: JobInfo = JobInfo.Builder(
                Random.nextInt(),
                ComponentName(context, TaskReminderJobService::class.java)
            )
                .setMinimumLatency(delay)
                .setExtras(notificationDetailBundle)
                .build()

            Log.d("TaskReminderNotificationsHandler", "Scheduling task ${task.name} with delay $delay")

            notificationScheduler.schedule(notificationJob)
        }
    }
}