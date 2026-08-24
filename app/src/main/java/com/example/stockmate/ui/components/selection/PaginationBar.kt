package com.example.stockmate.ui.components.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PaginationBar(
    currentPage: Int,
    visiblePages: List<Int>,
    onPageSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        visiblePages.forEachIndexed { index, page ->
            if (index > 0) {
                val previousPage = visiblePages[index - 1]
                if (page - previousPage > 1) {
                    Text(
                        text = "...",
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            TextButton(
                onClick = {
                    onPageSelected(page)
                }
            ) {
                Text(
                    text = (page + 1).toString(),
                    style = if (page == currentPage) {
                        MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        MaterialTheme.typography.labelLarge
                    }
                )
            }
        }
    }
}