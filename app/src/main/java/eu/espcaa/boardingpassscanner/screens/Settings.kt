package eu.espcaa.boardingpassscanner.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            SettingsSection(
                title = "Data",
                items = listOf(
                    { Text("Language") },
                    { Text("Theme") },
                    { Text("Notifications") }
                )
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, items: List<@Composable () -> Unit>) {
    Column {
        // create a card for each and round more the outer ones
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Column(
            modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
        ) {
            items.forEachIndexed { index, item ->
                Surface(
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 16.dp else 4.dp,
                        topEnd = if (index == 0) 16.dp else 4.dp,
                        bottomStart = if (index == items.size - 1) 16.dp else 4.dp,
                        bottomEnd = if (index == items.size - 1) 16.dp else 4.dp
                    ),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        item()
                    }
                }
            }
        }
    }
}
