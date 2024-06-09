package org.brindamour.timestamp

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.brindamour.excel.intValueOrNull
import org.brindamour.excel.stringValueOrNull
import org.brindamour.extensions.toLocalDateTime
import java.io.File
import java.io.FileInputStream


class PhotoshootExcelFileReader(
    val excelFile: File
) {
    fun read(): List<Timestamp> {
        val file = FileInputStream(excelFile)
        val workbook: Workbook = XSSFWorkbook(file)

        printDebugInfo(workbook)

        return parseTimestamps(workbook.getSheetAt(timestampSheetIndex))
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

    private fun parseTimestamps(sheet: Sheet): List<Timestamp> =
        sheet.rowIterator().asSequence().mapIndexedNotNull { index, row ->
            val cellId = row.getCell(idCellIndex)
            val cellName = row.getCell(nameCellIndex)
            val cellGroup = row.getCell(groupCellIndex)
            val cellTimestampStart = row.getCell(timestampStartIndex)
            val cellTimestampEnd = row.getCell(timestampEndIndex)

            try {
                cellId.intValueOrNull?.takeIf { it != 0 }?.let { id ->
                    Timestamp(
                        id = id,
                        name = cellName?.stringValueOrNull,
                        isNamePrefilled = cellName.cellType == CellType.FORMULA,
                        group = cellGroup.stringValueOrNull ?: cellGroup.intValueOrNull?.toString(),
                        timestampStart = cellTimestampStart.dateCellValue.toLocalDateTime(),
                        timestampEnd = cellTimestampEnd.dateCellValue.toLocalDateTime(),
                    ).also {
                        println(it.printDetails())
                    }
                }
            } catch (exception: Exception) {
                println("Cannot parse row ${index + 1} $exception")
                null
            }
        }.toList()

    fun readPasswords(): List<String> {
        val file = FileInputStream(excelFile)
        val workbook: Workbook = XSSFWorkbook(file)

        val sheet = workbook.getSheetAt(studentsSheetIndex)

        return sheet.rowIterator().asSequence().mapIndexedNotNull { index, row ->
            row.getCell(passwordColumnIndex).stringValueOrNull?.takeIf { index == 0 || row.getCell(ficheColumnIndex).intValueOrNull != null }
        }
            .filter { it.isNotEmpty() }
            .toList()
    }


    companion object {
        const val timestampSheetIndex = 2
        const val idCellIndex = 0
        const val nameCellIndex = 1
        const val groupCellIndex = 2
        const val timestampStartIndex = 6
        const val timestampEndIndex = 7
        const val studentsSheetIndex = 1
        const val passwordColumnIndex = 12
        const val ficheColumnIndex = 5
    }
}