package com.example.futurememailapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futurememailapp.network.FutureMailApi
import com.example.futurememailapp.network.model.MailsResponse
import kotlinx.coroutines.launch

class OverviewViewModel : ViewModel() {

    private val futureMailService by lazy { FutureMailApi.service }

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
        private set // 只允許 ViewModel 內部修改

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
        // 觸發 letters 更新，讓 Activity 重新排序
        _letters.value = _letters.value 
    }

    fun markAsRead(letterId: String) {
        if (!readLetterIds.contains(letterId)) {
            readLetterIds.add(letterId)
            // 更新 LiveData，讓 Activity 更新畫面
            _letters.value = _letters.value?.map { 
                if (it.id == letterId) it.copy(isRead = true) else it
            }
        }
    }
}
