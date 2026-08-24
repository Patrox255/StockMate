package com.example.stockmate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.stockmate.ui.components.GenericSmallErrorMessage

@Composable
fun GenericSmallErrorsList(
    errorMessages: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        errorMessages.forEach { error ->
            GenericSmallErrorMessage(
                errorMessage = error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}