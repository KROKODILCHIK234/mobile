package com.example.base

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object SimpleDBHelper {
    private const val DB_NAME = "school_database"

    fun copyDatabaseFromRaw(context: Context) {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            try {
                // Ensure the databases directory exists
                dbFile.parentFile?.mkdirs()

                // Open your local db as the input stream
                val inputStream = context.resources.openRawResource(R.raw.school_database)

                // Open the empty db as the output stream
                val outputStream = FileOutputStream(dbFile)

                // Transfer bytes from the inputfile to the outputfile
                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                // Close the streams
                outputStream.flush()
                outputStream.close()
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
