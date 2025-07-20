package com.project.todo_app.model

data class User(
    val uid: String = "",
    val email: String = "",
    val preferences: UserPreferences = UserPreferences()
)

data class UserPreferences(
    val isDarkMode: Boolean? = null,
    val reminderEnabled: Boolean = true,
    val defaultReminderMinutes: Int = 30
)