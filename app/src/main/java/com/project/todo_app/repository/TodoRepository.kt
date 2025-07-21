package com.project.todo_app.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.todo_app.model.Todo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.project.todo_app.model.Category
import com.project.todo_app.model.Priority

class TodoRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val todosRef = database.getReference("todos")

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getTodos(): Flow<List<Todo>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            Log.w("TodoRepository", "User not authenticated")
            trySend(emptyList())
            return@callbackFlow
        }

        Log.d("TodoRepository", "Getting todos for user: $userId")
        val query = todosRef.orderByChild("userId").equalTo(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("TodoRepository", "onDataChange called, snapshot exists: ${snapshot.exists()}")
                Log.d("TodoRepository", "Snapshot children count: ${snapshot.childrenCount}")

                val todos = mutableListOf<Todo>()
                for (childSnapshot in snapshot.children) {
                    try {
                        Log.d("TodoRepository", "Processing child: ${childSnapshot.key}")
                        Log.d("TodoRepository", "Child value: ${childSnapshot.value}")

                        val todoMap = childSnapshot.value as? Map<*, *>
                        if (todoMap != null) {
                            val priorityStr = todoMap["priority"] as? String ?: "MEDIUM"
                            val categoryStr = todoMap["category"] as? String ?: "PERSONAL"
                            val todo = Todo(
                                id = childSnapshot.key ?: "",
                                title = todoMap["title"] as? String ?: "",
                                description = todoMap["description"] as? String ?: "",
                                isCompleted = todoMap["isCompleted"] as? Boolean ?: false,
                                createdAt = (todoMap["createdAt"] as? Number)?.toLong() ?: 0L,
                                userId = todoMap["userId"] as? String ?: "",
                                priority = try { Priority.valueOf(priorityStr) } catch (_: IllegalArgumentException) { Priority.MEDIUM },
                                category = try { Category.valueOf(categoryStr) } catch (_: IllegalArgumentException) { Category.PERSONAL },
                                reminderTime = (todoMap["reminderTime"] as? Number)?.toLong(),
                                tags = (todoMap["tags"] as? List<String>) ?: emptyList()
                            )
                            todos.add(todo)

                        } else {
                            childSnapshot.getValue(Todo::class.java)?.let { todo ->
                                todos.add(todo.copy(id = childSnapshot.key ?: ""))
                                Log.d("TodoRepository", "Parsed with getValue: ${todo.title}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TodoRepository", "Error parsing todo: ${e.message}", e)
                        Log.e("TodoRepository", "Raw data: ${childSnapshot.value}")
                    }
                }

                val sortedTodos = todos.sortedByDescending { it.createdAt }
                Log.d("TodoRepository", "Sending ${sortedTodos.size} todos to UI")
                trySend(sortedTodos)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TodoRepository", "Database error: ${error.message}")
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose {
            Log.d("TodoRepository", "Removing listener")
            query.removeEventListener(listener)
        }
    }

    suspend fun addTodo(todo: Todo): Boolean {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.w("TodoRepository", "Cannot add todo: User not authenticated")
                return false
            }

            val key = todosRef.push().key
            if (key == null) {
                Log.e("TodoRepository", "Cannot generate key for new todo")
                return false
            }

            // Tạo Map để tránh vấn đề serialization với Enum
            val todoMap = mapOf(
                "id" to key,
                "userId" to userId,
                "title" to todo.title,
                "description" to todo.description,
                "priority" to todo.priority.name, // Chuyển Enum thành String
                "category" to todo.category.name, // Chuyển Enum thành String
                "isCompleted" to todo.isCompleted,
                "createdAt" to todo.createdAt,
                "reminderTime" to todo.reminderTime,
                "tags" to todo.tags
            )

            Log.d("TodoRepository", "Adding todo with key: $key, data: $todoMap")
            todosRef.child(key).setValue(todoMap).await()
            Log.d("TodoRepository", "Todo added successfully")
            true
        } catch (e: Exception) {
            Log.e("TodoRepository", "Error adding todo: ${e.message}")
            false
        }
    }

    suspend fun updateTodo(todo: Todo): Boolean {
        return try {
            if (todo.id.isEmpty()) {
                Log.w("TodoRepository", "Cannot update todo: empty ID")
                return false
            }

            val todoMap = mapOf(
                "id" to todo.id,
                "userId" to todo.userId,
                "title" to todo.title,
                "description" to todo.description,
                "priority" to todo.priority.name,
                "category" to todo.category.name,
                "isCompleted" to todo.isCompleted,
                "createdAt" to todo.createdAt,
                "reminderTime" to todo.reminderTime,
                "tags" to todo.tags
            )

            todosRef.child(todo.id).setValue(todoMap).await()
            true
        } catch (e: Exception) {
            Log.e("TodoRepository", "Error updating todo: ${e.message}")
            false
        }
    }

    suspend fun deleteTodo(todoId: String): Boolean {
        return try {
            if (todoId.isEmpty()) {
                Log.w("TodoRepository", "Cannot delete todo: empty ID")
                return false
            }

            todosRef.child(todoId).removeValue().await()
            true
        } catch (e: Exception) {
            Log.e("TodoRepository", "Error deleting todo: ${e.message}")
            false
        }
    }
}