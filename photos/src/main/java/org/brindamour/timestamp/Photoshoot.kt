package org.brindamour.timestamp

data class Photoshoot(
    val timestamps: List<Timestamp>,
    val nameTags: List<NameTagPhoto>
) {
    val timestampsKnownStudents = timestamps.filter { it.isStudentKnown }
    val timestampsUnknownStudents = timestamps.filterNot { it.isStudentKnown }

    fun matchTimestampsAndNameTags(): List<TimestampMatch> {
        val timestampMatch = timestampsKnownStudents.zip(nameTags).map {
            TimestampMatch(it.first, it.second).also(::println)
        }
        println()

        if (timestampsKnownStudents.size > nameTags.size) {
            println("Timestamps without name tag")
            timestampsKnownStudents
                .takeLast(timestampsKnownStudents.size - nameTags.size)
                .forEach(::println)
        } else if (nameTags.size > timestampsKnownStudents.size) {
            println("Name tag without timestamp")
            nameTags
                .takeLast(nameTags.size - timestampsKnownStudents.size)
                .forEach(::println)
        }

        return timestampMatch
    }
}