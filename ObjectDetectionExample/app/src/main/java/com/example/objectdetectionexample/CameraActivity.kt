package com.example.objectdetectionexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.objectdetectionexample.Util.Draw
import com.example.objectdetectionexample.databinding.ActivityCameraBinding
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import org.opencv.android.OpenCVLoader
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {


    lateinit var mBinding: ActivityCameraBinding

    var flashMode = ImageCapture.FLASH_MODE_OFF

    private var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService
    var camera: Camera? = null
    private lateinit var objectDetector: ObjectDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(mBinding?.viewFinder?.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280,720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            /*.also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    Timber.d(TAG, "Average luminosity: $luma")
                })
            }*/

            // Select back camera as a default
            val cameraSelector = lensFacing

            imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->

                val rotationDegree = imageProxy.imageInfo.rotationDegrees
                val image = imageProxy.image

                if (image != null) {

                    val processImage = InputImage.fromMediaImage(image, rotationDegree)

                    objectDetector.process(processImage)
                        .addOnSuccessListener { objects ->
                            for (it in objects){
                                if (mBinding.parentView.childCount  > 1) mBinding.parentView.removeViewAt(1)

                                if (it.labels.firstOrNull()?.text == "Receipt"){
                                    var element = Draw(context = this, rect = it.boundingBox, text = it.labels.firstOrNull()?.text ?: "Undefined")
                                    mBinding.parentView.addView(element)
                                    takePhoto()
                                    return@addOnSuccessListener
                                }
                                var element = Draw(context = this, rect = it.boundingBox, text = it.labels.firstOrNull()?.text ?: "Undefined")
                                mBinding.parentView.addView(element)
                            }
                            imageProxy.close()
                        }.addOnFailureListener{
                            Log.v("MainActivity: ", "Error: ${it.message}")
                            imageProxy.close()
                        }
                }
            }


            //     val viewPort = ViewPort.Builder(Rational(350, 100), Surface.ROTATION_0).build()
            val useCaseGroup = UseCaseGroup.Builder()
                .addUseCase(preview)
                .addUseCase(imageAnalyzer)
                .addUseCase(imageCapture!!)
                //   .setViewPort(viewPort)
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera

                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, useCaseGroup
                )
                // Bind use cases to camera
                /* cameraProvider.bindToLifecycle(
                     this, cameraSelector, preview, imageCapture, imageAnalyzer
                 )
 */
            } catch (exc: Exception) {

            }

        }, ContextCompat.getMainExecutor(this))


        val localModel = LocalModel.Builder()
            .setAssetFilePath("object_detection.tflite")
            .build()

        val customObjectDetectorOptions = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(3)
            .build()

        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            @ExperimentalGetImage object : ImageCapture.OnImageCapturedCallback() {
                @RequiresApi(Build.VERSION_CODES.R)
                override fun onCaptureSuccess(imag: ImageProxy) {

                    val currentLensOrientation: Int = CameraSelector.LENS_FACING_BACK

                    val rotationDirection = if (currentLensOrientation == CameraSelector.LENS_FACING_BACK) 1 else -1
                    val constantRotation = imag.imageInfo.rotationDegrees - camera!!.cameraInfo.sensorRotationDegrees
                    val rotationDegrees = camera!!.cameraInfo.sensorRotationDegrees - this@CameraActivity?.display!!.rotation * 90 + constantRotation * rotationDirection
                    var bitmap = imag.image?.toBitmap()

                    val rotatedBitmap = bitmap?.rotate(rotationDegrees)

                    if (rotatedBitmap != null) {
                        createImageFromBitmap(rotatedBitmap)
                    }

                    val intent = Intent(this@CameraActivity, CropReceiptActivity::class.java)
                    startActivity(intent)

                }

                override fun onError(exc: ImageCaptureException) {
                    super.onError(exc)
                 //   showToast("Photo capture failed: ${exc.message}")
                }
            })
    }

    fun Image.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    fun createImageFromBitmap(bitmap: Bitmap): String? {
        var fileName: String? = "myImage" //no .png or .jpg needed
        try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val fo: FileOutputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
            fo.write(bytes.toByteArray())
            // remember close file output
            fo.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            fileName = null
        }
        return fileName
    }

    fun Bitmap.rotate(degree:Int):Bitmap{
        // Initialize a new matrix
        val matrix = Matrix()

        // Rotate the bitmap
        matrix.postRotate(degree.toFloat())

        // Resize the bitmap
        val scaledBitmap = Bitmap.createScaledBitmap(
            this,
            width,
            height,
            true
        )

        // Create and return the rotated bitmap
        return Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
    }

    override fun onResume() {
        startCamera()
        super.onResume()
    }
}