package com.example.futurememailapp

import org.junit.Test
import org.junit.Assert.*

/**
 * ExampleUnitTest
 *
 * 類型說明：
 * Local Unit Test（本地單元測試）
 *
 * 執行環境：
 * - 開發電腦（JVM）
 * - 不需要 Android 裝置或模擬器
 * - 無法使用 Android Framework API（如 Context、Intent）
 *
 * 用途：
 * - 測試純 Kotlin / Java 的邏輯
 * - 驗證演算法、計算、工具方法是否正確
 * - 執行速度快，適合大量測試
 *
 * 與 Instrumented Test 差異：
 * - Local Unit Test：跑在 JVM（快、不能用 Android API）
 * - Instrumented Test：跑在裝置（慢、可用 Android API）
 */
class ExampleUnitTest {

    /**
     * addition_isCorrect
     *
     * 測試目標：
     * 驗證最基本的數值運算邏輯是否正確
     *
     * 測試流程：
     * 1. 執行 2 + 2
     * 2. 驗證結果是否等於 4
     *
     * 測試意義：
     * - 確認 JUnit 測試環境設定正常
     * - 驗證 assertEquals 能正確運作
     * - 作為專案單元測試的基本範例
     */
    @Test
    fun addition_isCorrect() {

        // 使用 JUnit 的 assertEquals 比對預期結果與實際結果
        assertEquals(4, 2 + 2)
    }
}
