package com.example.futurememailapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton

class OverviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)

        val header1: MaterialButton = findViewById(R.id.header1)
        val content1: NestedScrollView = findViewById(R.id.content1)

        header1.setOnClickListener {
            val expanded = content1.isVisible
            content1.isVisible = !expanded
            header1.icon = ContextCompat.getDrawable(
                this,
                if (expanded) R.drawable.ic_expand_more_24dp else R.drawable.ic_expand_less_24dp
            )
        }
    }
}
