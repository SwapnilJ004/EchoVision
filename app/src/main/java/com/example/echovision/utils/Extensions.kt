package com.example.echovision.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.abs

fun Bitmap.drawBoundingBoxes(
    detections: Array<FloatArray>,
    scores: FloatArray,
    classIndices: LongArray,
    originalWidth: Float,
    originalHeight: Float,
    newWidth: Float,
    newHeight: Float
): Bitmap {
    val outputBitmap = copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(outputBitmap)
    val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    val textPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 30f
        style = Paint.Style.FILL
    }


    val scoreThreshold = 0.4f  // 60%
    // Iterate through the detections
    detections.forEachIndexed { i, detection ->

        if (detection.size < 4) return@forEachIndexed


        val score = scores[i]
        if (score < scoreThreshold) return@forEachIndexed  // Skip low-confidence detections

        // Extract box coordinates
        val x1 = detection[0]
        val y1 = detection[1]
        val x2 = detection[2]
        val y2 = detection[3]

//        val area = abs(x2 - x1)* abs(y2 - y1)




        val adjX1 = x1 / (newWidth / originalWidth)
        val adjY1 = y1 / (newHeight / originalHeight)
        val adjX2 = x2 / (newWidth / originalWidth)
        val adjY2 = y2 / (newHeight / originalHeight)

        // Draw the bounding box
        canvas.drawRect(
            RectF(
                adjX1, adjY1, adjX2, adjY2
            ), paint
        )

        val label = labels[classIndices[i].toInt()]
        canvas.drawText("$label: %.2f".format(score), adjX1, adjY1 - 10, textPaint)

    }

    return outputBitmap
}



