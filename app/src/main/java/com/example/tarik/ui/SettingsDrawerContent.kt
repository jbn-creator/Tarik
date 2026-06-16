package com.example.tarik.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch



private val CATEGORY_LIST = listOf("War", "Science", "Politics", "Sports", "Religion", "General")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDrawerContent(
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    //all our settings are observed as states
    val darkMode by settingsViewModel.darkMode.collectAsState()
    val notificationTime by settingsViewModel.notificationTime.collectAsState()
    val categoryWeights by settingsViewModel.categoryWeights.collectAsState()


    var showTimePicker by remember { mutableStateOf(false) }

    // snackbar feedback when a slider settles to confirm when a change has been made
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            Text(
                text = "Tarik",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ታሪክ — Amharic for \"History\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // writes straight to datastore through the viewmodel and the entire app theme rebuilds
            // on change because Mainactivity wraps Tariktheme(darkTheme = darkMode) around the whole UI
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dark Mode",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = darkMode,
                    onCheckedChange = { settingsViewModel.setDarkMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Daily Notification Time",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(notificationTime)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // every category has its own weight slider, adjusting these weights allows users to have control over their feed.
            // as explained in other files, a higher weight means that that particular category appears closer to the top of your feed.
            Text(
                text = "Category Weights",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Higher weight = appears further up your feed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            CATEGORY_LIST.forEach { category ->
                val weight = categoryWeights[category] ?: 0.5f
                CategoryWeightSlider(
                    category = category,
                    weight = weight,
                    onWeightChange = { newWeight ->
                        settingsViewModel.setCategoryWeight(category, newWeight)
                    },
                    onWeightSettled = {
                        // dismiss any in flight snackbar before showing a new one otherwise rapid slider releases will
                        // queue up confirmations
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = "Feed re-sorted by your preferences",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "About Tarik",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tarik (ታሪክ) means \"History\" in Amharic. " +
                        "This app brings you daily historical facts, " +
                        "personalised to your interests, helping you discover " +
                        "the fascinating stories that shaped our world.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(80.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // dialog rendered outside the Column so it overlays the drawer correctly
    if (showTimePicker) {
        TarikTimePickerDialog(
            initialTime = notificationTime,
            onConfirm = { newTime ->
                settingsViewModel.setNotificationTime(newTime)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}


// since every category has its own slider we used a composable for the slider function
@Composable
private fun CategoryWeightSlider(
    category: String,
    weight: Float,
    onWeightChange: (Float) -> Unit,
    onWeightSettled: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        // label on the left, percentage readout on the right
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(weight * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = weight,
            onValueChange = onWeightChange,
            onValueChangeFinished = onWeightSettled,
            valueRange = 0f..1f
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarikTimePickerDialog(
    initialTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // defaults to 09:00 if the stored value is malformed for any reason
    val parts = initialTime.split(":").mapNotNull { it.toIntOrNull() }
    val initialHour = parts.getOrNull(0) ?: 9
    val initialMinute = parts.getOrNull(1) ?: 0

    // rememberTimePickerState survives rotation natively so the user doesn't lose their pick if the device rotates mid-selection
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // format back into HH:mm
                val newTime = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                onConfirm(newTime)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
