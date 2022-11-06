package com.example.composecamera


import android.os.Bundle

import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.composecamera.ui.theme.ComposeCameraTheme
import com.google.accompanist.permissions.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeCameraTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen() {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(android.Manifest.permission.CAMERA)
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if(event == Lifecycle.Event.ON_START) {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )
    permissionsState.permissions.forEach { permis ->
        when(permis.permission)
        {
            android.Manifest.permission.CAMERA -> {
                when {
                    permis.status.isGranted ->
                    {
                        Text(text= "Camera Permission Granted")
                        CameraView()
                    }
                    permis.status.shouldShowRationale ->
                    {
                        Text(text= "Camera Permission  for taking Photo")
                    }
                    !permis.status.isGranted && !permis.status.shouldShowRationale ->
                    {
                        Text(text= "Camera Permission Denied.Go To App settings for enabling")
                    }
                }
            }
        }
    }

}



@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeCameraTheme {
        CameraScreen()
    }
}