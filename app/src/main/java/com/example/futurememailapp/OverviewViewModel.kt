package com.example.futurememailapp

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.futurememailapp.network.FutureMailApi
import kotlinx.coroutines.launch

/**
 * OverviewViewModel
 *
 * 功能說明：
 * 「收信總覽（Overview）」頁的 ViewModel，負責管理：
 * 1) 從後端取得信件資料（Retrofit API：FutureMailApi.service.getMails）
 * 2) 將後端回傳資料轉換成 UI 使用的 Letter 模型
 * 3) Loading / Error 狀態（供 Activity 顯示 progressBar 與 Toast）
 * 4) 已讀狀態管理（透過 SharedPreferences 儲存 read_letter_ids）
 * 5) 排序狀態索引 currentSortIndex（由 Activity 決定實際排序呈現）
 *
 * 為什麼使用 AndroidViewModel：
 * - AndroidViewModel 內建 application，可用於取得 Context
 * - 本例需要 Context 來讀取 SharedPreferences，因此適合使用 AndroidViewModel
 */
class OverviewViewModel(application: Application) : AndroidViewModel(application) {

    // =========================
    // 1) 網路服務與本機儲存
    // =========================

    /**
     * futureMailService
     * 後端 API 服務介面（Retrofit）
     * 使用 lazy：第一次需要時才建立，避免過早初始化
     */
    private val futureMailService by lazy { FutureMailApi.service }

    /**
     * sharedPreferences
     * 用來存放「已讀信件 id」集合，讓已讀狀態在 App 重啟後仍保留
     */
    private val sharedPreferences =
        application.getSharedPreferences("FutureMeMailApp", Context.MODE_PRIVATE)

    // =========================
    // 2) LiveData（Activity 可以 observe 的資料）
    // =========================

    /**
     * _letters（MutableLiveData）
     * 內部可修改、對外用 letters（LiveData）唯讀曝光，符合封裝原則
     */
    private val _letters = MutableLiveData<List<Letter>>()
    val letters: LiveData<List<Letter>> = _letters

    /**
     * _isLoading
     * 控制畫面 loading 狀態（顯示 progressBar、隱藏列表）
     */
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * _errorMessage
     * 發生錯誤時，提供 Activity 顯示 Toast 的訊息
     * （成功後通常可由 Activity 自行清理或忽略）
     */
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // =========================
    // 3) ViewModel 內部狀態
    // =========================

    /**
     * readLetterIds
     * 已讀信件 id 的集合（存在記憶體中）
     * - ViewModel 初始化時會從 SharedPreferences 載入
     * - 每次 markAsRead 時會更新並回存
     */
    private val readLetterIds = mutableSetOf<String>()

    /**
     * currentSortIndex
     * 目前選擇的排序方式（對應 OverviewActivity 的 Dialog 選項）
     * 0~3：由 Activity 決定對應的排序行為
     *
     * private set：外部只能讀，不能直接改，必須透過 setSortIndex()
     */
    var currentSortIndex = 0
        private set

    // =========================
    // 4) 初始化：載入已讀狀態
    // =========================

    init {
        // ViewModel 初始化時，先把已讀 id 從 SharedPreferences 讀進來
        loadReadLetterIds()
    }

    /**
     * loadReadLetterIds
     *
     * 功能：
     * 從 SharedPreferences 讀取 "read_letter_ids" 字串集合，
     * 並填入 readLetterIds（記憶體集合）
     */
    private fun loadReadLetterIds() {
        val ids = sharedPreferences.getStringSet("read_letter_ids", emptySet()) ?: emptySet()
        readLetterIds.addAll(ids)
    }

    /**
     * saveReadLetterIds
     *
     * 功能：
     * 將目前 readLetterIds（已讀信件集合）寫回 SharedPreferences，
     * 以達成 App 重啟後仍能保留已讀狀態的「持久化」效果
     */
    private fun saveReadLetterIds() {
        with(sharedPreferences.edit()) {
            putStringSet("read_letter_ids", readLetterIds)
            apply()
        }
    }

