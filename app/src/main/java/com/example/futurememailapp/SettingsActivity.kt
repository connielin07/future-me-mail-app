package com.example.futurememailapp

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.futurememailapp.utils.LanguageHelper
import com.example.futurememailapp.utils.ThemeHelper
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 一進來先套用主題與語言
        ThemeHelper.applySavedTheme(this)
        LanguageHelper.applySavedLanguage(this)

        setContentView(R.layout.activity_settings)

        // ---------- Toolbar（瀏海頁） ----------
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // ---------- View ----------
        val btnToggleTheme = findViewById<Button>(R.id.btnToggleTheme)
        val rgLanguage = findViewById<RadioGroup>(R.id.rgLanguage)
        val rbChinese = findViewById<RadioButton>(R.id.rbChinese)
        val rbEnglish = findViewById<RadioButton>(R.id.rbEnglish)

        // ---------- 語言初始化 ----------
        when (LanguageHelper.getSavedLanguage(this)) {
            "en" -> rbEnglish.isChecked = true
            else -> rbChinese.isChecked = true
        }

        // ---------- 日 / 夜切換 ----------
        btnToggleTheme.setOnClickListener {
            ThemeHelper.toggleTheme(this)
        }

        // ---------- 中 / 英切換 ----------
        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbChinese -> {
                    LanguageHelper.setLanguage(this, "zh-Hant")
                    recreate()
                }
                R.id.rbEnglish -> {
                    LanguageHelper.setLanguage(this, "en")
                    recreate()
                }
            }
        }
    }
}
