package com.example.stockmate.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults.itemShape
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

data class TabbedComponentEntry(
    val title: String,
    val content: @Composable () -> Unit
)

@Composable
fun TabbedComponent(
    entries: List<TabbedComponentEntry>,
    header: String? = null
) {
    if (entries.isEmpty()) {
        throw Error("TabbedComponent must have at least one entry")
    }

    var selectedIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (header != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Preview Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = header,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            entries.forEachIndexed { index, entry ->
                SegmentedButton(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    shape = itemShape(index, count=entries.size)
                ) {
                    Text(entry.title)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Crossfade(
        targetState = selectedIndex,
        label = "TabTransition"
    ) { targetIndex ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.4f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            entries[targetIndex].content()
        }
    }
}