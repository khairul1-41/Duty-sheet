package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DutyEntry
import com.example.data.model.SalarySheetWithEntries
import com.example.ui.SalaryViewModel
import com.example.ui.components.EditEntryDialog
import com.example.ui.components.SheetSettingsDialog
import com.example.ui.theme.TableHeaderCyan
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetDetailScreen(
    viewModel: SalaryViewModel,
    onBack: () -> Unit,
    onConvertToPdf: (SalarySheetWithEntries) -> Unit
) {
    BackHandler { onBack() }

    val sheetData by viewModel.selectedSheet.collectAsStateWithLifecycle()

    var editingEntry by remember { mutableStateOf<DutyEntry?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val decimalFormat = remember { DecimalFormat("#,##0.##") }
    val currencyFormat = remember { DecimalFormat("#,##0") }

    if (sheetData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sheet not found")
        }
        return
    }

    val currentSheet = sheetData!!
    val entries = currentSheet.sortedEntries

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentSheet.sheet.employeeName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSheet.sheet.monthYear,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_detail")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("btn_sheet_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(
                        onClick = { onConvertToPdf(currentSheet) },
                        modifier = Modifier.testTag("btn_top_convert_pdf")
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = "Convert to PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val nextSort = (entries.maxOfOrNull { it.sortOrder } ?: 0) + 1
                            editingEntry = DutyEntry(
                                sheetId = currentSheet.sheet.id,
                                dateStr = "",
                                advanceAmount = 0.0,
                                travelDescription = "",
                                travelAmount = 0.0,
                                workPlace = entries.lastOrNull()?.workPlace ?: "",
                                dutyType = "Day",
                                dutyUnits = 1.0,
                                sortOrder = nextSort
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_add_duty_row")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Row")
                    }

                    Button(
                        onClick = { onConvertToPdf(currentSheet) },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_bottom_convert_pdf"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Convert to PDF")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Info Box (matches prompt document header)
            item {
                HeaderInfoCard(
                    employeeName = currentSheet.sheet.employeeName,
                    monthYear = currentSheet.sheet.monthYear,
                    account = currentSheet.sheet.accountDetails,
                    dailyRate = currentSheet.sheet.dailyRate,
                    currency = currentSheet.sheet.currency,
                    onEditClick = { showSettingsDialog = true }
                )
            }

            // Real-time Calculations KPI Bar
            item {
                CalculationKpiCard(
                    data = currentSheet,
                    currencyFormat = currencyFormat,
                    decimalFormat = decimalFormat
                )
            }

            // Table Header Title & Instruction
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Duty & Advance Records (${entries.size} days)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap row to edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Table Content (Horizontal Scrollable Table)
            item {
                DutyTableComponent(
                    entries = entries,
                    currency = currentSheet.sheet.currency,
                    decimalFormat = decimalFormat,
                    onRowClick = { entry -> editingEntry = entry }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialogs
    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            currency = currentSheet.sheet.currency,
            onDismiss = { editingEntry = null },
            onSave = { updatedEntry ->
                viewModel.saveEntry(updatedEntry)
                editingEntry = null
            },
            onDelete = { entryId ->
                viewModel.deleteEntry(entryId, currentSheet.sheet.id)
                editingEntry = null
            }
        )
    }

    if (showSettingsDialog) {
        SheetSettingsDialog(
            sheet = currentSheet.sheet,
            onDismiss = { showSettingsDialog = false },
            onSave = { updatedSheet ->
                viewModel.updateSheetDetails(updatedSheet)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
private fun HeaderInfoCard(
    employeeName: String,
    monthYear: String,
    account: String,
    dailyRate: Double,
    currency: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NAME: $employeeName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Details",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "NAME OF MONTH: $monthYear",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (account.isNotBlank()) {
                Text(
                    text = "ACCOUNT: $account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "DUTY RATE: ${dailyRate.toLong()} $currency / duty",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalculationKpiCard(
    data: SalarySheetWithEntries,
    currencyFormat: DecimalFormat,
    decimalFormat: DecimalFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Working Duties",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${decimalFormat.format(data.totalDutyUnits)} duties (${data.workingDaysCount} days)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Duty Earnings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${currencyFormat.format(data.dutyEarnings)} ${data.sheet.currency}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Advance Deductions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "-${currencyFormat.format(data.totalAdvance)} ${data.sheet.currency}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Travel Allowance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "+${currencyFormat.format(data.totalTravel)} ${data.sheet.currency}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Prominent Net Payable Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NET PAYABLE SALARY",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "${currencyFormat.format(data.netPayable)} ${data.sheet.currency}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun DutyTableComponent(
    entries: List<DutyEntry>,
    currency: String,
    decimalFormat: DecimalFormat,
    onRowClick: (DutyEntry) -> Unit
) {
    val scrollState = rememberScrollState()

    // Table Column Widths in dp (Total: ~580dp)
    val widthDate = 90.dp
    val widthAdvance = 80.dp
    val widthTravel = 190.dp
    val widthWorkPlace = 120.dp
    val widthDuty = 120.dp
    val widthOther = 80.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            // Table Header Row with Cyan Background
            Row(
                modifier = Modifier
                    .background(TableHeaderCyan)
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell(text = "DATE", width = widthDate, isHeader = true)
                TableCell(text = "ADVANCE", width = widthAdvance, isHeader = true)
                TableCell(text = "TRAVEL", width = widthTravel, isHeader = true)
                TableCell(text = "WORK PLACE", width = widthWorkPlace, isHeader = true)
                TableCell(text = "DUTY", width = widthDuty, isHeader = true)
                TableCell(text = "OTHER", width = widthOther, isHeader = true)
            }

            // Table Data Rows
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No duty entries yet. Tap 'Add Row' below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                entries.forEachIndexed { index, entry ->
                    val isOffDay = entry.dutyType.equals("stay Home", true) || entry.dutyType.equals("Off", true)
                    val rowBg = when {
                        isOffDay -> Color(0xFFFFF3E0) // Warm tint for stay home
                        index % 2 == 1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Row(
                        modifier = Modifier
                            .background(rowBg)
                            .clickable { onRowClick(entry) }
                            .padding(vertical = 8.dp)
                            .testTag("table_row_${entry.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(text = entry.dateStr, width = widthDate)
                        TableCell(
                            text = if (entry.advanceAmount > 0) decimalFormat.format(entry.advanceAmount) else "",
                            width = widthAdvance,
                            textColor = if (entry.advanceAmount > 0) MaterialTheme.colorScheme.error else Color.Unspecified,
                            isBold = entry.advanceAmount > 0
                        )
                        TableCell(text = entry.travelDescription, width = widthTravel, alignLeft = true)
                        TableCell(text = entry.workPlace, width = widthWorkPlace)
                        TableCell(
                            text = entry.dutyType,
                            width = widthDuty,
                            isBold = entry.dutyType.contains("Night", true) || isOffDay,
                            textColor = if (isOffDay) Color(0xFFE65100) else Color.Unspecified
                        )
                        TableCell(text = entry.otherNote, width = widthOther, alignLeft = true)
                    }

                    // Row separator
                    Box(
                        modifier = Modifier
                            .width(widthDate + widthAdvance + widthTravel + widthWorkPlace + widthDuty + widthOther)
                            .height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    alignLeft: Boolean = false,
    isBold: Boolean = false,
    textColor: Color = Color.Unspecified
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = if (alignLeft) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = text,
            style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isHeader) FontWeight.ExtraBold else if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) Color.Black else textColor,
            textAlign = if (alignLeft) TextAlign.Start else TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
