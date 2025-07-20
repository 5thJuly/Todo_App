package com.project.todo_app.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.todo_app.component.AddTodoDialog
import com.project.todo_app.component.FilterSection
import com.project.todo_app.component.QuickStatsCard
import com.project.todo_app.component.StatsDialog
import com.project.todo_app.component.ThemeDialog
import com.project.todo_app.component.TodoItem
import com.project.todo_app.model.ThemeViewModel

import com.project.todo_app.viewmodel.TodoViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    todoViewModel: TodoViewModel,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    
    val filteredTodos by todoViewModel.filteredTodos.collectAsState()
    val isLoading by todoViewModel.isLoading.collectAsState()
    val selectedCategory by todoViewModel.selectedCategory.collectAsState()
    val selectedPriority by todoViewModel.selectedPriority.collectAsState()
    val showCompleted by todoViewModel.showCompleted.collectAsState()
    val stats by todoViewModel.todoStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Todo App")
                        if (selectedCategory != null || selectedPriority != null || !showCompleted) {
                            Text(
                                text = "Đã lọc (${filteredTodos.size} nhiệm vụ)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Filter Toggle
                    IconButton(
                        onClick = { showFilters = !showFilters }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Bộ lọc",
                            tint = if (selectedCategory != null || selectedPriority != null || !showCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Statistics
                    IconButton(onClick = { showStatsDialog = true }) {
                        Icon(Icons.Default.Analytics, contentDescription = "Thống kê")
                    }
                    
                    // Theme
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Giao diện")
                    }
                    
                    // Logout
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng xuất")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm công việc")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Section
            if (showFilters) {
                FilterSection(
                    selectedCategory = selectedCategory,
                    selectedPriority = selectedPriority,
                    showCompleted = showCompleted,
                    onCategorySelected = todoViewModel::setSelectedCategory,
                    onPrioritySelected = todoViewModel::setSelectedPriority,
                    onShowCompletedChanged = todoViewModel::setShowCompleted,
                    onClearFilters = todoViewModel::clearFilters,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider()
            }
            
            // Quick Stats
            QuickStatsCard(
                stats = stats,
                modifier = Modifier.padding(16.dp)
            )
            
            // Todo List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (filteredTodos.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedCategory != null || selectedPriority != null || !showCompleted) {
                                "Không có nhiệm vụ phù hợp với bộ lọc"
                            } else {
                                "Chưa có công việc nào\nNhấn + để thêm công việc mới"
                            },
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (selectedCategory != null || selectedPriority != null || !showCompleted) {
                            TextButton(
                                onClick = todoViewModel::clearFilters,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Xóa bộ lọc")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTodos, key = { it.id }) { todo ->
                        TodoItem(
                            todo = todo,
                            onToggleComplete = { todoViewModel.toggleTodo(todo) },
                            onDelete = { todoViewModel.deleteTodo(todo) },
                            onEdit = { newTitle, newDescription, newPriority, newCategory, newReminderTime, newTags ->
                                todoViewModel.updateTodo(
                                    todo, newTitle, newDescription, 
                                    newPriority, newCategory, newReminderTime, newTags
                                )
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddTodoDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, description, priority, category, reminderTime, tags ->
                    todoViewModel.addTodo(title, description, priority, category, reminderTime, tags)
                    showAddDialog = false
                }
            )
        }
        
        if (showStatsDialog) {
            StatsDialog(
                stats = stats,
                onDismiss = { showStatsDialog = false }
            )
        }
        
        if (showThemeDialog) {
            ThemeDialog(
                themeViewModel = themeViewModel,
                onDismiss = { showThemeDialog = false }
            )
        }
    }
}
