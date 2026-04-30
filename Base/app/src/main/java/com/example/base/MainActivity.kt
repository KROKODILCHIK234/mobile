package com.example.base

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.base.databinding.ActivityMainBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: MainAdapter
    private var collectJob: Job? = null

    private var studentsList: List<Student> = emptyList()
    private var subjectsList: List<Subject> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2) Модифицировать класс SimpleDBHelper так, чтобы файл с БД копировался из папки raw-ресурсов в папку на устройство 
        SimpleDBHelper.copyDatabaseFromRaw(this)

        // 3) скопированную БД открыть на запись
        db = AppDatabase.getDatabase(this)
        
        setupUI()
        loadInitialData()
    }

    private fun setupUI() {
        adapter = MainAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.radioGroup.setOnCheckedChangeListener { _, _ ->
            updateSpinnerData()
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateRecyclerView()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            // Проверяем, пуста ли база, и заполняем её данными, если нужно
            val existingStudents = db.appDao().getAllStudents().first()
            if (existingStudents.isEmpty()) {
                populateDatabase()
            }

            // Наблюдаем за списками учеников и предметов
            launch {
                db.appDao().getAllStudents().collect {
                    studentsList = it
                    if (binding.radioStudent.isChecked) updateSpinnerData()
                }
            }
            launch {
                db.appDao().getAllSubjects().collect {
                    subjectsList = it
                    if (binding.radioSubject.isChecked) updateSpinnerData()
                }
            }
        }
    }

    private suspend fun populateDatabase() {
        // Данные как на скриншотах
        val s1 = Student(1, "Beff Jezos")
        val s2 = Student(2, "Hom Tanks")
        val s3 = Student(3, "Mark Suckerberg")

        val sub1 = Subject(1, "Avoiding depression")
        val sub2 = Subject(2, "Bug Fix Meditation")
        val sub3 = Subject(3, "Dating for programmers")
        val sub4 = Subject(4, "Logcat for Newbies")

        db.appDao().insertStudent(s1)
        db.appDao().insertStudent(s2)
        db.appDao().insertStudent(s3)

        db.appDao().insertSubject(sub1)
        db.appDao().insertSubject(sub2)
        db.appDao().insertSubject(sub3)
        db.appDao().insertSubject(sub4)

        // Связи (Beff Jezos изучает всё, Hom Tanks — первые два)
        db.appDao().insertStudentSubject(StudentSubject(1, 1))
        db.appDao().insertStudentSubject(StudentSubject(1, 2))
        db.appDao().insertStudentSubject(StudentSubject(1, 3))
        db.appDao().insertStudentSubject(StudentSubject(1, 4))
        
        db.appDao().insertStudentSubject(StudentSubject(2, 1))
        db.appDao().insertStudentSubject(StudentSubject(2, 2))
    }

    private fun updateSpinnerData() {
        val names = if (binding.radioStudent.isChecked) {
            binding.tvLabel.text = "Student's subjects"
            studentsList.map { it.name }
        } else {
            binding.tvLabel.text = "Students study"
            subjectsList.map { it.name }
        }

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = spinnerAdapter
        
        if (names.isNotEmpty()) {
            binding.spinner.setSelection(0)
            updateRecyclerView()
        } else {
            adapter.submitList(emptyList())
        }
    }

    private fun updateRecyclerView() {
        val selectedPosition = binding.spinner.selectedItemPosition
        if (selectedPosition < 0) {
            adapter.submitList(emptyList())
            return
        }

        collectJob?.cancel()
        collectJob = lifecycleScope.launch {
            if (binding.radioStudent.isChecked) {
                if (selectedPosition < studentsList.size) {
                    val studentId = studentsList[selectedPosition].studentId
                    db.appDao().getSubjectsForStudent(studentId).collect { subjects ->
                        adapter.submitList(subjects.map { it.name })
                    }
                }
            } else {
                if (selectedPosition < subjectsList.size) {
                    val subjectId = subjectsList[selectedPosition].subjectId
                    db.appDao().getStudentsForSubject(subjectId).collect { students ->
                        adapter.submitList(students.map { it.name })
                    }
                }
            }
        }
    }
}
