package com.project.todo_app.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.project.todo_app.model.Category
import com.project.todo_app.model.Priority


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Priority, Category, Long?, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(Category.PERSONAL) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var tagsText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Thêm Task mới",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    )
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Priority Selection
                Text(
                    text = "Mức độ ưu tiên",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.displayName) },
                            selected = selectedPriority == priority,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = androidx.compose.ui.graphics.Color(priority.color).copy(alpha = 0.3f),
                                selectedLabelColor = androidx.compose.ui.graphics.Color(priority.color)
                            )
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = "Danh mục",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = Category.entries
                    categories.chunked(3).forEach { categoryRow ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoryRow.forEach { category ->
                                FilterChip(
                                    onClick = { selectedCategory = category },
                                    label = { Text("${category.icon} ${category.displayName}") },
                                    selected = selectedCategory == category,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = androidx.compose.ui.graphics.Color(category.color).copy(alpha = 0.3f),
                                        selectedLabelColor = androidx.compose.ui.graphics.Color(category.color)
                                    ),
                                    modifier = Modifier.weight(1f, false)
                                )
                            }
                            repeat(3 - categoryRow.size) {
                                Spacer(modifier = Modifier.weight(1f, false))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                // Reminder Section
                ReminderSection(
                    reminderTime = reminderTime,
                    onReminderTimeChanged = { reminderTime = it }
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Tags
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Thẻ (phân cách bằng dấu phẩy)") },
                    placeholder = { Text("ví dụ: quan trọng, gấp, dự án") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(Modifier.height(24.dp))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val tags = tagsText.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotBlank() }
                                onAdd(
                                    title, description, selectedPriority, 
                                    selectedCategory, reminderTime, tags
                                )
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Thêm")
                    }
                }
            }
        }
    }
}
