package com.fxanhkhoa.what_to_eat_android.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fxanhkhoa.what_to_eat_android.model.UserDishCollectionModel
import com.fxanhkhoa.what_to_eat_android.network.RetrofitProvider
import com.fxanhkhoa.what_to_eat_android.services.UserDishCollectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserDishCollectionViewModel : ViewModel() {

    private val service = RetrofitProvider.createService<UserDishCollectionService>()

    private val _collections = MutableStateFlow<List<UserDishCollectionModel>>(emptyList())
    val collections: StateFlow<List<UserDishCollectionModel>> = _collections.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadCollections(userId: String) {
        if (_isLoading.value) return
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = service.findAll(mapOf("userId" to userId))
                _collections.value = response.data ?: emptyList()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
                Log.e("UserDishCollectionVM", "loadCollections error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCollection(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                service.delete(id)
                _collections.value = _collections.value.filter { it.id != id }
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage
                Log.e("UserDishCollectionVM", "deleteCollection error", e)
            }
        }
    }
}
