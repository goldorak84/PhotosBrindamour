package org.brindamour.timestamp

import net.sourceforge.tess4j.ITessAPI
import net.sourceforge.tess4j.ITesseract
import net.sourceforge.tess4j.Tesseract
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants
import java.awt.Rectangle
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NameTagsReader(val validate: Boolean) {
    fun readNameTags(nameTagsPath: File): List<NameTagPhoto> {
        val instance: ITesseract = Tesseract() // JNA Interface Mapping
        System.setProperty("jna.library.path", "/usr/local/Cellar/tesseract/5.2.0/lib");
        instance.setDatapath("/usr/local/Cellar/tesseract/5.2.0/share/tessdata") // path to tessdata directory
        instance.setTessVariable("load_system_dawg", "F")
        instance.setTessVariable("load_freq_dawg", "F")
        instance.setTessVariable("user_words_suffix", "user-words")
        instance.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_CUBE_ONLY)
        instance.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_BLOCK)

        return nameTagsPath
            .listFiles { _, name -> name.endsWith(".jpg") }
            ?.map { it ->
                val size = Imaging.getImageSize(it)

                val newWidth: Int = size.width
                val newHeight: Int = size.height

                val metadata = Imaging.getMetadata(it) as JpegImageMetadata
                val date = metadata.exif.getFieldValue(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL) as Array<String>
                val result = if (validate) {
                    instance.doOCR(
                        it,
                        Rectangle(
                            (newWidth * 0.15).toInt(),
                            (newHeight * .4).toInt(),
                            (newWidth * .70).toInt(),
                            (newHeight * .5).toInt()
                        )
                    ).split(' ', '.', ',', '\n').filterNot { it.length <= 4 }.joinToString(separator = " ")
                } else {
                    null
                }
                val localDate = LocalDateTime.parse(date[0], DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"))
                NameTagPhoto(it, result, localDate).also(::println)
            }
            ?.sortedBy { it.date }
            ?: emptyList()
    }
}