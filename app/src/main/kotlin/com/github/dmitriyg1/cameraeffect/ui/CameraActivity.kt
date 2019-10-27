package com.github.dmitriyg1.cameraeffect.ui

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysisConfig
import androidx.camera.core.UseCase
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.dmitriyg1.cameraeffect.R
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
  private val executor = Executors.newSingleThreadExecutor()
  private lateinit var surfaceView: GLSurfaceView
  private lateinit var renderer: GlRenderer

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_camera)

    surfaceView = findViewById(R.id.glsurface)

    renderer = GlRenderer(surfaceView)
    surfaceView.preserveEGLContextOnPause = true
    surfaceView.setEGLContextClientVersion(2)
    surfaceView.setRenderer(renderer)
    surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

    start()
  }

  private fun start() {
    // Request camera permissions
    if (allPermissionsGranted()) {
      surfaceView.post { startCamera() }
    } else {
      ActivityCompat.requestPermissions(
        this,
        REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
      )
    }

  }

  private fun startCamera() {
    // Bind use cases to lifecycle
    // If Android Studio complains about "this" being not a LifecycleOwner
    // try rebuilding the project or updating the appcompat dependency to
    // version 1.1.0 or higher.
    CameraX.bindToLifecycle(
      this,
      imageAnalyzer()
    )
  }

  private fun imageAnalyzer(): UseCase {
    val analyzerConfig = ImageAnalysisConfig.Builder().apply {
      setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
      setTargetResolution(Size(WIDTH, HEIGHT))
    }.build()

    return ImageAnalysis(analyzerConfig).apply {
      setAnalyzer(executor, renderer)
    }
  }

  /**
   * Process result from permission request dialog box, has the request
   * been granted? If yes, start Camera. Otherwise display a toast
   */
  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>, grantResults: IntArray
  ) {
    if (requestCode == REQUEST_CODE_PERMISSIONS) {
      if (allPermissionsGranted()) {
        surfaceView.post { startCamera() }
      } else {
        Toast.makeText(
          this,
          "Permissions not granted by the user.",
          Toast.LENGTH_SHORT
        ).show()
        finish()
      }
    }
  }

  /**
   * Check if all permission specified in the manifest have been granted
   */
  private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(
      baseContext, it
    ) == PackageManager.PERMISSION_GRANTED
  }
}

// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 10

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

const val WIDTH = 640
const val HEIGHT = 640
