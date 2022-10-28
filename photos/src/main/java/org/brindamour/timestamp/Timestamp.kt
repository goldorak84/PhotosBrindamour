package org.brindamour.timestamp

import java.time.LocalDateTime

data class Timestamp(
    val id: Int,
    val name: String?,
    val isNamePrefilled: Boolean,
    val group: String?,
    val timestampStart: LocalDateTime,
    val timestampEnd: LocalDateTime
) {

    val isStudentKnown: Boolean
        get() = isNamePrefilled && name != null

    fun printDetails(): String {
        return toString().removeSuffix(")") + ", isStudentKnown=$isStudentKnown)"
    }
}