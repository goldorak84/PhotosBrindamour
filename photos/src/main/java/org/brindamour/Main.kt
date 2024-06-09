package org.brindamour

import org.brindamour.extensions.unaccent
import org.brindamour.timestamp.*
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val isSimpleExcelExport = args.any { it == "-x" }
        val isValidateNames = args.any { it == "-v" }
        val createUploadFolders = args.any { it == "-e" }
        val isValidateStudentsPhotoCount = args.any { it == "-p" }
        val isExportGpi = args.any { it == "-s" }
        val isCreatePasswordFile = args.any { it == "-w" }

        if (args.lastIndex < timestampFileIndex) {
            println("Aucun fichier excel spécifié.")
            exitProcess(-1)
        }
        val photoshootDirectory = File(args[timestampFileIndex])

        if (isValidateStudentsPhotoCount) {
            validateStudentsPhotoCount(args, photoshootDirectory)
        } else if (isExportGpi) {
            exportGpi(args, photoshootDirectory)
        }  else {
            val timestampFile = File(args[timestampFileIndex])
                    .listFiles { _, name -> name.endsWith(".xlsx") }?.firstOrNull()

            if (timestampFile == null) {
                println("Aucun fichier excel trouvé dans ce répertoire.")
                exitProcess(-1)
            }

            val photoshootReader = PhotoshootExcelFileReader(timestampFile)
            val timestamps = photoshootReader.read()


            if (isCreatePasswordFile) {
                exportPasswordFile(photoshootDirectory, photoshootReader)
            }
            else if (isSimpleExcelExport) {

                val studentPhotoPath = File(photoshootDirectory, "Eleves")
                val studentsPhotosReader = StudentsPhotosReader()
                val studentsPhotos = studentsPhotosReader.readStudentsPhotos(studentPhotoPath)
                matchStudentPhotosAndTimestamps(photoshootDirectory, studentsPhotos, timestamps, createUploadFolders)
            } else {
                val nameTagsPath = File(photoshootDirectory, "Noms")
                if (!nameTagsPath.exists()) {
                    println("Dossier 'Noms' manquant.")
                    exitProcess(-1)
                }

                val nameTagsReader = NameTagsReader(false)
                val nameTags = nameTagsReader.readNameTags(nameTagsPath)

                val photoshoot = Photoshoot(timestamps, nameTags)
                val timestampMatch = photoshoot.matchTimestampsAndNameTags()

                if (isValidateNames) {
                    createNameValidationFiles(photoshootDirectory, timestampMatch)
                }

                if (createUploadFolders) {
                    val studentPhotoPath = File(photoshootDirectory, "Eleves")
                    val studentsPhotosReader = StudentsPhotosReader()
                    val studentsPhotos = studentsPhotosReader.readStudentsPhotos(studentPhotoPath)
                    createUploadDirectoryStructure(photoshootDirectory, studentsPhotos, timestampMatch)
                }
            }
        }
    }

    private fun exportPasswordFile(
        photoshootDirectory: File,
        photoshootReader: PhotoshootExcelFileReader,
    ) {
        val passwordFile = File(photoshootDirectory, "passwords.txt")
        val passwords = photoshootReader.readPasswords()
        passwordFile.bufferedWriter().use { out ->
            passwords.forEach {
                out.write(it)
                out.newLine()
            }
        }
    }

    private fun validateStudentsPhotoCount(args: Array<String>, photoshootDirectory: File) {
        val timestampFile = File(args[timestampFileIndex])
                .listFiles { _, name -> name.startsWith("Master") && name.endsWith(".xlsx") }?.firstOrNull()

        if (timestampFile == null) {
            println("Aucun fichier excel trouvé dans ce répertoire.")
            exitProcess(-1)
        }

        val studentReader = StudentsExcelFileReader()
        val students = studentReader.read(timestampFile)

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

    private fun exportGpi(args: Array<String>, photoshootDirectory: File) {
        val timestampFile = File(args[timestampFileIndex])
                .listFiles { _, name -> name.startsWith("Master") && name.endsWith(".xlsx") }?.firstOrNull()

        if (timestampFile == null) {
            println("Aucun fichier excel trouvé dans ce répertoire.")
            exitProcess(-1)
        }

        val studentReader = StudentsExcelFileReader()
        val students = studentReader.read(timestampFile)

        val targetDirectory = File(photoshootDirectory, "GPI")
        if (targetDirectory.exists()) {
            targetDirectory.deleteRecursively()
        }
        Files.createDirectory(targetDirectory.toPath())

        val uploadDirectory = File(photoshootDirectory, "ToUpload")
        students.forEach { student ->
            if (student.isStudentKnown) {
                val studentDirectory = File(uploadDirectory, "Classe ${student.group}/Eleve ${student.id}")
                studentDirectory.listFiles { _, name -> name.endsWith(".jpg") }?.sortedBy { it.name }?.firstOrNull()
                        ?.let {
                            val targetFile = File(targetDirectory, "${student.id}.jpg")
                            it.copyTo(targetFile)
                        }
            }
        }
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

    private fun matchStudentPhotosAndTimestamps(
            photoshootDirectory: File,
            studentsPhotos: List<StudentPhoto>,
            timestamps: List<Timestamp>,
            createUploadFolders: Boolean,
    ) {
        if(createUploadFolders) {
            println("Création du fichier Upload et association des photos...")
        } else {
            println("Association des photos...")
        }
        val uploadFolder = File(photoshootDirectory, "Upload")
        if (uploadFolder.exists()) {
            uploadFolder.deleteRecursively()
        }
        if (createUploadFolders) {
            Files.createDirectory(uploadFolder.toPath())
        }

        studentsPhotos.forEach studentLoop@{ studentPhoto ->
            timestamps.singleOrNull { timestamp -> studentPhoto.date >= timestamp.timestampStart && studentPhoto.date < timestamp.timestampEnd }
                    ?.let { timestamp ->

                        if (!timestamp.isStudentKnown) {
                            println("Photo(${studentPhoto.file.name}, barcode ${timestamp.id}) trouvée, mais aucune fiche d'élève trouvée")
                        } else if (createUploadFolders) {
                            val classroomFolder = File(uploadFolder, "Classe ${timestamp.group}")
                            if (!classroomFolder.exists()) {
                                Files.createDirectory(classroomFolder.toPath())
                            }

                            val studentFolder = File(classroomFolder, "Eleve ${timestamp.id}")
                            if (!studentFolder.exists()) {
                                Files.createDirectory(studentFolder.toPath())
                            }

                            val studentPhotoFile =
                                    File(
                                            studentFolder,
                                            "${timestamp.id}_${timestamp.name?.unaccent()}_${studentPhoto.file.name}"
                                    )
                            studentPhoto.file.copyTo(studentPhotoFile)
                        }
                    } ?: run {
                println("Photo ${studentPhoto.file.name} non trouvée dans le fichier timestamp")
            }
        }

        if(createUploadFolders) {
            println("Création du fichier Upload et association des photos terminée!")
        } else {
            println("Association des photos terminée!")
        }
    }

    private fun createUploadDirectoryStructure(
            photoshootDirectory: File,
            studentsPhotos: List<StudentPhoto>,
            timestampMatches: List<TimestampMatch>
    ) {
        println("Création du fichier Upload et association des photos...")

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
                            File(
                                    studentFolder,
                                    "${match.timestamp.id}_${match.timestamp.name?.unaccent()}_${studentPhoto.file.name}"
                            )
                    studentPhoto.file.copyTo(studentPhotoFile)

                    return@studentLoop
                }
            }
        }

        println("Création du fichier Upload et association des photos terminée!")
    }

    private const val timestampFileIndex = 0
}