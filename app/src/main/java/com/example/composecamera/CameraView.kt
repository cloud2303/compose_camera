package com.example.composecamera

import android.content.ContentValues.TAG
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import java.nio.ByteBuffer
import java.util.concurrent.Executors


@Composable
fun CameraView(
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    var state by remember { mutableStateOf("") }
    val cameraExecutor = remember {
        Log.d(TAG, "CameraView: 执行了x次")
        Executors.newSingleThreadExecutor()
    }
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = {context->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val previewView = PreviewView(context)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val camSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
//                        state=luma.toString()
                    })
                }

            try {
                cameraProviderFuture.get().bindToLifecycle(
                    lifecycleOwner,
                    camSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            previewView

        }, modifier = Modifier.fillMaxSize()
        )
        Button(onClick = {

        }) {
            Text(text = state)
        }

    }
}
typealias LumaListener = (luma:  ByteArray?) -> Unit

private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {

//        val buffer = image.planes[0].buffer
//        val data = buffer.toByteArray()
//        val pixels = data.map { it.toInt() and 0xFF }
//        val luma = pixels.average()
        val nv21 = yuv420ToNv21(image)
        listener(nv21)

        image.close()
    }
    private fun yuv420ToNv21(image: ImageProxy): ByteArray {
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val size = image.width * image.height
        val nv21 = ByteArray(size * 3 / 2)
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        val u = ByteArray(uSize)
        uBuffer[u]

        //每隔开一位替换V，达到VU交替
        var pos = ySize + 1
        for (i in 0 until uSize) {
            if (i % 2 == 0) {
                nv21[pos] = u[i]
                pos += 2
            }
        }
        return nv21
    }
}