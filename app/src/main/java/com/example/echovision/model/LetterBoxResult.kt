package com.example.echovision.model

data class LetterboxResult(
    val tensorData: FloatArray,
    val newWidth: Int,
    val newHeight: Int,
    val oldWidth: Int,
    val oldHeight: Int,
    val paddingW: Int,
    val paddingH: Int
)