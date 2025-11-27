package com.example.futurememailapp

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.futurememailapp.network.FutureMailApi
import kotlinx.coroutines.launch

class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    private val futureMailService by lazy { FutureMailApi.service }
    private val sharedPreferences = application.getSharedPreferences("FutureMeMailApp", Context.MODE_PRIVATE)

    // --- LiveData --- (可以被 Activity 觀察的資料)
    private val _letters = MutableLiveData<List<Letter>>()
    val letters: LiveData<List<Letter>> = _letters

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // --- 資料與狀態 ---
    private val readLetterIds = mutableSetOf<String>()
    var currentSortIndex = 0
        private set

    init {
        // ViewModel 初始化時，從 SharedPreferences 載入已讀狀態
        loadReadLetterIds()
    }

    private fun loadReadLetterIds() {
        val ids = sharedPreferences.getStringSet("read_letter_ids", emptySet()) ?: emptySet()
        readLetterIds.addAll(ids)
    }

    private fun saveReadLetterIds() {
        with(sharedPreferences.edit()) {
            putStringSet("read_letter_ids", readLetterIds)
            apply()
        }
    }

    fun loadLetters() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = futureMailService.getMails()
                if (response.isSuccessful) {
                    val lettersFromApi = response.body() ?: emptyList()
                    _letters.value = lettersFromApi.map { apiLetter ->
                        Letter(
                            id = apiLetter.id,
                            subject = apiLetter.subject,
                            content = apiLetter.content,
                            writeDate = apiLetter.writeDate.substring(0, 10),
                            deliveryDate = apiLetter.receiveDate.substring(0, 10),
                            isRead = readLetterIds.contains(apiLetter.id)
                        )
                    }
                } else {
                    _errorMessage.value = "讀取信件失敗: ${response.code()}"
                    _letters.value = emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "讀取信件失敗: ${e.localizedMessage}"
                _letters.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSortIndex(index: Int) {
        currentSortIndex = index
        _letters.value = _letters.value 
    }

    fun markAsRead(letterId: String) {
        if (!readLetterIds.contains(letterId)) {
            readLetterIds.add(letterId)
            saveReadLetterIds() // 每次更新時，都回存到 SharedPreferences

            _letters.value = _letters.value?.map { 
                if (it.id == letterId) it.copy(isRead = true) else it
            }
        }
    }
}
