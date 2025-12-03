package com.example.mam.utils

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

fun rememberQrBitmap(content: String, size: Int = 512): ImageBitmap? {
    if (content.isEmpty()) return null
    return try {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.MARGIN, 1) // Viền trắng nhỏ
        }
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)

        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}