package com.project.todo_app.model

data class Todo(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = "",
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.PERSONAL,
    val reminderTime: Long? = null,
    val tags: List<String> = emptyList()
)

enum class Priority(val displayName: String, val color: Long) {
    LOW("Thấp", 0xFF4CAF50),      // Green
    MEDIUM("Trung bình", 0xFFFF9800), // Orange  
    HIGH("Cao", 0xFFF44336)       // Red
}

enum class Category(val displayName: String, val icon: String, val color: Long) {
    WORK("Công việc", "💼", 0xFF2196F3),      // Blue
    PERSONAL("Cá nhân", "👤", 0xFF9C27B0),    // Purple
    STUDY("Học tập", "📚", 0xFF00BCD4),       // Cyan
    HEALTH("Sức khỏe", "💪", 0xFF4CAF50),     // Green
    SHOPPING("Mua sắm", "🛒", 0xFFFF5722),    // Deep Orange
    HOME("Nhà cửa", "🏠", 0xFF795548),        // Brown
    OTHER("Khác", "📝", 0xFF607D8B)           // Blue Grey
}