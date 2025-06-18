package com.example.brainlog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.brainlog.model.ApiClient
import com.example.brainlog.model.MediaRepository
import com.example.brainlog.model.RawgApiClient

class ProfileScreenViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileScreenViewModel::class.java)) {
            // Erstelle hier das MediaRepository mit beiden API-Services
            val mediaRepository = MediaRepository(
                mediaApi = ApiClient.mediaApi,     // Dein TMDB-Service
                rawgApi = RawgApiClient.rawgApi   // Dein RAWG-Service
            )
            // Übergebe das initialisierte Repository an das ViewModel
            return ProfileScreenViewModel(
                mediaRepository = mediaRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}