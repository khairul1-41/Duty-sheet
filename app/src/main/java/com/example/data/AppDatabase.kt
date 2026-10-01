package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.SalarySheetDao
import com.example.data.model.DutyEntry
import com.example.data.model.SalarySheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SalarySheet::class, DutyEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun salarySheetDao(): SalarySheetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "salary_sheet_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedInitialData(database.salarySheetDao())
                    }
                }
            }

            suspend fun seedInitialData(dao: SalarySheetDao) {
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
            }
        }
    }
}
