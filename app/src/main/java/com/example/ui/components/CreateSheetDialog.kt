package com.example.ui.components

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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CreateSheetDialog(
    onDismiss: () -> Unit,
    onCreate: (
        employeeName: String,
        monthYear: String,
        accountDetails: String,
        companyName: String,
        dailyRate: Double,
        dayAndHalfNightMultiplier: Double,
        currency: String,
        autoPopulateDays: Boolean,
        monthNumber: Int,
        yearNumber: Int
    ) -> Unit
) {
    val cal = remember { Calendar.getInstance() }
    val currentMonthName = remember {
        SimpleDateFormat("MMMM (yyyy)", Locale.getDefault()).format(cal.time).uppercase(Locale.getDefault())
    }

    var employeeName by remember { mutableStateOf("") }
    var monthYear by remember { mutableStateOf(currentMonthName) }
    var accountDetails by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("Logistics & Transport") }
    var dailyRateText by remember { mutableStateOf("800") }
    var currency by remember { mutableStateOf("Tk") }
    var autoPopulateDays by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Salary Sheet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Configure employee details and monthly duty format.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Employee Name
                OutlinedTextField(
                    value = employeeName,
                    onValueChange = { employeeName = it },
                    label = { Text("Employee Name *") },
                    placeholder = { Text("e.g. KHAIRUL") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_employee_name")
                )

                // Month & Year
                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("Month & Year *") },
                    placeholder = { Text("e.g. SEPTEMBER (2026)") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_month_year")
                )

                // Account / Payment Info
                OutlinedTextField(
                    value = accountDetails,
                    onValueChange = { accountDetails = it },
                    label = { Text("Account / Payment Details *") },
                    placeholder = { Text("e.g. 01920235578 (BKASH)") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_details")
                )

                // Company Name
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company / Organization") },
                    placeholder = { Text("e.g. Tarabo Logistics") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Daily Rate & Currency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dailyRateText,
                        onValueChange = { dailyRateText = it },
                        label = { Text("Daily Duty Rate") },
                        placeholder = { Text("800") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_daily_rate")
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Currency") },
                        singleLine = true,
                        modifier = Modifier.width(90.dp)
                    )
                }

                // Checkbox for Auto-populating Days
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autoPopulateDays,
                        onCheckedChange = { autoPopulateDays = it },
                        modifier = Modifier.testTag("checkbox_auto_days")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Auto-create month calendar days (Day 1 - 30/31)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = dailyRateText.toDoubleOrNull() ?: 800.0
                    val monthNum = cal.get(Calendar.MONTH) + 1
                    val yearNum = cal.get(Calendar.YEAR)
                    onCreate(
                        employeeName.ifBlank { "EMPLOYEE" },
                        monthYear.ifBlank { currentMonthName },
                        accountDetails,
                        companyName,
                        rate,
                        1.5,
                        currency.ifBlank { "Tk" },
                        autoPopulateDays,
                        monthNum,
                        yearNum
                    )
                },
                modifier = Modifier.testTag("btn_confirm_create_sheet")
            ) {
                Text("Create Sheet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
