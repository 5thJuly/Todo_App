package com.project.todo_app.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.todo_app.model.Todo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TodoRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val todosRef = database.getReference("todos")

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getTodos(): Flow<List<Todo>> = callbackFlow {
        val userId = getCurrentUserId() ?: return@callbackFlow
        val query = todosRef.orderByChild("userId").equalTo(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val todos = mutableListOf<Todo>()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(Todo::class.java)?.let { todo ->
                        todos.add(todo.copy(id = childSnapshot.key ?: ""))
                    }
                }
                trySend(todos.sortedByDescending { it.createdAt })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun addTodo(todo: Todo): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            val key = todosRef.push().key ?: return false
            val todoWithUser = todo.copy(userId = userId)
            todosRef.child(key).setValue(todoWithUser).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateTodo(todo: Todo): Boolean {
        return try {
            if (todo.id.isNotEmpty()) {
                todosRef.child(todo.id).setValue(todo).await()
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTodo(todoId: String): Boolean {
        return try {
            todosRef.child(todoId).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
