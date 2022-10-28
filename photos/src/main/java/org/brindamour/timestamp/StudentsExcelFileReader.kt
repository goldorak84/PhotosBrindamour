package org.brindamour.timestamp

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.brindamour.excel.intValueOrNull
import org.brindamour.excel.stringValueOrNull
import java.io.File
import java.io.FileInputStream


class StudentsExcelFileReader {
    fun read(excelFile: File): List<Student> {
        val file = FileInputStream(excelFile)
        val workbook: Workbook = XSSFWorkbook(file)

        printDebugInfo(workbook)

        return parseTimestamps(workbook.getSheetAt(studentFileIndex))
    }

    private fun printDebugInfo(workbook: Workbook) {
        println("Spreadsheet version : ${workbook.spreadsheetVersion}")
        println("Number of sheets ${workbook.numberOfSheets}")
        println("Sheet info : ")
        workbook.sheetIterator().forEach {
            println("   Name : ${it.sheetName}")
            println("   Rows : ${it.lastRowNum}")
            println()
        }
    }

    private fun parseTimestamps(sheet: Sheet): List<Student> =
        sheet.rowIterator().asSequence().mapIndexedNotNull { index, row ->
            val cellId = row.getCell(idCellIndex)
            val cellName = row.getCell(nameCellIndex)
            val cellGroup = row.getCell(groupCellIndex)

            try {
                cellId.intValueOrNull?.takeIf { it != 0 }?.let { id ->
                    Student(
                        id = id,
                        name = cellName?.stringValueOrNull,
                        group = cellGroup.stringValueOrNull ?: cellGroup.intValueOrNull?.toString(),
                    ).also {
                        println(it.printDetails())
                    }
                }
            } catch (exception: Exception) {
                println("Cannot parse row ${index + 1} $exception")
                null
            }
        }.toList()

    companion object {
        const val studentFileIndex = 1
        const val idCellIndex = 5
        const val nameCellIndex = 7
        const val groupCellIndex = 6
    }
}