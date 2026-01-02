package com.example.futurememailapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

// =====================================================
// 1) 資料模型（UI 用）
// =====================================================

/**
 * Letter
 *
 * 功能說明：
 * 此資料類別為「前端 UI 顯示用」的信件模型，供 OverviewActivity / RecyclerView 使用。
 *
 * 欄位說明：
 * - id：信件唯一識別碼（通常對應後端資料 id）
 * - subject：主旨
 * - content：內容（點入詳細頁會用到）
 * - writeDate：寫信日期（字串格式 yyyy-MM-dd）
 * - deliveryDate：寄送 / 收信日期（字串格式 yyyy-MM-dd）
 * - isRead：是否已讀（控制列表左側未讀紅點顯示）
 *
 * 注意：
 * - 這裡是「UI 模型」，不一定與 Network DTO (MailsResponse) 完全相同
 */
data class Letter(
    val id: String,
    val subject: String,
    val content: String,
    val writeDate: String,
    val deliveryDate: String,
    var isRead: Boolean = false
)

// =====================================================
// 2) RecyclerView Adapter（信件列表）
// =====================================================

/**
 * LetterAdapter
 *
 * 功能說明：
 * 用於在 RecyclerView 中顯示信件列表，每個 item 對應 letter_item.xml
 *
 * 設計重點：
 * - onItemClicked：點擊事件由外部傳入（Activity 決定要做什麼）
 * - isRead 控制未讀點顯示：已讀 -> 隱藏；未讀 -> 顯示
 */
class LetterAdapter(
    private val letters: List<Letter>,
    private val onItemClicked: (Letter) -> Unit // 由外層決定點擊後行為（例如進入 Detail）
) : RecyclerView.Adapter<LetterAdapter.LetterViewHolder>() {

    /**
     * LetterViewHolder
     * 用於快取 item view 內的 UI 元件，避免重複 findViewById 提升效能
     */
    class LetterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val unreadDotImageView: ImageView = itemView.findViewById(R.id.unreadDotImageView)
        val subjectTextView: TextView = itemView.findViewById(R.id.letterSubjectTextView)
        val writeDateTextView: TextView = itemView.findViewById(R.id.writeDateTextView)
        val deliveryDateTextView: TextView = itemView.findViewById(R.id.deliveryDateTextView)
    }

    /**
     * onCreateViewHolder
     * 建立每個列表項目的 View（inflate letter_item.xml）
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.letter_item, parent, false)
        return LetterViewHolder(view)
    }

    /**
     * RecyclerView 的 item 數量
     */
    override fun getItemCount() = letters.size

    /**
     * onBindViewHolder
     * 將資料綁到 UI 上（每一列顯示主旨、日期、未讀點）
     */
    override fun onBindViewHolder(holder: LetterViewHolder, position: Int) {
        val letter = letters[position]

        // 主旨與日期顯示
        holder.subjectTextView.text = letter.subject
        holder.writeDateTextView.text = "寫於: ${letter.writeDate}"
        holder.deliveryDateTextView.text = "寄送: ${letter.deliveryDate}"

        // 未讀點顯示邏輯：
        // - isRead = true  -> 不顯示（INVISIBLE）
        // - isRead = false -> 顯示（VISIBLE）
        holder.unreadDotImageView.visibility =
            if (letter.isRead) View.INVISIBLE else View.VISIBLE

        // 點擊事件交由外部傳入的 lambda 處理
        holder.itemView.setOnClickListener {
            onItemClicked(letter)
        }
    }
}

// =====================================================
// 3) 日曆打點裝飾器（Calendar Decorator）
// =====================================================

/**
 * EventDecorator
 *
 * 功能說明：
 * 用於 MaterialCalendarView 的日期裝飾器（DayViewDecorator），
 * 將指定的日期集合加上「點點」提示（DotSpan），用來表示：
 * - 未來會收到信件的日期（future letters）
 *
 * @param color 點點顏色
 * @param dates 需要被打點的日期集合
 */
class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {

    // 使用 HashSet 加快 contains 查詢效能
    private val dates: HashSet<CalendarDay> = HashSet(dates)

    /**
     * shouldDecorate
     * 判斷某一天是否要套用裝飾（是否在 dates 集合中）
     */
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    /**
     * decorate
     * 對該日期的 cell 加上 DotSpan（5f 表示點點大小）
     */
    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(5f, color))
    }
}

