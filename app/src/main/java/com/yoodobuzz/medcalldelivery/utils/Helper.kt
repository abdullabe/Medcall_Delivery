package com.yoodobuzz.medcalldelivery.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.widget.Toast
import androidx.cardview.widget.CardView
import cn.pedant.SweetAlert.SweetAlertDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object Helper {
    fun Context.toast(message: String){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    }
    fun buttonLayout(button: CardView){
        button.isEnabled = false
        button.alpha = 0.5f

        Handler().postDelayed(object : Runnable {
            override fun run() {
                button.isEnabled = true
                button.alpha = 1.0f

            }
        }, 1500) // time intervel

    }
    fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? {
        // Create a temporary file
        val file = createTempFile(context)
        try {
            // Open an OutputStream on the file
            val outputStream: OutputStream = FileOutputStream(file)
            // Compress the Bitmap to JPEG format and write it to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            // Close the OutputStream
            outputStream.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    private fun createTempFile(context: Context): File {
        val fileName = "temp_image.jpg"
        return File(context.externalCacheDir, fileName)
    }
    fun showDialog(dialog: SweetAlertDialog) {
        dialog.setContentText("Loading")
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun hideDialog(dialog: SweetAlertDialog) {
        dialog.dismiss()
    }
}