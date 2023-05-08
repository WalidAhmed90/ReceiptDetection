package com.example.objectdetectionexample

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.example.objectdetectionexample.Util.Draw
import com.example.objectdetectionexample.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var objectDetector: ObjectDetector
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }



    }


    private fun startCamera() {
       cameraProviderFuture.addListener({

       val cameraProvider = cameraProviderFuture.get()
        bindPreview(cameraProvider)
       },ContextCompat.getMainExecutor(this))

        val localModel = LocalModel.Builder()
            .setAssetFilePath("default_model.tflite")
            .build()

        val customObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(3)
            .build()

        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)

    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindPreview(cameraProvider: ProcessCameraProvider){
        val preview = Preview.Builder().build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280,720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->

            val rotationDegree = imageProxy.imageInfo.rotationDegrees
            val image = imageProxy.image

            if (image != null) {

                val processImage = InputImage.fromMediaImage(image, rotationDegree)

                objectDetector.process(processImage)
                    .addOnSuccessListener { objects ->
                        for (it in objects){
                            if (binding.parentView.childCount  > 1) binding.parentView.removeViewAt(1)

                         var element = Draw(context = this, rect = it.boundingBox, text = it.labels.firstOrNull()?.text ?: "Undefined")
                            binding.parentView.addView(element)
                        }
                        imageProxy.close()
                    }.addOnFailureListener{
                        Log.v("MainActivity: ", "Error: ${it.message}")
                        imageProxy.close()
                    }
            }

            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector,imageAnalysis, preview)

        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()

    }

    companion object {
        private const val TAG = "CameraX-MLKit"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                android.Manifest.permission.CAMERA
            ).toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}