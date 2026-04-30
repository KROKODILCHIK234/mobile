package com.example.base

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val studentId: Long,
    val name: String
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val subjectId: Long,
    val name: String
)

@Entity(
    tableName = "student_subject",
    primaryKeys = ["studentId", "subjectId"],
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId"), Index("subjectId")]
)
data class StudentSubject(
    val studentId: Long,
    val subjectId: Long
)
