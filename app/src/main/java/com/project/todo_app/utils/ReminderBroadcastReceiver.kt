package com.project.todo_app.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.project.todo_app.MainActivity

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val todoTitle = intent.getStringExtra("todo_title") ?: "Nhiệm vụ"
        val todoDescription = intent.getStringExtra("todo_description") ?: ""
        val todoId = intent.getStringExtra("todo_id") ?: ""
        
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, "TODO_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔔 Nhắc nhở: $todoTitle")
            .setContentText(todoDescription.ifBlank { "Đã đến lúc thực hiện nhiệm vụ!" })
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(todoId.hashCode(), notification)
    }
}