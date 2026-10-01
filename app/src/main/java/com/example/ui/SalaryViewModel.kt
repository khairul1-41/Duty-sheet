package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.DutyEntry
import com.example.data.model.SalarySheet
import com.example.data.model.SalarySheetWithEntries
import com.example.pdf.PdfExporter
import com.example.pdf.PdfSalarySheetGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SalaryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = database.salarySheetDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedSheetId = MutableStateFlow<Long?>(null)
    val selectedSheetId: StateFlow<Long?> = _selectedSheetId

    val allSheets: StateFlow<List<SalarySheetWithEntries>> = dao.getAllSheetsWithEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSheets: StateFlow<List<SalarySheetWithEntries>> = combine(allSheets, _searchQuery) { sheets, query ->
        if (query.isBlank()) {
            sheets
        } else {
            val q = query.trim().lowercase(Locale.getDefault())
            sheets.filter {
                it.sheet.employeeName.lowercase(Locale.getDefault()).contains(q) ||
                        it.sheet.monthYear.lowercase(Locale.getDefault()).contains(q) ||
                        it.sheet.accountDetails.lowercase(Locale.getDefault()).contains(q) ||
                        it.sheet.companyName.lowercase(Locale.getDefault()).contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedSheet: StateFlow<SalarySheetWithEntries?> = combine(allSheets, _selectedSheetId) { sheets, id ->
        if (id == null) {
            sheets.firstOrNull()
        } else {
            sheets.find { it.sheet.id == id } ?: sheets.firstOrNull()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // PDF State
    private val _isGeneratingPdf = MutableStateFlow(false)
    val isGeneratingPdf: StateFlow<Boolean> = _isGeneratingPdf

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile

    private val _pdfBitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val pdfBitmaps: StateFlow<List<Bitmap>> = _pdfBitmaps

    init {
        // Automatically check if database is empty and pre-populate if needed
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.getSheetsCount() == 0) {
                AppDatabase.getDatabase(getApplication(), viewModelScope)
                val sheet = SalarySheet(
                    employeeName = "KHAIRUL",
                    monthYear = "SEPTEMBER (2026)",
                    accountDetails = "01920235578 (BKASH)",
                    companyName = "Logistics & Transport Fleet",
                    dailyRate = 800.0,
                    dayAndHalfNightMultiplier = 1.5,
                    currency = "Tk"
                )
                val sheetId = dao.insertSheet(sheet)

                val entries = listOf(
                    DutyEntry(sheetId = sheetId, dateStr = "01/09/2026", advanceAmount = 0.0, travelDescription = "Home to Tarabo Ghat-80 Tk", travelAmount = 80.0, workPlace = "Tarabo Ghat", dutyType = "Day + Half Night", dutyUnits = 1.5, sortOrder = 1),
                    DutyEntry(sheetId = sheetId, dateStr = "02/09/2026", advanceAmount = 1000.0, travelDescription = "Home to Tarabo Ghat-80 Tk", travelAmount = 80.0, workPlace = "Tarabo Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 2),
                    DutyEntry(sheetId = sheetId, dateStr = "03/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 3),
                    DutyEntry(sheetId = sheetId, dateStr = "04/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 4),
                    DutyEntry(sheetId = sheetId, dateStr = "05/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day + Half Night", dutyUnits = 1.5, sortOrder = 5),
                    DutyEntry(sheetId = sheetId, dateStr = "06/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day + Half Night", dutyUnits = 1.5, sortOrder = 6),
                    DutyEntry(sheetId = sheetId, dateStr = "07/09/2026", advanceAmount = 1000.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 7),
                    DutyEntry(sheetId = sheetId, dateStr = "08/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 8),
                    DutyEntry(sheetId = sheetId, dateStr = "09/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 9),
                    DutyEntry(sheetId = sheetId, dateStr = "10/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 10),
                    DutyEntry(sheetId = sheetId, dateStr = "11/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 11),
                    DutyEntry(sheetId = sheetId, dateStr = "12/09/2026", advanceAmount = 1000.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 12),
                    DutyEntry(sheetId = sheetId, dateStr = "13/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 13),
                    DutyEntry(sheetId = sheetId, dateStr = "14/09/2026", advanceAmount = 0.0, travelDescription = "Home to city mill-20Tk", travelAmount = 20.0, workPlace = "City Mill", dutyType = "Day", dutyUnits = 1.0, sortOrder = 14),
                    DutyEntry(sheetId = sheetId, dateStr = "15/09/2026", advanceAmount = 0.0, travelDescription = "Home to Habib Ghat-40 tk", travelAmount = 40.0, workPlace = "Habib Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 15),
                    DutyEntry(sheetId = sheetId, dateStr = "16/09/2026", advanceAmount = 0.0, travelDescription = "Home to New Rupshi-40 tk", travelAmount = 40.0, workPlace = "New Rupshi Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 16),
                    DutyEntry(sheetId = sheetId, dateStr = "17/09/2026", advanceAmount = 1500.0, travelDescription = "Home to New Rupshi-40tk", travelAmount = 40.0, workPlace = "New Rupshi Ghat", dutyType = "Day", dutyUnits = 1.0, sortOrder = 17),
                    DutyEntry(sheetId = sheetId, dateStr = "18/09/2026", advanceAmount = 0.0, travelDescription = "", travelAmount = 0.0, workPlace = "", dutyType = "stay Home", dutyUnits = 0.0, sortOrder = 18)
                )
                dao.insertEntries(entries)
                _selectedSheetId.value = sheetId
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSheet(sheetId: Long) {
        _selectedSheetId.value = sheetId
    }

    fun createNewSheet(
        employeeName: String,
        monthYear: String,
        accountDetails: String,
        companyName: String,
        dailyRate: Double,
        dayAndHalfNightMultiplier: Double,
        currency: String,
        autoPopulateMonthDays: Boolean = true,
        monthNumber: Int = 10,
        yearNumber: Int = 2026
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sheet = SalarySheet(
                employeeName = employeeName.trim().ifBlank { "EMPLOYEE" },
                monthYear = monthYear.trim().ifBlank { "OCTOBER (2026)" },
                accountDetails = accountDetails.trim(),
                companyName = companyName.trim(),
                dailyRate = dailyRate,
                dayAndHalfNightMultiplier = dayAndHalfNightMultiplier,
                currency = currency.ifBlank { "Tk" }
            )
            val newSheetId = dao.insertSheet(sheet)

            if (autoPopulateMonthDays) {
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, yearNumber)
                cal.set(Calendar.MONTH, monthNumber - 1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                val entries = mutableListOf<DutyEntry>()
                val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                for (day in 1..daysInMonth) {
                    cal.set(Calendar.DAY_OF_MONTH, day)
                    val dateFormatted = df.format(cal.time)
                    entries.add(
                        DutyEntry(
                            sheetId = newSheetId,
                            dateStr = dateFormatted,
                            advanceAmount = 0.0,
                            travelDescription = "",
                            travelAmount = 0.0,
                            workPlace = "",
                            dutyType = "Day",
                            dutyUnits = 1.0,
                            sortOrder = day
                        )
                    )
                }
                dao.insertEntries(entries)
            }

            _selectedSheetId.value = newSheetId
        }
    }

    fun updateSheetDetails(sheet: SalarySheet) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateSheet(sheet.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteSheet(sheetId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteSheetById(sheetId)
            if (_selectedSheetId.value == sheetId) {
                _selectedSheetId.value = null
            }
        }
    }

    fun duplicateSheet(source: SalarySheetWithEntries, newMonthYear: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newSheet = source.sheet.copy(
                id = 0,
                monthYear = newMonthYear,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val newId = dao.insertSheet(newSheet)
            val newEntries = source.sortedEntries.map { entry ->
                entry.copy(
                    id = 0,
                    sheetId = newId,
                    advanceAmount = 0.0 // reset advances for the new month
                )
            }
            dao.insertEntries(newEntries)
            _selectedSheetId.value = newId
        }
    }

    fun saveEntry(entry: DutyEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            if (entry.id == 0L) {
                dao.insertEntry(entry)
            } else {
                dao.updateEntry(entry)
            }
            // update timestamp of parent sheet
            val sheet = dao.getSheetWithEntriesByIdSync(entry.sheetId)?.sheet
            if (sheet != null) {
                dao.updateSheet(sheet.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun deleteEntry(entryId: Long, sheetId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteEntryById(entryId)
            val sheet = dao.getSheetWithEntriesByIdSync(sheetId)?.sheet
            if (sheet != null) {
                dao.updateSheet(sheet.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun addDaysToSheet(sheetId: Long, count: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = dao.getSheetWithEntriesByIdSync(sheetId) ?: return@launch
            val nextOrder = (current.entries.maxOfOrNull { it.sortOrder } ?: 0) + 1
            val newEntry = DutyEntry(
                sheetId = sheetId,
                dateStr = "",
                advanceAmount = 0.0,
                travelDescription = "",
                travelAmount = 0.0,
                workPlace = current.entries.lastOrNull()?.workPlace ?: "",
                dutyType = "Day",
                dutyUnits = 1.0,
                sortOrder = nextOrder
            )
            dao.insertEntry(newEntry)
        }
    }

    fun generatePdfForCurrentSheet(sheetWithEntries: SalarySheetWithEntries) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                val file = withContext(Dispatchers.IO) {
                    PdfSalarySheetGenerator.generatePdf(getApplication(), sheetWithEntries)
                }
                val bitmaps = withContext(Dispatchers.IO) {
                    PdfExporter.renderPdfToBitmaps(file)
                }
                _generatedPdfFile.value = file
                _pdfBitmaps.value = bitmaps
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    companion object {
        fun extractTravelAmount(text: String): Double {
            if (text.isBlank()) return 0.0
            val regex = """(\d+(?:\.\d+)?)\s*(?:tk|Tk|TK|/-)""".toRegex()
            val match = regex.find(text)
            if (match != null) {
                return match.groupValues[1].toDoubleOrNull() ?: 0.0
            }
            // fallback: any number preceded by hyphen or space
            val fallbackRegex = """[-:]\s*(\d+(?:\.\d+)?)""".toRegex()
            val fallbackMatch = fallbackRegex.find(text)
            if (fallbackMatch != null) {
                return fallbackMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            }
            return 0.0
        }
    }
}
