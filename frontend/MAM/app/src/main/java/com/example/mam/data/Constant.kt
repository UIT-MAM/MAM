package com.example.mam.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

object Constant {
    const val BASE_URL = "http://172.30.98.165:8080/api/v1/"
    const val STORAGE_URL = BASE_URL + "storage/images?url="
    const val BASE_IMAGE = "https://static.vecteezy.com/system/resources/previews/056/202/171/non_2x/add-image-or-photo-icon-vector.jpg"
    const val BASE_AVT ="https://sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png"
    const val DELAY_TIME = 2_000L
    enum class metadata{
        OTP_ACTION,
        ROLE_NAME,
        USER_STATUS,
        ORDER_STATUS,
        PAYMENT_METHOD,
    }
    enum class notiStatus {
        GENERAL,              // Thông báo chung
        ORDER_PLACED,         // Đơn hàng đã được đặt
        ORDER_RECEIVED,          // Đơn hàng đã được tiếp nhận
        ORDER_PREPARING,    // Đơn hàng đang được chế biến
        ORDER_DELIVERING,    // Đơn hàng đang được giao
        ORDER_DELIVERED,       // Đơn hàng đã giao xong
        PROMOTION              // Khuyến mãi
    }

}