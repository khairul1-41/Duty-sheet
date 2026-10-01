package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "salary_sheets")
data class SalarySheet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeName: String,
    val monthYear: String,
    val accountDetails: String,
    val companyName: String = "",
    val dailyRate: Double = 600.0,
    val dayAndHalfNightMultiplier: Double = 1.5,
    val nightMultiplier: Double = 1.0,
    val isDailyRateMode: Boolean = true,
    val fixedMonthlySalary: Double = 0.0,
    val otherAllowances: Double = 0.0,
    val otherDeductions: Double = 0.0,
    val currency: String = "Tk",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "duty_entries",
    foreignKeys = [
        ForeignKey(
            entity = SalarySheet::class,
            parentColumns = ["id"],
            childColumns = ["sheetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sheetId"])]
)
data class DutyEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sheetId: Long,
    val dateStr: String,
    val advanceAmount: Double = 0.0,
    val travelDescription: String = "",
    val travelAmount: Double = 0.0,
    val workPlace: String = "",
    val dutyType: String = "Day",
    val dutyUnits: Double = 1.0,
    val otherNote: String = "",
    val sortOrder: Int = 0
)

data class SalarySheetWithEntries(
    @Embedded val sheet: SalarySheet,
    @Relation(
        parentColumn = "id",
        entityColumn = "sheetId"
    )
    val entries: List<DutyEntry>
) {
    val sortedEntries: List<DutyEntry>
        get() = entries.sortedWith(compareBy({ it.sortOrder }, { it.id }))

    val totalAdvance: Double
        get() = entries.sumOf { it.advanceAmount }

    val totalTravel: Double
        get() = entries.sumOf { it.travelAmount }

    val totalDutyUnits: Double
        get() = entries.sumOf {
            if (it.dutyType.equals("stay Home", ignoreCase = true) || it.dutyType.equals("Off", ignoreCase = true)) 0.0
            else it.dutyUnits
        }

    val workingDaysCount: Int
        get() = entries.count {
            !it.dutyType.equals("stay Home", ignoreCase = true) && !it.dutyType.equals("Off", ignoreCase = true) && it.dutyUnits > 0.0
        }

    val stayHomeDaysCount: Int
        get() = entries.count {
            it.dutyType.equals("stay Home", ignoreCase = true) || it.dutyType.equals("Off", ignoreCase = true)
        }

    val dutyEarnings: Double
        get() = if (sheet.isDailyRateMode) {
            totalDutyUnits * sheet.dailyRate
        } else {
            val totalMonthDays = if (entries.isNotEmpty()) entries.size else 30
            val perDay = if (totalMonthDays > 0) sheet.fixedMonthlySalary / totalMonthDays else 0.0
            maxOf(0.0, sheet.fixedMonthlySalary - (stayHomeDaysCount * perDay))
        }

    val grossEarnings: Double
        get() = dutyEarnings + totalTravel + sheet.otherAllowances

    val totalDeductions: Double
        get() = totalAdvance + sheet.otherDeductions

    val netPayable: Double
        get() = grossEarnings - totalDeductions
}
