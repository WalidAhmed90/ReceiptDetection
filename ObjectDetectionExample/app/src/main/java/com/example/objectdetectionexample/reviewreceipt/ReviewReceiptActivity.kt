package com.example.objectdetectionexample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import java.io.File
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoViewAttacher


/**
 * Created by phuchoang on 11/5/17
 */
class ReviewReceiptActivity : AppCompatActivity() {
    lateinit var mPhotoViewAttacher: PhotoViewAttacher
    companion object {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_receipt)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()!!.setHomeButtonEnabled(true)


       /* val receiptPath = intent.getStringExtra(MainActivity.KEY_RECEIPT_PATH)
//        var receipt = AppUtils.getBitmap(receiptPath, this)
        val mReceiptFile = File(receiptPath)*/
        var receipt: Bitmap? = null
        receipt =BitmapFactory.decodeStream(this.openFileInput("myImage"))

        val ivReceipt = findViewById<ImageView>(R.id.review_receipt_iv_receipt)
        val vto = ivReceipt.getViewTreeObserver()
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                ivReceipt.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                val width = ivReceipt.getMeasuredWidth()
                val height = ivReceipt.getMeasuredHeight()
                var newWidth = receipt!!.width
                if (receipt!!.width < width) {
                    newWidth = width
                }

                val newHeight = newWidth * height / width
                val newReceipt = Bitmap.createScaledBitmap(receipt, newWidth, newHeight, false)

                ivReceipt.setImageBitmap(newReceipt)
                mPhotoViewAttacher = PhotoViewAttacher(ivReceipt)
                mPhotoViewAttacher.scaleType = ImageView.ScaleType.CENTER_CROP

            }
        })

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