package com.example.objectdetectionexample

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.transition.Visibility
import com.example.objectdetectionexample.Util.AdjustUtil
import com.example.objectdetectionexample.databinding.ActivityCropBinding


class CropActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityCropBinding
    lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_crop)

        bitmap =  BitmapFactory.decodeStream(this.openFileInput("myImage"))

        if (bitmap != null) {
            mBinding.ivPreviewCrop.setImageToCrop(bitmap)
            changeBrightness(20f)
        }

        mBinding.ivDone.setOnClickListener {
            if (mBinding.ivDone.text == "Back")
            {
                onBackPressedDispatcher.onBackPressed()
            }else {
                if (mBinding.ivPreviewCrop.canRightCrop()) {
                    bitmap = mBinding.ivPreviewCrop.crop()
                    mBinding.ivPreviewCrop.visibility = View.GONE
                    mBinding.imCropImage.setImageBitmap(bitmap)
                    mBinding.ivDone.text = "Back"

                }
            }
        }




    }


    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    private fun changeBrightness(brightness: Float) {
        mBinding.ivPreviewCrop.setImageBitmap(
            AdjustUtil.changeBitmapContrastBrightness(
                bitmap,
                1.0f,
                brightness
            )
        )
    }

}