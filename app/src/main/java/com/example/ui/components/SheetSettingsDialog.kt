package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.SalarySheet

@Composable
fun SheetSettingsDialog(
    sheet: SalarySheet,
    onDismiss: () -> Unit,
    onSave: (SalarySheet) -> Unit
) {
    var employeeName by remember { mutableStateOf(sheet.employeeName) }
    var monthYear by remember { mutableStateOf(sheet.monthYear) }
    var accountDetails by remember { mutableStateOf(sheet.accountDetails) }
    var companyName by remember { mutableStateOf(sheet.companyName) }
    var dailyRateText by remember { mutableStateOf(sheet.dailyRate.toLong().toString()) }
    var otherAllowancesText by remember {
        mutableStateOf(if (sheet.otherAllowances > 0) sheet.otherAllowances.toLong().toString() else "0")
    }
    var otherDeductionsText by remember {
        mutableStateOf(if (sheet.otherDeductions > 0) sheet.otherDeductions.toLong().toString() else "0")
    }
    var currency by remember { mutableStateOf(sheet.currency) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sheet & Salary Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = employeeName,
                    onValueChange = { employeeName = it },
                    label = { Text("Employee Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("Month & Year") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accountDetails,
                    onValueChange = { accountDetails = it },
                    label = { Text("Account Details (e.g. BKASH)") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dailyRateText,
                        onValueChange = { dailyRateText = it },
                        label = { Text("Duty Rate") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Currency") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = otherAllowancesText,
                        onValueChange = { otherAllowancesText = it },
                        label = { Text("Bonus/Allowance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = otherDeductionsText,
                        onValueChange = { otherDeductionsText = it },
                        label = { Text("Other Deductions") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = dailyRateText.toDoubleOrNull() ?: sheet.dailyRate
                    val allowances = otherAllowancesText.toDoubleOrNull() ?: 0.0
                    val deductions = otherDeductionsText.toDoubleOrNull() ?: 0.0

                    onSave(
                        sheet.copy(
                            employeeName = employeeName.trim().ifBlank { sheet.employeeName },
                            monthYear = monthYear.trim().ifBlank { sheet.monthYear },
                            accountDetails = accountDetails.trim(),
                            companyName = companyName.trim(),
                            dailyRate = rate,
                            otherAllowances = allowances,
                            otherDeductions = deductions,
                            currency = currency.trim().ifBlank { "Tk" }
                        )
                    )
                },
                modifier = Modifier.testTag("btn_save_sheet_settings")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
