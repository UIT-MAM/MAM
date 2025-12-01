package com.example.mam.repository.retrofit

import com.example.mam.data.UserPreferencesRepository
import com.example.mam.repository.AuthPrivateRepository
import com.example.mam.repository.AuthPublicRepository
import com.example.mam.repository.CartItemRepository
import com.example.mam.repository.CartRepository
import com.example.mam.repository.NotificationRepository
import com.example.mam.repository.OrderRepository
import com.example.mam.repository.PaymentRepository
import com.example.mam.repository.ProductCategoryRepository
import com.example.mam.repository.ProductRepository
import com.example.mam.repository.PromotionRepository
import com.example.mam.repository.ShipperRepository
import com.example.mam.repository.StastiticRepository
import com.example.mam.repository.TwoFaRepository
import com.example.mam.repository.UserPromotionRepository
import com.example.mam.repository.UserRepository
import com.example.mam.repository.VariationOptionRepository
import com.example.mam.repository.VariationRepository

//BaseService để khởi tạo Retrofit client và gọi các service
class BaseRepository(userPreferencesRepository: UserPreferencesRepository) {
    private val privateRetrofit = RetrofitClient.createPrivateRetrofit(userPreferencesRepository)

    private val publicRetrofit = RetrofitClient.createPublicRetrofit()
    // Các service sẽ được khởi tạo ở đây, sử dụng lazy để chỉ khởi tạo khi cần thiết
    val authPublicRepository: AuthPublicRepository by lazy {
        publicRetrofit.create(AuthPublicRepository::class.java) }

    val twoFaRepository: TwoFaRepository by lazy {
        privateRetrofit.create(TwoFaRepository::class.java)
    }

    val authPrivateRepository: AuthPrivateRepository by lazy {
        privateRetrofit.create(AuthPrivateRepository::class.java) }

    val productCategoryRepository: ProductCategoryRepository by lazy {
        privateRetrofit.create(ProductCategoryRepository::class.java)
    }

    val productRepository: ProductRepository by lazy {
        privateRetrofit.create(ProductRepository::class.java)
    }

    val variationRepository: VariationRepository by lazy {
        privateRetrofit.create(VariationRepository::class.java)
    }

    val variationOptionRepository: VariationOptionRepository by lazy {
        privateRetrofit.create(VariationOptionRepository::class.java)
    }

    val cartRepository: CartRepository by lazy {
        privateRetrofit.create(CartRepository::class.java)
    }

    val cartItemRepository: CartItemRepository by lazy {
        privateRetrofit.create(CartItemRepository::class.java)
    }

    val shipperRepository: ShipperRepository by lazy {
        privateRetrofit.create(ShipperRepository::class.java)
    }
    val userRepository: UserRepository by lazy {
        privateRetrofit.create(UserRepository::class.java)
    }
    val notificationRepository: NotificationRepository by lazy {
        privateRetrofit.create(NotificationRepository::class.java)
    }
    val orderRepository: OrderRepository by lazy {
        privateRetrofit.create(OrderRepository::class.java)
    }
    val promotionRepository: PromotionRepository by lazy {
        privateRetrofit.create(PromotionRepository::class.java)
    }
    val userPromotionRepository: UserPromotionRepository by lazy {
        privateRetrofit.create(UserPromotionRepository::class.java)
    }
    val stastiticRepository: StastiticRepository by lazy {
        privateRetrofit.create(StastiticRepository::class.java)
    }
    val paymentRepository: PaymentRepository by lazy {
        privateRetrofit.create(PaymentRepository::class.java)
    }
}