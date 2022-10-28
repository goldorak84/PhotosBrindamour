package org.brindamour.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType

val Cell?.intValueOrNull: Int?
    get() = this?.takeIf { it.isNumeric }?.numericCellValue?.toInt()

val Cell?.doubleValueOrNull: Double?
    get() = this?.takeIf { it.isNumeric }?.numericCellValue

val Cell?.stringValueOrNull: String?
    get() = this?.takeIf { it.isString }?.stringCellValue

val Cell.isNumeric: Boolean
    get() = isType(CellType.NUMERIC)

val Cell.isString: Boolean
    get() = isType(CellType.STRING)

fun Cell.isType(expectedCellType: CellType): Boolean =
    cellType == expectedCellType || (cellType == CellType.FORMULA && cachedFormulaResultType == expectedCellType)