package org.brindamour.timestamp

import java.io.File
import java.time.LocalDateTime

data class NameTagPhoto(
    val file: File,
    val detectedText: String?,
    val date: LocalDateTime
) {
    override fun toString(): String {
        return "name=${file.name}, detectedText=$detectedText, date=$date"
    }
}