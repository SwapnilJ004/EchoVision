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