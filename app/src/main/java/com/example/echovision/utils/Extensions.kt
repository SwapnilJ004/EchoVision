package com.example.echovision.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import kotlin.math.abs


fun Bitmap.drawBoundingBoxes(
    context: Context,
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
    val detectedObjects = mutableListOf<Array<String>>()
    val proximityThreshold = 300000 // threshold area of bounding box indicating nearness or farness
    val catastropicProximityThreshold = 3000000

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

        val adjX1 = x1 / (newWidth / originalWidth)
        val adjY1 = y1 / (newHeight / originalHeight)
        val adjX2 = x2 / (newWidth / originalWidth)
        val adjY2 = y2 / (newHeight / originalHeight)

       val area = abs(adjX2 - adjX1)*abs(adjY2 - adjY1)
        println("Area is: $area for ${labels[classIndices[i].toInt()]}")
        Log.d("MyApp", "Area is: $area for ${labels[classIndices[i].toInt()]}")

        // Draw the bounding box
        canvas.drawRect(
            RectF(
                adjX1, adjY1, adjX2, adjY2
            ), paint
        )

        val label = labels[classIndices[i].toInt()]
        canvas.drawText("$label: %.2f".format(score), adjX1, adjY1 - 10, textPaint)

        if(area >= catastropicProximityThreshold){
            detectedObjects.add(arrayOf(label, "very near"))
        }
        else if(area >= proximityThreshold){
            detectedObjects.add(arrayOf(label, "near"))
        }
        else{
            detectedObjects.add(arrayOf(label, "far"))
        }
    }

    var textStringToSpeech = ""
    detectedObjects.forEach { (name, status) ->
        if(status == "very near"){
            textStringToSpeech += "Take care! Detected $name is $status,"
        }
        else textStringToSpeech += "Detected $name is $status,"
        Log.d("MyApp", "Detected $name is $status")
    }

    // Text to speech:
    var tts: TextToSpeech? = null

    fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1")
    }
    tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
            } else {
                // You can speak immediately here if you want

                speakOut(textStringToSpeech)
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }


    return outputBitmap
}



