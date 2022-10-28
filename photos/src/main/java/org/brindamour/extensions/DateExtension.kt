package org.brindamour.extensions

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


fun Date.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(time)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();