package org.brindamour.timestamp

data class StudentPhotoCount(
    val student: Student,
    val photoCount: Int
) {
    override fun toString(): String =
        "${student.id} ${student.group} ${student.name} : $photoCount pictures taken"
}