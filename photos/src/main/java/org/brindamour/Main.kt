package org.brindamour

import org.brindamour.timestamp.*
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val isValidateNames = args.getOrNull(optionIndex) == "-v"
        val isCreateUploadFolders = args.getOrNull(optionIndex) == "-e"
        val isValidateStudentsPhotoCount = args.getOrNull(optionIndex) == "-p"

        val photoshootDirectory = File(args[timestampFileIndex])

        if (isValidateStudentsPhotoCount) {
            validateStudentsPhotoCount(args, photoshootDirectory)
        } else {
            val timestampFile = File(args[timestampFileIndex])
                .listFiles { _, name -> name.endsWith(".xlsx") }?.firstOrNull()

            if (timestampFile == null) {
                println("Aucun fichier excel trouvé dans ce répertoire.")
                exitProcess(-1)
            }

            val nameTagsPath = File(photoshootDirectory, "Noms")
            if (!nameTagsPath.exists()) {
                println("Dossier 'Noms' manquant.")
                exitProcess(-1)
            }

            val photoshootReader = PhotoshootExcelFileReader()
            val timestamps = photoshootReader.read(timestampFile)

            val nameTagsReader = NameTagsReader(false)
            val nameTags = nameTagsReader.readNameTags(nameTagsPath)

            val photoshoot = Photoshoot(timestamps, nameTags)
            val timestampMatch = photoshoot.matchTimestampsAndNameTags()

            if (isValidateNames) {
                createNameValidationFiles(photoshootDirectory, timestampMatch)
            }

            if (isCreateUploadFolders) {
                val studentPhotoPath = File(photoshootDirectory, "Eleves")
                val studentsPhotosReader = StudentsPhotosReader()
                val studentsPhotos = studentsPhotosReader.readStudentsPhotos(studentPhotoPath)
                createUploadDirectoryStructure(photoshootDirectory, studentsPhotos, timestampMatch)
            }
        }
    }

    private fun validateStudentsPhotoCount(args: Array<String>, photoshootDirectory: File) {
        val timestampFile = File(args[timestampFileIndex])
            .listFiles { _, name -> name.endsWith("MASTER.xlsx") }?.firstOrNull()

        if (timestampFile == null) {
            println("Aucun fichier excel trouvé dans ce répertoire.")
            exitProcess(-1)
        }

        val studentReader = StudentsExcelFileReader()
        val students = studentReader.read(timestampFile)
        validateStudentsPhotoCount(photoshootDirectory, students)
    }

    private fun validateStudentsPhotoCount(photoshootDirectory: File, students: List<Student>) {
        val uploadDirectory = File(photoshootDirectory, "ToUpload")
        val studentPhotoCount = students.mapNotNull { student ->
            if (student.isStudentKnown) {
                val studentDirectory = File(uploadDirectory, "Classe ${student.group}/Eleve ${student.id}")
                val pictureCount = studentDirectory.listFiles { _, name -> name.endsWith(".jpg") }?.size

                StudentPhotoCount(student, pictureCount ?: 0)
            } else {
                null
            }
        }.sortedBy(StudentPhotoCount::photoCount)

        studentPhotoCount.forEach(::println)
    }

    private fun createNameValidationFiles(photoshootDirectory: File, timestampMatches: List<TimestampMatch>) {
        val validationFolder = File(photoshootDirectory, "NamesValidation")
        if (validationFolder.exists()) {
            validationFolder.deleteRecursively()
        }
        Files.createDirectory(validationFolder.toPath())

        timestampMatches.forEach {
            val sourceFile = it.nameTagPhoto.file
            val outputFile = File(validationFolder, "${sourceFile.name}-${it.timestamp.name}.${sourceFile.extension}")

            sourceFile.copyTo(outputFile)
        }
    }

    private fun createUploadDirectoryStructure(
        photoshootDirectory: File,
        studentsPhotos: List<StudentPhoto>,
        timestampMatches: List<TimestampMatch>
    ) {
        val uploadFolder = File(photoshootDirectory, "Upload")
        if (uploadFolder.exists()) {
            uploadFolder.deleteRecursively()
        }
        Files.createDirectory(uploadFolder.toPath())
        val sortedTimestampMatches = timestampMatches.sortedBy { it.nameTagPhoto.date }

        var timestampIndex = 0
        studentsPhotos.forEach studentLoop@{ studentPhoto ->
            for (i in timestampIndex..sortedTimestampMatches.lastIndex) {
                timestampIndex = i
                val match = timestampMatches[i]
                val nextMatch = timestampMatches.getOrNull(i + 1)
                if (studentPhoto.date >= match.nameTagPhoto.date && (nextMatch == null || studentPhoto.date < nextMatch.nameTagPhoto.date)) {
                    val classroomFolder = File(uploadFolder, "Classe ${match.timestamp.group}")
                    if (!classroomFolder.exists()) {
                        Files.createDirectory(classroomFolder.toPath())
                    }

                    val studentFolder = File(classroomFolder, "Eleve ${match.timestamp.id}")
                    if (!studentFolder.exists()) {
                        Files.createDirectory(studentFolder.toPath())
                    }

                    val studentPhotoFile =
                        File(studentFolder, "${match.timestamp.id}_${match.timestamp.name}_${studentPhoto.file.name}")
                    studentPhoto.file.copyTo(studentPhotoFile)

                    return@studentLoop
                }
            }
        }
    }

    private const val timestampFileIndex = 0
    private const val optionIndex = 1
}