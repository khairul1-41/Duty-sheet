package com.example

import com.example.data.model.DutyEntry
import com.example.data.model.SalarySheet
import com.example.data.model.SalarySheetWithEntries
import com.example.ui.SalaryViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testTravelAmountExtraction() {
        assertEquals(80.0, SalaryViewModel.extractTravelAmount("Home to Tarabo Ghat-80 Tk"), 0.01)
        assertEquals(20.0, SalaryViewModel.extractTravelAmount("Home to city mill-20Tk"), 0.01)
        assertEquals(40.0, SalaryViewModel.extractTravelAmount("Home to Habib Ghat-40 tk"), 0.01)
        assertEquals(0.0, SalaryViewModel.extractTravelAmount("stay Home"), 0.01)
    }

    @Test
    fun testSalaryCalculations() {
        val sheet = SalarySheet(
            id = 1,
            employeeName = "KHAIRUL",
            monthYear = "SEPTEMBER (2026)",
            accountDetails = "01920235578 (BKASH)",
            dailyRate = 800.0,
            currency = "Tk"
        )
        val entries = listOf(
            DutyEntry(sheetId = 1, dateStr = "01/09/2026", advanceAmount = 0.0, travelAmount = 80.0, dutyType = "Day + Half Night", dutyUnits = 1.5),
            DutyEntry(sheetId = 1, dateStr = "02/09/2026", advanceAmount = 1000.0, travelAmount = 80.0, dutyType = "Day", dutyUnits = 1.0),
            DutyEntry(sheetId = 1, dateStr = "03/09/2026", advanceAmount = 0.0, travelAmount = 0.0, dutyType = "stay Home", dutyUnits = 0.0)
        )
        val data = SalarySheetWithEntries(sheet, entries)

        assertEquals(2.5, data.totalDutyUnits, 0.01)
        assertEquals(2, data.workingDaysCount)
        assertEquals(1, data.stayHomeDaysCount)
        assertEquals(160.0, data.totalTravel, 0.01)
        assertEquals(1000.0, data.totalAdvance, 0.01)

        // 2.5 * 800 = 2000 duty earnings
        assertEquals(2000.0, data.dutyEarnings, 0.01)
        // 2000 + 160 = 2160 gross
        assertEquals(2160.0, data.grossEarnings, 0.01)
        // 2160 - 1000 advance = 1160 net
        assertEquals(1160.0, data.netPayable, 0.01)
    }
}
