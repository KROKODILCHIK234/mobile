package com.example.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val capitalization: Long
)
