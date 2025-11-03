package com.example.echovision.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.echovision.model.LetterboxResult
object ImageUtils {

    fun letterboxResize(
        bitmap: Bitmap,
        targetSize: Int,
        mean: FloatArray = floatArrayOf(0.406f, 0.456f, 0.485f),
        std: FloatArray = floatArrayOf(0.225f, 0.224f, 0.229f)
    ): LetterboxResult {
        val oldW = bitmap.width
        val oldH = bitmap.height
        val (newW, newH) = if (oldW >= oldH) {
            targetSize to (targetSize * oldH) / oldW
        } else {
            (targetSize * oldW) / oldH to targetSize
        }
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, true)
        val paddingW = targetSize - newW
        val paddingH = targetSize - newH
        // Create a canvas and paste the resized bitmap onto the top-left corner
        val paddedBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(paddedBitmap)
        canvas.drawColor(Color.BLACK) // padding background
        val softwareBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, false)
        canvas.drawBitmap(softwareBitmap, 0f, 0f, null)
        // Convert to normalized float array in NCHW format
        val tensorData = FloatArray(3 * targetSize * targetSize)
        val pixels = IntArray(targetSize * targetSize)
        paddedBitmap.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)
        for (y in 0 until targetSize) {
            for (x in 0 until targetSize) {
                val pixel = pixels[y * targetSize + x]
                val r = ((pixel shr 16) and 0xFF) / 255f
                val g = ((pixel shr 8) and 0xFF) / 255f
                val b = (pixel and 0xFF) / 255f

                val index = y * targetSize + x
                tensorData[index] = (r - mean[0]) / std[0]
                tensorData[targetSize * targetSize + index] = (g - mean[1]) / std[1]
                tensorData[2 * targetSize * targetSize + index] = (b - mean[2]) / std[2]
            }
        }
        paddedBitmap.recycle()
        return LetterboxResult(tensorData, newW, newH, oldW, oldH, paddingW, paddingH)
    }

    fun convertUriToBitmap(selectedPhotoUri: Uri, context: Context): Bitmap {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                selectedPhotoUri
            )
            else -> {
                val source = ImageDecoder.createSource(context.contentResolver, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source)
            }
        }
        return bitmap
    }
}