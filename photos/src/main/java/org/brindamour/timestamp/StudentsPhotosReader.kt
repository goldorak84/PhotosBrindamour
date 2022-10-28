package org.brindamour.timestamp

import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StudentsPhotosReader {
    fun readStudentsPhotos(studentPhotoPath: File): List<StudentPhoto> {

        return studentPhotoPath
            .listFiles { _, name -> name.endsWith(".jpg") }
            ?.map {
                val metadata = Imaging.getMetadata(it) as JpegImageMetadata
                val date = metadata.exif.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL) as Array<String>
                val localDate = LocalDateTime.parse(date[0], DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"))
                StudentPhoto(it, localDate).also(::println)
            }
            ?.sortedBy { it.date }
            ?: emptyList()
    }
}