// =====================================================
// 4) OverviewActivity（收信總覽主頁）
// =====================================================

/**
 * OverviewActivity
 *
 * 功能說明：
 * 「收信總覽」頁面，提供：
 * 1) 月曆顯示未來信件的寄送日期（打點提示）
 * 2) 信箱列表顯示已到期/已可閱讀的信件（RecyclerView）
 * 3) 排序功能（Dialog：依收信日期/撰寫日期、新到舊/舊到新）
 * 4) 點擊信件進入 LetterDetailActivity 顯示完整內容
 * 5) 未讀狀態（點擊後 markAsRead）
 * 6) loading 與 error 狀態處理（progressBar / Toast）
 *
 * 架構：
 * - 使用 viewModels() 取得 OverviewViewModel（MVVM）
 * - 透過 LiveData observer 更新 UI
 */
class OverviewActivity : AppCompatActivity() {

    // 使用 AndroidX 的 viewModels() 委派取得 ViewModel（生命週期感知）
    private val viewModel: OverviewViewModel by viewModels()

    // UI 元件
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSort: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView

    // 日期解析格式（字串 yyyy-MM-dd）
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 載入收信總覽頁面的 XML
        setContentView(R.layout.activity_overview)

        // =========================
        // 1) 初始化畫面元件
        // =========================
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.lettersRecyclerView)
        btnSort = findViewById(R.id.btnSort)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)

        // 點擊排序按鈕 → 顯示排序選項 Dialog
        btnSort.setOnClickListener { showSortDialog() }

        // 設定 LiveData 監聽（資料/載入/錯誤）
        setupObservers()

        // 設定底部導覽列
        setupBottomNavigation()
    }

    /**
     * onResume
     *
     * 功能說明：
     * 每次回到此頁時都重新載入信件資料（確保內容是最新的）
     * - 例如從 Detail 返回後，已讀狀態可能更新
     */
    override fun onResume() {
        super.onResume()
        viewModel.loadLetters()
    }

    /**
     * setupObservers
     *
     * 功能說明：
     * 監聽 ViewModel 的 LiveData，當資料/狀態改變時更新畫面
     * - letters：更新日曆打點與列表
     * - isLoading：顯示/隱藏進度條與內容
     * - errorMessage：顯示錯誤 Toast
     */
    private fun setupObservers() {

        // 信件列表資料變更 → 更新日曆與 RecyclerView
        viewModel.letters.observe(this) { letters ->
            setupCalendar(letters)
            setupRecyclerView(letters)
        }

        // 讀取狀態 → 顯示進度條，避免使用者看到半套 UI
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.isVisible = isLoading
            if (isLoading) {
                recyclerView.isVisible = false
                emptyView.isVisible = false
            }
        }

        // 錯誤訊息 → Toast 提示
        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * showSortDialog
     *
     * 功能說明：
     * 顯示單選排序方式 Dialog，
     * 使用 viewModel.currentSortIndex 記錄目前排序選項並同步 UI。
     */
    private fun showSortDialog() {

        // 提供四種排序模式（對應 currentSortIndex 0~3）
        val sortOptions = arrayOf(
            "依收信日期 (新到舊)",
            "依收信日期 (舊到新)",
            "依撰寫日期 (新到舊)",
            "依撰寫日期 (舊到新)"
        )

        AlertDialog.Builder(this)
            .setTitle("選擇排序方式")
            .setSingleChoiceItems(sortOptions, viewModel.currentSortIndex) { dialog, which ->
                // 將選到的 index 存入 ViewModel（由 ViewModel 控制排序狀態）
                viewModel.setSortIndex(which)
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * setupCalendar
     *
     * 功能說明：
     * 在 MaterialCalendarView 上「打點」顯示未來會收到信件的日期
     *
     * 邏輯：
     * - 找出 deliveryDate 在「今天之後」的信件（futureLetters）
     * - 將 deliveryDate 轉換成 CalendarDay
     * - 使用 EventDecorator 加上 DotSpan
     */
    private fun setupCalendar(letters: List<Letter>) {

        // 先移除舊的 decorators，避免重複打點疊加
        calendarView.removeDecorators()

        val today = LocalDate.now()

        // 只挑「未來」才會寄送的信件來打點
        val futureLetters = letters.filter { letter ->
            try {
                val deliveryDate = LocalDate.parse(letter.deliveryDate, formatter)
                deliveryDate.isAfter(today)
            } catch (e: Exception) {
                // 日期解析失敗時忽略，避免崩潰
                false
            }
        }

        // 將 futureLetters 的寄送日期轉成 CalendarDay
        val deliveryDates = futureLetters.mapNotNull { letter ->
            try {
                val localDate = LocalDate.parse(letter.deliveryDate, formatter)
                CalendarDay.from(localDate.year, localDate.monthValue, localDate.dayOfMonth)
            } catch (e: Exception) {
                null
            }
        }

        // 若有要打點的日期才加 decorator
        if (deliveryDates.isNotEmpty()) {
            calendarView.addDecorator(EventDecorator(Color.RED, deliveryDates))
        }
    }

    /**
     * setupRecyclerView
     *
     * 功能說明：
     * 建立信件列表（RecyclerView），顯示「已到期可閱讀」的信件，
     * 並依選擇的排序方式排序後顯示。
     *
     * 邏輯：
     * - receivedLetters：deliveryDate <= today 的信件（今天或以前）
     * - sortedLetters：依 currentSortIndex 進行排序
     * - 空列表：顯示 emptyView
     * - 點擊 item：標記已讀 + 跳轉至 LetterDetailActivity
     */
    private fun setupRecyclerView(letters: List<Letter>) {

        val today = LocalDate.now()

        // 只顯示「已到期」的信件（今天或以前）
        val receivedLetters = letters.filter { letter ->
            try {
                val deliveryDate = LocalDate.parse(letter.deliveryDate, formatter)
                !deliveryDate.isAfter(today) // deliveryDate <= today
            } catch (e: Exception) {
                false
            }
        }
        // 根據 ViewModel 記錄的排序 index 進行排序
        val sortedLetters = when (viewModel.currentSortIndex) {
            0 -> receivedLetters.sortedByDescending { it.deliveryDate } // 收信日期 新到舊
            1 -> receivedLetters.sortedBy { it.deliveryDate }           // 收信日期 舊到新
            2 -> receivedLetters.sortedByDescending { it.writeDate }    // 撰寫日期 新到舊
            3 -> receivedLetters.sortedBy { it.writeDate }              // 撰寫日期 舊到新
            else -> receivedLetters.sortedByDescending { it.deliveryDate }
        }

        // 空狀態 UI：
        // - 沒有已到期信件：隱藏列表、顯示 emptyView
        if (sortedLetters.isEmpty()) {
            recyclerView.isVisible = false
            emptyView.isVisible = true
        } else {
            recyclerView.isVisible = true
            emptyView.isVisible = false
        }

        // 建立 Adapter，並處理 item 點擊事件
        val adapter = LetterAdapter(sortedLetters) { clickedLetter ->

            // 點擊後標記為已讀（由 ViewModel 負責狀態管理）
            viewModel.markAsRead(clickedLetter.id)

            // 跳轉至信件內容頁（Detail），並透過 Intent Extras 傳遞資料
            val intent = Intent(this, LetterDetailActivity::class.java)
            intent.putExtra(LetterDetailActivity.EXTRA_SUBJECT, clickedLetter.subject)
            intent.putExtra(LetterDetailActivity.EXTRA_CONTENT, clickedLetter.content)
            intent.putExtra(LetterDetailActivity.EXTRA_WRITE_DATE, clickedLetter.writeDate)
            intent.putExtra(LetterDetailActivity.EXTRA_DELIVERY_DATE, clickedLetter.deliveryDate)
            startActivity(intent)
        }

        // 設定 RecyclerView 基本配置
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * setupBottomNavigation
     *
     * 功能說明：
     * 設定底部導覽列，使使用者可在 Home / Instruct / Write / Overview 間切換。
     */
    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // 預設選中 Overview（nav4）
        bottomNavigationView.selectedItemId = R.id.nav4

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav1 -> { // Home
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav2 -> { // Instruct
                    startActivity(Intent(this, InstructActivity::class.java))
                    true
                }
                R.id.nav3 -> { // Write
                    startActivity(Intent(this, WriteActivity::class.java))
                    true
                }
                R.id.nav4 -> { // Overview（當前頁，不跳）
                    true
                }
                else -> false
            }
        }
    }
}
