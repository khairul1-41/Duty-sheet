package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DutyEntry
import com.example.ui.SalaryViewModel

@Composable
fun EditEntryDialog(
    entry: DutyEntry,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (DutyEntry) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    var dateStr by remember { mutableStateOf(entry.dateStr) }
    var advanceText by remember {
        mutableStateOf(if (entry.advanceAmount > 0) entry.advanceAmount.toLong().toString() else "")
    }
    var travelDescription by remember { mutableStateOf(entry.travelDescription) }
    var travelAmountText by remember {
        mutableStateOf(if (entry.travelAmount > 0) entry.travelAmount.toLong().toString() else "")
    }
    var workPlace by remember { mutableStateOf(entry.workPlace) }
    var dutyType by remember { mutableStateOf(entry.dutyType) }
    var dutyUnitsText by remember { mutableStateOf(entry.dutyUnits.toString()) }
    var otherNote by remember { mutableStateOf(entry.otherNote) }

    val dutyPresets = listOf(
        "Day" to 1.0,
        "Day + Half Night" to 1.5,
        "Night" to 1.0,
        "stay Home" to 0.0,
        "Off" to 0.0,
        "Half Day" to 0.5
    )

    val workPlaceSuggestions = listOf(
        "Tarabo Ghat",
        "Habib Ghat",
        "New Rupshi Ghat",
        "City Mill",
        "Central Depot"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (entry.id == 0L) "Add Daily Duty" else "Edit Duty Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (entry.id != 0L && onDelete != null) {
                    IconButton(
                        onClick = { onDelete(entry.id) },
                        modifier = Modifier.testTag("btn_delete_entry")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete entry",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Date & Advance Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        label = { Text("Date") },
                        placeholder = { Text("01/09/2026") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("input_entry_date")
                    )

                    OutlinedTextField(
                        value = advanceText,
                        onValueChange = { advanceText = it },
                        label = { Text("Advance ($currency)") },
                        placeholder = { Text("1000") },
                        leadingIcon = { Icon(Icons.Default.MoneyOff, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_entry_advance")
                    )
                }

                // Duty Type Selection Chips
                Text(
                    text = "Duty Status / Shift",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dutyPresets.forEach { (type, units) ->
                        FilterChip(
                            selected = dutyType.equals(type, ignoreCase = true),
                            onClick = {
                                dutyType = type
                                dutyUnitsText = units.toString()
                                if (type == "stay Home" || type == "Off") {
                                    workPlace = ""
                                    travelDescription = ""
                                    travelAmountText = "0"
                                }
                            },
                            label = { Text(type) }
                        )
                    }
                }

                // Duty Units (Multiplier)
                OutlinedTextField(
                    value = dutyUnitsText,
                    onValueChange = { dutyUnitsText = it },
                    label = { Text("Duty Equivalent Units") },
                    placeholder = { Text("1.0 (or 1.5 for Day+Half Night)") },
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Work Place
                OutlinedTextField(
                    value = workPlace,
                    onValueChange = { workPlace = it },
                    label = { Text("Work Place") },
                    placeholder = { Text("e.g. Tarabo Ghat / Habib Ghat") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_entry_workplace")
                )

                // Work Place Quick Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    workPlaceSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = {
                                workPlace = suggestion
                                if (travelDescription.isBlank()) {
                                    travelDescription = "Home to $suggestion-40 tk"
                                    travelAmountText = "40"
                                }
                            },
                            label = { Text(suggestion, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                // Travel Description
                OutlinedTextField(
                    value = travelDescription,
                    onValueChange = { newDesc ->
                        travelDescription = newDesc
                        val autoAmount = SalaryViewModel.extractTravelAmount(newDesc)
                        if (autoAmount > 0) {
                            travelAmountText = autoAmount.toLong().toString()
                        }
                    },
                    label = { Text("Travel Description") },
                    placeholder = { Text("e.g. Home to Tarabo Ghat-80 Tk") },
                    leadingIcon = { Icon(Icons.Default.DirectionsTransit, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_entry_travel_desc")
                )

                // Travel Amount
                OutlinedTextField(
                    value = travelAmountText,
                    onValueChange = { travelAmountText = it },
                    label = { Text("Travel Allowance ($currency)") },
                    placeholder = { Text("80") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_entry_travel_amount")
                )

                // Other Note
                OutlinedTextField(
                    value = otherNote,
                    onValueChange = { otherNote = it },
                    label = { Text("Other Remarks / Notes") },
                    placeholder = { Text("Optional notes") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val adv = advanceText.toDoubleOrNull() ?: 0.0
                    val travelAmt = travelAmountText.toDoubleOrNull() ?: 0.0
                    val units = dutyUnitsText.toDoubleOrNull() ?: if (dutyType == "Day + Half Night") 1.5 else 1.0

                    val updated = entry.copy(
                        dateStr = dateStr.trim(),
                        advanceAmount = adv,
                        travelDescription = travelDescription.trim(),
                        travelAmount = travelAmt,
                        workPlace = workPlace.trim(),
                        dutyType = dutyType.trim(),
                        dutyUnits = units,
                        otherNote = otherNote.trim()
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("btn_save_entry")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
