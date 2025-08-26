package com.hackathon.dinemate.questionnaire
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedOptionsViewer(
    modifier: Modifier = Modifier,
    title: String = "Selected preferences"
) {
    val items by SelectedOptionsStore.items.collectAsState()


    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.height(8.dp))


    if (items.isEmpty()) {
        Text(
            text = "Nothing selected yet.",
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }


    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { label ->
            Surface(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .height(36.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}