package com.project.todo_app.component


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSection(
    reminderTime: Long?,
    onReminderTimeChanged: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nhắc nhở",
                style = MaterialTheme.typography.labelLarge
            )
            
            if (reminderTime != null) {
                IconButton(
                    onClick = { onReminderTimeChanged(null) }
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Xóa nhắc nhở"
                    )
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        if (reminderTime != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatDateTime(reminderTime),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Đặt thời gian nhắc nhở")
            }
        }
    }
    
    if (showTimePicker) {
        ReminderTimePicker(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { time ->
                onReminderTimeChanged(time)
                showTimePicker = false
            }
        )
    }
}

@Composable
fun ReminderTimePicker(
    onDismiss: () -> Unit,
    onTimeSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    var selectedDate by remember { 
        mutableStateOf(calendar.apply { add(Calendar.HOUR, 1) }.timeInMillis) 
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn thời gian nhắc nhở") },
        text = {
            Column {
                Text("Thời gian hiện tại: ${formatDateTime(selectedDate)}")
                Spacer(Modifier.height(16.dp))
                
                // Quick options
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTimeOption("15 phút nữa", 15) { selectedDate = it }
                    QuickTimeOption("30 phút nữa", 30) { selectedDate = it }
                    QuickTimeOption("1 giờ nữa", 60) { selectedDate = it }
                    QuickTimeOption("2 giờ nữa", 120) { selectedDate = it }
                    QuickTimeOption("Ngày mai 9:00", -1) { 
                        val tomorrow = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, 9)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                        }
                        selectedDate = tomorrow.timeInMillis
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTimeSelected(selectedDate) }
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun QuickTimeOption(
    label: String,
    minutes: Int,
    onSelected: (Long) -> Unit
) {
    TextButton(
        onClick = {
            val time = if (minutes == -1) {
                // Special case for "tomorrow 9:00"
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.timeInMillis
            } else {
                System.currentTimeMillis() + (minutes * 60 * 1000L)
            }
            onSelected(time)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label)
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}