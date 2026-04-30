package com.example.base

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM subjects")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("""
        SELECT subjects.* FROM subjects
        INNER JOIN student_subject ON subjects.subjectId = student_subject.subjectId
        WHERE student_subject.studentId = :studentId
    """)
    fun getSubjectsForStudent(studentId: Long): Flow<List<Subject>>

    @Query("""
        SELECT students.* FROM students
        INNER JOIN student_subject ON students.studentId = student_subject.studentId
        WHERE student_subject.subjectId = :subjectId
    """)
    fun getStudentsForSubject(subjectId: Long): Flow<List<Student>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentSubject(studentSubject: StudentSubject)
}
