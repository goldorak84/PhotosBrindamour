package org.brindamour.timestamp

data class TimestampMatch(
    val timestamp: Timestamp,
    val nameTagPhoto: NameTagPhoto
) {
    val matchLevel: MatchLevel
        get() {
            val isMatched = nameTagPhoto.detectedText?.takeUnless { it.trim().isEmpty() }?.split(' ')?.any { tagName ->
                timestamp.name?.contains(tagName) ?: false
            } ?: false

            return when {
                isMatched -> MatchLevel.MATCHED
                nameTagPhoto.detectedText.isNullOrEmpty() -> MatchLevel.NO_TEXT_TO_MATCH
                else -> MatchLevel.NOT_MATCHED
            }
        }

    override fun toString(): String {
        return "$matchLevel $timestamp + $nameTagPhoto"
    }
}