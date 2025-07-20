package com.project.todo_app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.project.todo_app.model.Todo

class ReminderManager(private val context: Context? = null) {
    
    fun scheduleReminder(todo: Todo, reminderTime: Long) {
        context?.let { ctx ->
            val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(ctx, ReminderBroadcastReceiver::class.java).apply {
                putExtra("todo_id", todo.id)
                putExtra("todo_title", todo.title)
                putExtra("todo_description", todo.description)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                ctx,
                todo.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Handle case where SCHEDULE_EXACT_ALARM permission is denied
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        }
    }
    
    fun cancelReminder(todoId: String) {
        context?.let { ctx ->
            val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(ctx, ReminderBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                ctx,
                todoId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}