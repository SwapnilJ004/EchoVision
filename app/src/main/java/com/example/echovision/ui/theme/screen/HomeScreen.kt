package com.example.echovision.ui.theme.screen

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.echovision.utils.ImageUtils
import com.example.echovision.utils.ImageUtils.letterboxResize
import com.example.echovision.utils.drawBoundingBoxes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import kotlin.text.toFloat

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val env = OrtEnvironment.getEnvironment()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var currentImage by remember { mutableStateOf<Uri?>(null) }

    val pickImage =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback invoked after media selected or picker activity closed.
            if (uri != null) {
                currentImage = uri
                coroutineScope.launch {

                    detectImage(env, context, uri) { bitmap ->
                        resultBitmap?.recycle()
                        resultBitmap = bitmap
                    }
                }
            }
        }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
    ) {
        ImagePreview(currentImage)
        Button(onClick = {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Pick Image and Detect Object")
        }
        resultBitmap?.let { ImageResult(it) }
    }

}

@Composable
fun ImageResult(image: Bitmap, modifier: Modifier = Modifier) {

    Column {
        Text(
            "Result",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        AsyncImage(
            model = image,
            contentDescription = "",
            modifier = modifier
                .fillMaxWidth()
//                .heightIn(max = 200.dp) // small preview
                .heightIn(min = 300.dp, max = 800.dp) // Bigger preview
        )
    }

}

@Composable
fun ImagePreview(currentImage: Uri?, modifier: Modifier = Modifier) {
    if (currentImage != null) {
        Column {
            Text(
                "Original Version",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            AsyncImage(
                model = currentImage,
                contentDescription = "",
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            )
        }

    }
}

private suspend fun detectImage(
    env: OrtEnvironment,
    context: Context,
    uri: Uri,
    onResult: (Bitmap) -> Unit
) {
    val startTime = System.currentTimeMillis()
    val originalBitmap = ImageUtils.convertUriToBitmap(uri, context)

    val inputSize = 640
    val (tensorData, newW, newH, oldW, oldH, padW, padH) = letterboxResize(
        originalBitmap,
        inputSize
    )

    val inputTensor = OnnxTensor.createTensor(
        env,
        FloatBuffer.wrap(tensorData),
        longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong())
    )
    val session = createOrtSession(env, context)
    val output = runInference(session, inputTensor)

    val boxes = output[0].value as Array<FloatArray>
    val classIndices = output.get(1).value as LongArray
    val scores = output.get(2).value as FloatArray
    val annotatedBitmap = originalBitmap.drawBoundingBoxes(
        boxes,
        scores,
        classIndices,
        originalWidth = oldW.toFloat(),
        originalHeight = oldH.toFloat(),
        newWidth = newW.toFloat(),
        newHeight = newH.toFloat()
    )
    val endTime = (System.currentTimeMillis() - startTime)
    println("inference time : $endTime")
    onResult.invoke(annotatedBitmap)
}

private fun runInference(
    ortSession: OrtSession,
    inputTensor: OnnxTensor
): OrtSession.Result {
    ortSession.use {
        val inputs = mapOf(ortSession.inputNames.iterator().next() to inputTensor)
        val results = ortSession.run(inputs)
        return results
    }
}

// Create a new ORT session in background
private suspend fun createOrtSession(env: OrtEnvironment, context: Context): OrtSession =
    withContext(Dispatchers.Default) {
        env.createSession(context.assets.open("yolo.onnx").readBytes())
    }


// with esp32

//import ai.onnxruntime.OnnxTensor
//import ai.onnxruntime.OrtEnvironment
//import ai.onnxruntime.OrtSession
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import coil3.compose.AsyncImage
//import com.example.Echo_Vision.utils.ImageUtils
//import com.example.Echo_Vision.utils.drawBoundingBoxes
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.net.HttpURLConnection
//import java.net.URL
//import java.nio.FloatBuffer
//
//@Composable
//fun HomeScreen(modifier: Modifier = Modifier) {
//    val context = LocalContext.current
//    val env = remember { OrtEnvironment.getEnvironment() }
//    val coroutineScope = rememberCoroutineScope()
//
//    // Reuse YOLO session for all captures
//    val session by remember {
//        mutableStateOf(
//            env.createSession(context.assets.open("yolo.onnx").readBytes())
//        )
//    }
//
//    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
//    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }
//
//    val esp32CamUrl = "http://192.168.1.11/capture"
//
//    Column(
//        modifier = modifier
//            .verticalScroll(rememberScrollState())
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        currentBitmap?.let { ImagePreview(it) }
//
//        Button(
//            onClick = {
//                coroutineScope.launch {
//                    try {
//                        // Step 1: Fetch Image
//                        val bitmap = fetchImageFromESP32(esp32CamUrl)
//                        currentBitmap = bitmap
//
//                        // Step 2: Detect Objects (off UI thread)
//                        detectBitmap(env, bitmap, session) { annotated ->
//                            resultBitmap?.recycle()
//                            resultBitmap = annotated
//                        }
//
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//            },
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        ) {
//            Text("Capture from ESP32-CAM")
//        }
//
//        resultBitmap?.let { ImageResult(it) }
//    }
//}
//
///* -------------------- IMAGE DISPLAY COMPOSABLES -------------------- */
//
//@Composable
//fun ImagePreview(bitmap: Bitmap) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Text("Captured Image", modifier = Modifier.padding(8.dp))
//        AsyncImage(
//            model = bitmap,
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxWidth()
//                .heightIn(max = 400.dp)
//        )
//    }
//}
//
//@Composable
//fun ImageResult(bitmap: Bitmap) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Text("Detection Result", modifier = Modifier.padding(8.dp))
//        AsyncImage(
//            model = bitmap,
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxWidth()
//                .heightIn(max = 400.dp)
//        )
//    }
//}
//
///* -------------------- ESP32-CAM IMAGE FETCH -------------------- */
//
//private suspend fun fetchImageFromESP32(url: String): Bitmap = withContext(Dispatchers.IO) {
//    val connection = URL(url).openConnection() as HttpURLConnection
//    connection.connectTimeout = 5000
//    connection.readTimeout = 5000
//    connection.doInput = true
//    connection.connect()
//    val inputStream = connection.inputStream
//    val bitmap = BitmapFactory.decodeStream(inputStream)
//    inputStream.close()
//    bitmap
//}
//
///* -------------------- DETECTION PIPELINE -------------------- */
//
//private suspend fun detectBitmap(
//    env: OrtEnvironment,
//    bitmap: Bitmap,
//    session: OrtSession,
//    onResult: (Bitmap) -> Unit
//) {
//    withContext(Dispatchers.Default) {
//        val startTime = System.currentTimeMillis()
//        val inputSize = 640
//
//        val (tensorData, newW, newH, oldW, oldH, padW, padH) =
//            ImageUtils.letterboxResize(bitmap, inputSize)
//
//        val inputTensor = OnnxTensor.createTensor(
//            env,
//            FloatBuffer.wrap(tensorData),
//            longArrayOf(1, 3, inputSize.toLong(), inputSize.toLong())
//        )
//
//        val output = runInference(session, inputTensor)
//        val boxes = output[0].value as Array<FloatArray>
//        val classIndices = output[1].value as LongArray
//        val scores = output[2].value as FloatArray
//
//        val annotated = bitmap.drawBoundingBoxes(
//            boxes,
//            scores,
//            classIndices,
//            originalWidth = oldW.toFloat(),
//            originalHeight = oldH.toFloat(),
//            newWidth = newW.toFloat(),
//            newHeight = newH.toFloat()
//        )
//
//        val end = System.currentTimeMillis() - startTime
//        println("🔹 Inference time: ${end}ms")
//
//        withContext(Dispatchers.Main) {
//            onResult(annotated)
//        }
//    }
//}
//
///* -------------------- ONNX SESSION RUNNER -------------------- */
//
//private fun runInference(
//    session: OrtSession,
//    tensor: OnnxTensor
//): OrtSession.Result {
//    val inputName = session.inputNames.iterator().next()
//    return session.run(mapOf(inputName to tensor))
//}