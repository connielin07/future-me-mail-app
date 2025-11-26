package com.example.futurememailapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

// --- 資料模型與 Adapter (與之前相同) ---
data class Letter(
    val subject: String,
    val writeDate: String, 
    val deliveryDate: String
)

class LetterAdapter(
    private val letters: List<Letter>,
    private val onItemClicked: (Letter) -> Unit
) : RecyclerView.Adapter<LetterAdapter.LetterViewHolder>() {

    class LetterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        holder.itemView.setOnClickListener {
            onItemClicked(letter)
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
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        // 初始化畫面元件
        calendarView = findViewById(R.id.calendarView)
        recyclerView = findViewById(R.id.lettersRecyclerView)

        // 讀取資料並更新畫面
        val letters = loadLetters()
        setupCalendar(letters)
        setupRecyclerView(letters)

        // 設定底部導覽列
        setupBottomNavigation()
    }

    /**
     * 載入信件資料。
     * 未來這個函式會改成從資料庫讀取真實資料。
     */
    private fun loadLetters(): List<Letter> {
        return listOf(
            Letter("給十年後自己的信", "2024-05-20", "2034-12-25"),
            Letter("關於夢想", "2024-01-01", "2025-01-01"),
            Letter("2024 年的總結", "2023-12-31", "2024-12-31"),
            Letter("生日快樂！", "2024-08-10", "2026-08-15"),
            Letter("一項秘密計畫", "2024-07-01", "2027-07-07"),
            Letter("給家人的話", "2023-05-20", "2028-05-20"),
            Letter("新工作的期許", "2024-03-01", "2025-03-15"),
            Letter("環球旅行計畫", "2022-09-10", "2030-09-10"),
            Letter("買房子的那天", "2024-11-11", "2029-11-11"),
            Letter("寵物的回憶", "2023-06-01", "2026-06-01")
            // ... 其他信件
        )
    }

    /**
     * 設定日曆，為有信件的日期加上打點。
     */
    private fun setupCalendar(letters: List<Letter>) {
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

    /**
     * 設定信箱列表，只顯示已到期的信件。
     */
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
        
        val sortedLetters = receivedLetters.sortedByDescending { it.deliveryDate }

        val adapter = LetterAdapter(sortedLetters) { clickedLetter ->
            val intent = Intent(this, LetterDetailActivity::class.java)
            intent.putExtra(LetterDetailActivity.EXTRA_SUBJECT, clickedLetter.subject)
            intent.putExtra(LetterDetailActivity.EXTRA_WRITE_DATE, clickedLetter.writeDate)
            intent.putExtra(LetterDetailActivity.EXTRA_DELIVERY_DATE, clickedLetter.deliveryDate)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * 設定底部導覽列的點擊事件。
     */
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