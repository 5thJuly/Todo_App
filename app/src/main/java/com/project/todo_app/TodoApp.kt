package com.project.todo_app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.todo_app.viewmodel.ThemeViewModel
import com.project.todo_app.screen.AuthScreen
import com.project.todo_app.screen.TodoScreen
import com.project.todo_app.viewmodel.AuthViewModel
import com.project.todo_app.viewmodel.TodoViewModel


@Composable
fun TodoApp() {
    val authViewModel: AuthViewModel = viewModel()
    val todoViewModel: TodoViewModel = viewModel()
    val themeViewModel: ThemeViewModel = viewModel()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    if (isAuthenticated) {
        TodoScreen(
            todoViewModel = todoViewModel,
            themeViewModel = themeViewModel,
            onLogout = { authViewModel.logout() }
        )
    } else {
        AuthScreen(authViewModel = authViewModel)
    }


}