    // =========================
    // 5) 載入信件資料（呼叫 API）
    // =========================

    /**
     * loadLetters
     *
     * 功能：
     * 呼叫後端 API 取得所有信件資料，並更新 LiveData：
     * - _isLoading：載入中 true / 結束 false
     * - _letters：成功時寫入轉換後的 Letter 列表
     * - _errorMessage：失敗時提供錯誤訊息
     *
     * 非同步：
     * - 使用 viewModelScope.launch 在協程中執行網路請求
     * - 避免阻塞主執行緒（UI Thread）
     */
    fun loadLetters() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 呼叫 Retrofit API：取得信件列表
                val response = futureMailService.getMails()

                if (response.isSuccessful) {
                    // 成功：取出 body（若為 null 則用空列表）
                    val lettersFromApi = response.body() ?: emptyList()

                    // 將後端 DTO（MailsResponse）轉換成 UI 模型（Letter）
                    // 並套用「已讀狀態」：isRead = readLetterIds.contains(id)
                    _letters.value = lettersFromApi.map { apiLetter ->
                        Letter(
                            id = apiLetter.id,
                            subject = apiLetter.subject,
                            content = apiLetter.content,

                            // writeDate / receiveDate 可能包含時間字串，例如 2025-01-01T12:34:56
                            // substring(0,10) 取前 10 碼保留 yyyy-MM-dd 給 UI 顯示
                            writeDate = apiLetter.writeDate.substring(0, 10),
                            deliveryDate = apiLetter.receiveDate.substring(0, 10),

                            // 已讀狀態由本機儲存判斷
                            isRead = readLetterIds.contains(apiLetter.id)
                        )
                    }
                } else {
                    // API 回應成功但狀態碼不是 2xx
                    _errorMessage.value = "讀取信件失敗: ${response.code()}"
                    _letters.value = emptyList()
                }
            } catch (e: Exception) {
                // 網路/解析/其他例外錯誤
                _errorMessage.value = "讀取信件失敗: ${e.localizedMessage}"
                _letters.value = emptyList()
            } finally {
                // 無論成功或失敗，最後都要關閉 loading
                _isLoading.value = false
            }
        }
    }

    // =========================
    // 6) 排序狀態更新
    // =========================

    /**
     * setSortIndex
     *
     * 功能：
     * 更新目前排序選項的 index。
     *
     * 這裡透過 `_letters.value = _letters.value` 觸發 LiveData 更新，
     * 讓 Activity 重新走 observe 回呼並重建畫面排序結果。
     * （排序實際在 Activity 的 setupRecyclerView() 中執行）
     */
    fun setSortIndex(index: Int) {
        currentSortIndex = index

        // 重新指派自身以觸發 observers（讓 UI 重新根據 currentSortIndex 排序）
        _letters.value = _letters.value
    }

    // =========================
    // 7) 已讀狀態更新
    // =========================

    /**
     * markAsRead
     *
     * 功能：
     * 將指定信件標記為已讀，並同步更新：
     * 1) readLetterIds（記憶體集合）
     * 2) SharedPreferences（持久化存檔）
     * 3) _letters LiveData（更新 UI，隱藏未讀點）
     *
     * @param letterId 要標記為已讀的信件 id
     */
    fun markAsRead(letterId: String) {
        // 若尚未讀過才需要更新（避免重複寫入）
        if (!readLetterIds.contains(letterId)) {
            readLetterIds.add(letterId)

            // 每次更新已讀狀態後都立即存檔，確保退出 App 也不會遺失
            saveReadLetterIds()

            // 更新 LiveData：把對應 id 的 Letter 複製成 isRead = true
            // 使用 copy() 產生新物件，避免直接改動舊資料造成狀態不一致
            _letters.value = _letters.value?.map {
                if (it.id == letterId) it.copy(isRead = true) else it
            }
        }
    }
}
