package com.example.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies")
    fun getAll(): Flow<List<Company>>

    @Insert
    suspend fun insertAll(companies: List<Company>)

    @Query("DELETE FROM companies WHERE name LIKE '%' || :substring || '%'")
    suspend fun deleteBySubstring(substring: String)

    @Query("SELECT SUM(capitalization) FROM companies")
    suspend fun getTotalCapitalization(): Long?

    @Query("SELECT COUNT(*) FROM companies WHERE capitalization > (SELECT AVG(capitalization) FROM companies)")
    suspend fun getCountAboveAverage(): Int

    // Using the hint: NAME < 'А' (Cyrillic А) to detect English names
    @Query("SELECT COUNT(*) FROM companies WHERE name < 'А'")
    suspend fun getCountEnglishNames(): Int

    @Query("SELECT name FROM companies ORDER BY capitalization DESC, name ASC LIMIT 1")
    suspend fun getHighestCapitalizationName(): String?

    @Query("SELECT name FROM companies ORDER BY length(name) DESC, name ASC LIMIT 1")
    suspend fun getLongestName(): String?

    @Query("DELETE FROM companies")
    suspend fun deleteAll()
}
