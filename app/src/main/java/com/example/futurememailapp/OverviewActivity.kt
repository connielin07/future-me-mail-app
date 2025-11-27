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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futurememailapp.network.FutureMailApi
import com.example.futurememailapp.network.model.MailsResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

// 修正：在 Letter 中加入 id，用來追蹤讀取狀態
data class Letter(
    val id: String,
    val subject: String,
    val content: String,
    val writeDate: String, 
    val deliveryDate: String,
    var isRead: Boolean = false
)

class LetterAdapter(
    private val letters: List<Letter>,
    private val onItemClicked: (Letter, Int) -> Unit
) : RecyclerView.Adapter<LetterAdapter.LetterViewHolder>() {

    class LetterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val unreadDotImageView: ImageView = itemView.findViewById(R.id.unreadDotImageView)
        val subjectTextView: TextView = itemView.findViewById(R.id.letterSubjectTextView)
        val writeDateTextView: TextView = itemView.findViewById(R.id.writeDateTextView)
        val deliveryDateTextView: TextView = itemView.findViewById(R.id.deliveryDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LetterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.letter_item, parent, false)
        return LetterViewHolder(view)
    }

    override fun getItemCount() = letters.size

    override fun onBindViewHolder(holder: LetterViewHolder, position: Int) {
        val letter = letters[position]
        holder.subjectTextView.text = letter.subject
        holder.writeDateTextView.text = "寫於: ${letter.writeDate}"
        holder.deliveryDateTextView.text = "寄送: ${letter.deliveryDate}"
        holder.unreadDotImageView.visibility = if (letter.isRead) View.INVISIBLE else View.VISIBLE

        holder.itemView.setOnClickListener {
            onItemClicked(letter, holder.adapterPosition)
        }
    }
}

// --- 日曆打點的裝飾器 ---
class EventDecorator(private val color: Int, dates: Collection<CalendarDay>) : DayViewDecorator {
    private val dates: HashSet<CalendarDay> = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(5f, color))
    }
}

// --- 主頁面 ---
class OverviewActivity : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnSort: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val futureMailService by lazy { FutureMailApi.service }

    private var allLetters: List<Letter> = emptyList() 
    private var currentSortIndex = 0 
    private val readLetterIds = mutableSetOf<String>() // 新增：用來記錄已讀信件的 ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.lettersRecyclerView)
        btnSort = findViewById(R.id.btnSort)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)

        btnSort.setOnClickListener { showSortDialog() }

        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        loadLettersFromApi()
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "依收信日期 (新到舊)",
            "依收信日期 (舊到新)",
            "依撰寫日期 (新到舊)",
            "依撰寫日期 (舊到新)"
        )

        AlertDialog.Builder(this)
            .setTitle("選擇排序方式")
            .setSingleChoiceItems(sortOptions, currentSortIndex) { dialog, which ->
                currentSortIndex = which
                setupRecyclerView(allLetters)
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun loadLettersFromApi() {
        progressBar.isVisible = true
        recyclerView.isVisible = false
        emptyView.isVisible = false

        lifecycleScope.launch {
            try {
                val response = futureMailService.getMails()
                if (response.isSuccessful) {
                    val lettersFromApi = response.body() ?: emptyList()
                    allLetters = lettersFromApi.map { apiLetter ->
                        Letter(
                            id = apiLetter.id,
                            subject = apiLetter.subject,
                            content = apiLetter.content,
                            writeDate = apiLetter.writeDate.substring(0, 10),
                            deliveryDate = apiLetter.receiveDate.substring(0, 10),
                            // 修正：從 readLetterIds 恢復已讀狀態
                            isRead = readLetterIds.contains(apiLetter.id)
                        )
                    }
                } else {
                    Toast.makeText(this@OverviewActivity, "讀取信件失敗: ${response.code()}", Toast.LENGTH_SHORT).show()
                    allLetters = emptyList()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OverviewActivity, "讀取信件失敗: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                allLetters = emptyList()
            } finally {
                progressBar.isVisible = false
                setupCalendar(allLetters)
                setupRecyclerView(allLetters)
            }
        }
    }

    private fun setupCalendar(letters: List<Letter>) {
        calendarView.removeDecorators()
        
        val deliveryDates = letters.mapNotNull { letter ->
            try {
                val localDate = LocalDate.parse(letter.deliveryDate, formatter)
                CalendarDay.from(localDate.year, localDate.monthValue, localDate.dayOfMonth)
            } catch (e: Exception) {
                null
            }
        }

        if (deliveryDates.isNotEmpty()) {
            calendarView.addDecorator(EventDecorator(Color.RED, deliveryDates))
        }
    }

    private fun setupRecyclerView(letters: List<Letter>) {
        val today = LocalDate.now()
        val receivedLetters = letters.filter { letter ->
            try {
                val deliveryDate = LocalDate.parse(letter.deliveryDate, formatter)
                !deliveryDate.isAfter(today)
            } catch (e: Exception) {
                false
            }
        }
        
        val sortedLetters = when (currentSortIndex) {
            0 -> receivedLetters.sortedByDescending { it.deliveryDate }
            1 -> receivedLetters.sortedBy { it.deliveryDate }
            2 -> receivedLetters.sortedByDescending { it.writeDate }
            3 -> receivedLetters.sortedBy { it.writeDate }
            else -> receivedLetters.sortedByDescending { it.deliveryDate }
        }

        if (sortedLetters.isEmpty()) {
            recyclerView.isVisible = false
            emptyView.isVisible = true
        } else {
            recyclerView.isVisible = true
            emptyView.isVisible = false
        }

        val adapter = LetterAdapter(sortedLetters) { clickedLetter, position ->
            // 修正：將已讀狀態儲存到 readLetterIds 中
            if (!clickedLetter.isRead) {
                clickedLetter.isRead = true
                readLetterIds.add(clickedLetter.id)
                (recyclerView.adapter as? LetterAdapter)?.notifyItemChanged(position)
            }

            val intent = Intent(this, LetterDetailActivity::class.java)
            intent.putExtra(LetterDetailActivity.EXTRA_SUBJECT, clickedLetter.subject)
            intent.putExtra(LetterDetailActivity.EXTRA_CONTENT, clickedLetter.content)
            intent.putExtra(LetterDetailActivity.EXTRA_WRITE_DATE, clickedLetter.writeDate)
            intent.putExtra(LetterDetailActivity.EXTRA_DELIVERY_DATE, clickedLetter.deliveryDate)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.nav4

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId != bottomNavigationView.selectedItemId) {
                val intent = when (item.itemId) {
                    R.id.nav1 -> Intent(this, MainActivity::class.java)
                    R.id.nav2 -> Intent(this, InstructActivity::class.java)
                    R.id.nav3 -> Intent(this, WriteActivity::class.java)
                    else -> null
                }
                intent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(it)
                }
            }
            true
        }
    }
}