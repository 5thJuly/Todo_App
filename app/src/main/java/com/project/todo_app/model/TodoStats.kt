package com.project.todo_app.model

data class TodoStats(
    val totalTodos: Int = 0,
    val completedTodos: Int = 0,
    val pendingTodos: Int = 0,
    val completionRate: Float = 0f,
    val todayCompleted: Int = 0,
    val weekCompleted: Int = 0,
    val monthCompleted: Int = 0,
    val categoryStats: Map<Category, CategoryStats> = emptyMap(),
    val priorityStats: Map<Priority, PriorityStats> = emptyMap()
)
data class CategoryStats(
    val total: Int = 0,
    val completed: Int = 0,
    val completionRate: Float = 0f
)

data class PriorityStats(
    val total: Int = 0,
    val completed: Int = 0,
    val completionRate: Float = 0f
)