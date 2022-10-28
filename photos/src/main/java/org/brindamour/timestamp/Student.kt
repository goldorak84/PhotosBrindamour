package org.brindamour.timestamp

data class Student(
    val id: Int,
    val name: String?,
    val group: String?,
) {

    val isStudentKnown: Boolean
        get() = name != null

    fun printDetails(): String {
        return toString().removeSuffix(")") + ", isStudentKnown=$isStudentKnown)"
    }
}