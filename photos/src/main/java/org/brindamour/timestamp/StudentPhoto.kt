package org.brindamour.timestamp

import java.io.File
import java.time.LocalDateTime

data class StudentPhoto(
    val file: File,
    val date: LocalDateTime
) {
    override fun toString(): String {
        return "name=${file.name}, date=$date"
    }
}