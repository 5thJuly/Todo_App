package com.project.todo_app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode

    private val _isDynamicColor = MutableStateFlow(false)
    val isDynamicColor: StateFlow<Boolean> = _isDynamicColor

    init {
        loadThemePreferences()
    }

    private fun loadThemePreferences() {
        _isDarkMode.value = prefs.getBoolean("dark_mode", false)
        _isDynamicColor.value = prefs.getBoolean("dynamic_color", false)
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            _isDarkMode.value = isDark
            prefs.edit { putBoolean("dark_mode", isDark) }
        }
    }

    fun setDynamicColor(isDynamic: Boolean) {
        viewModelScope.launch {
            _isDynamicColor.value = isDynamic
            prefs.edit { putBoolean("dynamic_color", isDynamic) }
        }
    }
}