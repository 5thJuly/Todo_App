package com.project.todo_app.component


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.todo_app.model.Category
import com.project.todo_app.model.Priority


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    selectedCategory: Category?,
    selectedPriority: Priority?,
    showCompleted: Boolean,
    onCategorySelected: (Category?) -> Unit,
    onPrioritySelected: (Priority?) -> Unit,
    onShowCompletedChanged: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Filter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bộ lọc",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (selectedCategory != null || selectedPriority != null || !showCompleted) {
                TextButton(
                    onClick = onClearFilters
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Xóa bộ lọc")
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Show Completed Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = showCompleted,
                onCheckedChange = onShowCompletedChanged
            )
            Spacer(Modifier.width(8.dp))
            Text("Hiện nhiệm vụ đã hoàn thành")
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Priority Filters
        Text(
            text = "Mức độ ưu tiên",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Priority.entries.toTypedArray()) { priority ->
                FilterChip(
                    onClick = { 
                        onPrioritySelected(
                            if (selectedPriority == priority) null else priority
                        )
                    },
                    label = { Text(priority.displayName) },
                    selected = selectedPriority == priority,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(priority.color).copy(alpha = 0.3f),
                        selectedLabelColor = Color(priority.color)
                    )
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Category Filters
        Text(
            text = "Danh mục",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Category.entries.toTypedArray()) { category ->
                FilterChip(
                    onClick = { 
                        onCategorySelected(
                            if (selectedCategory == category) null else category
                        )
                    },
                    label = { 
                        Text("${category.icon} ${category.displayName}") 
                    },
                    selected = selectedCategory == category,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(category.color).copy(alpha = 0.3f),
                        selectedLabelColor = Color(category.color)
                    )
                )
            }
        }
    }
}