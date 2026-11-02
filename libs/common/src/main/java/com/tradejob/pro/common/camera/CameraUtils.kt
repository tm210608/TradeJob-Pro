package com.tradejob.pro.common.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object CameraUtils {

    fun createImageFile(context: Context, jobId: Long): File {
        val storageDir = File(context.filesDir, "job_photos/$jobId")
        if (!storageDir.exists()) storageDir.mkdirs()
        return File(storageDir, "photo_${UUID.randomUUID()}.jpg")
    }

    fun compressImage(sourceFile: File, maxWidth: Int = 1920, quality: Int = 80): File {
        val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return sourceFile

        val width = bitmap.width
        val height = bitmap.height
        val scale = if (width > maxWidth) maxWidth.toFloat() / width else 1f

        val scaledBitmap = if (scale < 1f) {
            val matrix = Matrix().apply { postScale(scale, scale) }
            Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        } else {
            bitmap
        }

        val correctedBitmap = applyExifOrientation(scaledBitmap, sourceFile)

        val compressedFile = File(sourceFile.parentFile, sourceFile.nameWithoutExtension + "_compressed.jpg")
        FileOutputStream(compressedFile).use { out ->
            correctedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        if (correctedBitmap != bitmap) correctedBitmap.recycle()
        if (scaledBitmap != bitmap) scaledBitmap.recycle()
        bitmap.recycle()

        sourceFile.delete()
        compressedFile.renameTo(sourceFile)

        return sourceFile
    }

    private fun applyExifOrientation(bitmap: Bitmap, file: File): Bitmap {
        return try {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }
            val corrected = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (corrected != bitmap) bitmap.recycle()
            corrected
        } catch (e: Exception) {
            bitmap
        }
    }
}
