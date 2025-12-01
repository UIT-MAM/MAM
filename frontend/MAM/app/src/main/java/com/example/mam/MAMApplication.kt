package com.example.mam

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.mam.data.UserPreferencesRepository
import com.example.mam.repository.retrofit.BaseRepository

//Để quản lý (khời tạo) dataStore
private const val TOKEN_MANAGER = "Token_Manager"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = TOKEN_MANAGER
)

class MAMApplication: Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var baseRepository: BaseRepository
    override fun onCreate() {
        super.onCreate()
        // Initialize any global resources or configurations here
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        baseRepository = BaseRepository(userPreferencesRepository)
    }
}
