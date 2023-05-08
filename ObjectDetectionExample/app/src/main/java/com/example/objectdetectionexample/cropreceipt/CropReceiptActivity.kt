package com.example.objectdetectionexample

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import org.opencv.android.OpenCVLoader
import ppapps.cropreceiptdemo.opencv.OpenCVUtils
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by phuchoang on 11/5/17
 */
class CropReceiptActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        if(v!!.id == R.id.scanButton){
            sourceFrame!!.post{
                val croppedBitmap = OpenCVUtils.cropReceiptByFourPoints(bitmap!!, polygonView!!.getListPoint(), sourceImageView.width, sourceImageView.height)
                if (croppedBitmap != null) {
                    createImageFromBitmap(croppedBitmap)
                }
                val intent = Intent(this, ReviewReceiptActivity::class.java)
                startActivity(intent)
            }
        }
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

    init {
        //here goes static initializer code
        if (!OpenCVLoader.initDebug()) {
            Log.e("Scan", "OpenCVLoader.initDebug() = FALSE")
        } else {
            Log.e("Scan", "OpenCVLoader.initDebug() = TRUE")
        }
    }

    private var scanButton: Button? = null
    private lateinit var sourceImageView: ImageView
    private var sourceFrame: FrameLayout? = null
    private var polygonView: PolygonView? = null
    //    private var progressDialogFragment: ProgressDialogFragment? = null
//    private var scanner: IScanner? = null
    private var original: Bitmap? = null

    private var bitmap: Bitmap?
        get() {
            try {
                var bitmap = BitmapFactory.decodeStream(this.openFileInput("myImage"))
                bitmap!!.setDensity(Bitmap.DENSITY_NONE)
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    bitmap = OpenCVUtils.rotate(bitmap, 90)
                }
//                this.contentResolver.delete(uri!!, null, null)
                return bitmap
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }
        set(original) {
            val scaledBitmap = original?.let { Bitmap.createScaledBitmap(it, sourceImageView.width, sourceImageView.height, false) };
            sourceImageView.setImageBitmap(scaledBitmap)
            val pointFs = scaledBitmap?.let { OpenCVUtils.getEdgePoints(it, polygonView!!) }
            polygonView!!.points = pointFs!!
            polygonView!!.visibility = View.VISIBLE

            val layoutParams = FrameLayout.LayoutParams(sourceImageView.width, sourceImageView.height)
            layoutParams.gravity = Gravity.CENTER
            polygonView!!.layoutParams = layoutParams
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_receipt)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        init()
    }

    private fun init() {
        sourceImageView = findViewById(R.id.sourceImageView)
        scanButton = findViewById(R.id.scanButton)
        scanButton!!.setOnClickListener(this)
        sourceFrame = findViewById(R.id.sourceFrame)
        polygonView = findViewById(R.id.polygonView)
        sourceFrame!!.post {
            original = bitmap
            if (original != null) {
                bitmap = original
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

}