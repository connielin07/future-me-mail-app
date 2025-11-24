package com.example.futurememailapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

// 1. 更新 Letter 資料模型，加入 writeDate
data class Letter(
    val subject: String,
    val writeDate: String, // 新增的撰寫日期
    val deliveryDate: String
)

// 2. 更新 Adapter 來顯示新的日期
class LetterAdapter(
    private val letters: List<Letter>,
    private val onItemClicked: (Letter) -> Unit
) : RecyclerView.Adapter<LetterAdapter.LetterViewHolder>() {

    class LetterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectTextView: TextView = itemView.findViewById(R.id.letterSubjectTextView)
        val writeDateTextView: TextView = itemView.findViewById(R.id.writeDateTextView) // 新的 TextView
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
        holder.writeDateTextView.text = "寫於: ${letter.writeDate}" // 設定撰寫日期的文字
        holder.deliveryDateTextView.text = "寄送: ${letter.deliveryDate}" // 順便為寄送日期加上前綴

        holder.itemView.setOnClickListener {
            onItemClicked(letter)
        }
    }
}


class OverviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        val recyclerView: RecyclerView = findViewById(R.id.lettersRecyclerView)

        // 3. 更新假資料，為每一封信都加上撰寫日期
        val fakeLetters = listOf(
            Letter("給十年後自己的信", "2024-05-20", "2034-12-25"),
            Letter("關於夢想", "2024-01-01", "2025-01-01"),
            Letter("2024 年的總結", "2023-12-31", "2024-12-31"),
            Letter("生日快樂！", "2024-08-10", "2026-08-15"),
            Letter("一項秘密計畫", "2024-07-01", "2027-07-07"),
            Letter("給家人的話", "2023-05-20", "2028-05-20"),
            Letter("新工作的期許", "2024-03-01", "2025-03-15"),
            Letter("環球旅行計畫", "2022-09-10", "2030-09-10"),
            Letter("買房子的那天", "2024-11-11", "2029-11-11"),
            Letter("寵物的回憶", "2023-06-01", "2026-06-01"),
            Letter("一封道歉信", "2024-02-10", "2025-02-14"),
            Letter("給自己的挑戰", "2023-11-30", "2024-11-30"),
            Letter("學習新技能的紀錄", "2024-01-15", "2026-01-15"),
            Letter("第一輛車", "2024-03-20", "2027-04-01"),
            Letter("對未來的想像", "2020-01-01", "2040-01-01"),
            Letter("簡單的日常", "2024-07-15", "2025-07-22"),
            Letter("健康檢查提醒", "2024-10-01", "2028-10-05"),
            Letter("投資理財目標", "2021-01-01", "2035-01-01"),
            Letter("給朋友的祝福", "2024-09-01", "2026-09-30"),
            Letter("再次回到這裡", "2022-02-28", "2032-02-29")
        )

        val adapter = LetterAdapter(fakeLetters) { clickedLetter ->
            val intent = Intent(this, LetterDetailActivity::class.java)
            intent.putExtra(LetterDetailActivity.EXTRA_SUBJECT, clickedLetter.subject)
            // 加上要傳遞的 writeDate
            intent.putExtra(LetterDetailActivity.EXTRA_WRITE_DATE, clickedLetter.writeDate)
            intent.putExtra(LetterDetailActivity.EXTRA_DELIVERY_DATE, clickedLetter.deliveryDate)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // --- 底部導覽列 --- 
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
