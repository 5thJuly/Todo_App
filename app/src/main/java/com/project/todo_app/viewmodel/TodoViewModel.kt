package com.project.todo_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.todo_app.model.Category
import com.project.todo_app.model.CategoryStats
import com.project.todo_app.model.Priority
import com.project.todo_app.model.PriorityStats
import com.project.todo_app.model.Todo
import com.project.todo_app.model.TodoStats
import com.project.todo_app.repository.TodoRepository
import com.project.todo_app.utils.ReminderManager

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TodoViewModel : ViewModel() {
    private val repository = TodoRepository()
    private val reminderManager = ReminderManager()
    
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Filter states
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory
    
    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    val selectedPriority: StateFlow<Priority?> = _selectedPriority
    
    private val _showCompleted = MutableStateFlow(true)
    val showCompleted: StateFlow<Boolean> = _showCompleted
    
    // Filtered todos
    val filteredTodos = combine(
        _todos,
        _selectedCategory,
        _selectedPriority,
        _showCompleted
    ) { todos, category, priority, showCompleted ->
        todos.filter { todo ->
            val categoryMatch = category == null || todo.category == category
            val priorityMatch = priority == null || todo.priority == priority
            val completedMatch = showCompleted || !todo.isCompleted
            
            categoryMatch && priorityMatch && completedMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Statistics
    val todoStats = _todos.map { todos ->
        calculateStats(todos)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoStats()
    )

    init {
        loadTodos()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            repository.getTodos().collect { todoList ->
                _todos.value = todoList
            }
        }
    }

    fun addTodo(
        title: String, 
        description: String, 
        priority: Priority = Priority.MEDIUM,
        category: Category = Category.PERSONAL,
        reminderTime: Long? = null,
        tags: List<String> = emptyList()
    ) {
        if (title.isBlank()) return
        
        viewModelScope.launch {
            _isLoading.value = true
            val todo = Todo(
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                category = category,
                reminderTime = reminderTime,
                tags = tags
            )
            
            val success = repository.addTodo(todo)
            if (success && reminderTime != null) {
                reminderManager.scheduleReminder(todo.copy(id = "temp"), reminderTime)
            }
            
            _isLoading.value = false
        }
    }

    fun toggleTodo(todo: Todo) {
        viewModelScope.launch {
            val updatedTodo = todo.copy(isCompleted = !todo.isCompleted)
            repository.updateTodo(updatedTodo)
            
            // Cancel reminder if completed
            if (updatedTodo.isCompleted) {
                reminderManager.cancelReminder(todo.id)
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo.id)
            reminderManager.cancelReminder(todo.id)
        }
    }

    fun updateTodo(
        todo: Todo, 
        newTitle: String, 
        newDescription: String,
        newPriority: Priority,
        newCategory: Category,
        newReminderTime: Long?,
        newTags: List<String>
    ) {
        if (newTitle.isBlank()) return
        
        viewModelScope.launch {
            val updatedTodo = todo.copy(
                title = newTitle.trim(),
                description = newDescription.trim(),
                priority = newPriority,
                category = newCategory,
                reminderTime = newReminderTime,
                tags = newTags
            )
            repository.updateTodo(updatedTodo)
            
            // Update reminder
            reminderManager.cancelReminder(todo.id)
            if (newReminderTime != null && !updatedTodo.isCompleted) {
                reminderManager.scheduleReminder(updatedTodo, newReminderTime)
            }
        }
    }
    
    // Filter functions
    fun setSelectedCategory(category: Category?) {
        _selectedCategory.value = category
    }
    
    fun setSelectedPriority(priority: Priority?) {
        _selectedPriority.value = priority
    }
    
    fun setShowCompleted(show: Boolean) {
        _showCompleted.value = show
    }
    
    fun clearFilters() {
        _selectedCategory.value = null
        _selectedPriority.value = null
        _showCompleted.value = true
    }
    
    private fun calculateStats(todos: List<Todo>): TodoStats {
        val total = todos.size
        val completed = todos.count { it.isCompleted }
        val pending = total - completed
        val completionRate = if (total > 0) completed.toFloat() / total else 0f
        
        // Today's stats
        val today = System.currentTimeMillis()
        val startOfDay = today - (today % (24 * 60 * 60 * 1000))
        val todayCompleted = todos.count { 
            it.isCompleted && it.createdAt >= startOfDay 
        }
        
        // Week's stats
        val startOfWeek = startOfDay - (6 * 24 * 60 * 60 * 1000)
        val weekCompleted = todos.count { 
            it.isCompleted && it.createdAt >= startOfWeek 
        }
        
        val startOfMonth = startOfDay - (29 * 24 * 60 * 60 * 1000)
        val monthCompleted = todos.count { 
            it.isCompleted && it.createdAt >= startOfMonth 
        }
        
        val categoryStats = Category.entries.associateWith { category ->
            val categoryTodos = todos.filter { it.category == category }
            val categoryCompleted = categoryTodos.count { it.isCompleted }
            CategoryStats(
                total = categoryTodos.size,
                completed = categoryCompleted,
                completionRate = if (categoryTodos.isNotEmpty())
                    categoryCompleted.toFloat() / categoryTodos.size else 0f
            )
        }.filterValues { it.total > 0 }
        
        val priorityStats = Priority.entries.associateWith { priority ->
            val priorityTodos = todos.filter { it.priority == priority }
            val priorityCompleted = priorityTodos.count { it.isCompleted }
            PriorityStats(
                total = priorityTodos.size,
                completed = priorityCompleted,
                completionRate = if (priorityTodos.isNotEmpty())
                    priorityCompleted.toFloat() / priorityTodos.size else 0f
            )
        }.filterValues { it.total > 0 }
        
        return TodoStats(
            totalTodos = total,
            completedTodos = completed,
            pendingTodos = pending,
            completionRate = completionRate,
            todayCompleted = todayCompleted,
            weekCompleted = weekCompleted,
            monthCompleted = monthCompleted,
            categoryStats = categoryStats,
            priorityStats = priorityStats
        )
    }
}


