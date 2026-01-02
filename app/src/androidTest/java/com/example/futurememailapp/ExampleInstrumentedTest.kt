package com.example.futurememailapp

// InstrumentationRegistry：用來取得「實際裝置或模擬器」上的 App Context
import androidx.test.platform.app.InstrumentationRegistry

// AndroidJUnit4：Android 官方提供的測試 Runner，負責在 Android 環境中執行測試
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * ExampleInstrumentedTest
 *
 * 類型說明：
 * Instrumented Test（裝置測試）
 *
 * 執行環境：
 * - 真實 Android 裝置 或 Emulator（非 JVM）
 * - 可存取 Android Framework API（Context、Resources、PackageManager 等）
 *
 * 用途：
 * - 驗證 App 在 Android 系統環境下是否能正確取得系統資源
 * - 確認 Application Context 是否正確
 *
 * 常見應用情境：
 * - Context 是否為預期的 package
 * - 資源載入是否正常
 * - 與系統服務互動是否正確
 */
@RunWith(AndroidJUnit4::class) // 指定此測試類別由 AndroidJUnit4 Runner 執行
class ExampleInstrumentedTest {

    /**
     * useAppContext
     *
     * 測試目標：
     * 驗證「App 的 Application Context」是否正確
     *
     * 測試流程：
     * 1. 透過 InstrumentationRegistry 取得目標 App 的 Context
     * 2. 讀取 Context 中的 packageName
     * 3. 與專案實際 applicationId 做比對
     *
     * 測試意義：
     * - 確保測試執行時，Context 指向正確的 App
     * - 避免因設定錯誤導致測試執行在錯誤的 package
     */
    @Test
    fun useAppContext() {

        // 取得目前測試目標 App 的 Context（非測試 App 本身）
        val appContext = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        // 驗證取得的 packageName 是否為專案設定的 applicationId
        assertEquals(
            "com.example.futurememailapp",
            appContext.packageName
        )
    }
}
