package org.brindamour.extensions

val Boolean?.isTrue: Boolean
    get() = this != null && this == true

val Boolean?.isTrueOrNull: Boolean
    get() = this == null || this == true

val Boolean?.isFalseOrNull: Boolean
    get() = this == null || this == false

val Boolean?.isFalse: Boolean
    get() = this != null && this == false