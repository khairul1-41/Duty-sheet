package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DutyEntry
import com.example.data.model.SalarySheet
import com.example.data.model.SalarySheetWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface SalarySheetDao {

    @Transaction
    @Query("SELECT * FROM salary_sheets ORDER BY updatedAt DESC")
    fun getAllSheetsWithEntries(): Flow<List<SalarySheetWithEntries>>

    @Transaction
    @Query("SELECT * FROM salary_sheets WHERE id = :sheetId")
    fun getSheetWithEntriesById(sheetId: Long): Flow<SalarySheetWithEntries?>

    @Transaction
    @Query("SELECT * FROM salary_sheets WHERE id = :sheetId")
    suspend fun getSheetWithEntriesByIdSync(sheetId: Long): SalarySheetWithEntries?

    @Query("SELECT COUNT(*) FROM salary_sheets")
    suspend fun getSheetsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSheet(sheet: SalarySheet): Long

    @Update
    suspend fun updateSheet(sheet: SalarySheet)

    @Delete
    suspend fun deleteSheet(sheet: SalarySheet)

    @Query("DELETE FROM salary_sheets WHERE id = :sheetId")
    suspend fun deleteSheetById(sheetId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DutyEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<DutyEntry>): List<Long>

    @Update
    suspend fun updateEntry(entry: DutyEntry)

    @Delete
    suspend fun deleteEntry(entry: DutyEntry)

    @Query("DELETE FROM duty_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: Long)

    @Query("DELETE FROM duty_entries WHERE sheetId = :sheetId")
    suspend fun deleteAllEntriesForSheet(sheetId: Long)
}
