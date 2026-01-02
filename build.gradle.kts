// Top-level build file
// 功能說明：
// 此檔案為整個 Android 專案的「最上層 Gradle 設定檔」，
// 用於集中管理所有子模組（app、library 等）
// 可能會共用的 Plugin 設定。
// 實際套用（apply）通常會在各 module 的 build.gradle.kts 中進行。

plugins {

    // Android Application Plugin
    // - 定義 Android 應用程式所需的建置能力
    // - 使用 Version Catalog（libs.versions.toml）中的 plugin 定義
    // - apply false：僅宣告版本，不在此層實際套用
    alias(libs.plugins.android.application) apply false

    // Kotlin Android Plugin
    // - 讓 Android 專案支援 Kotlin 語言
    // - 提供 Kotlin 編譯與 Android 整合功能
    // - apply false：由各 module 自行決定是否使用
    alias(libs.plugins.kotlin.android) apply false

    // Google Services Plugin
    // - Firebase / Google Services 必要 Plugin
    // - 用於解析 google-services.json 並產生對應設定
    // - 通常只會在 app module 中實際套用
    alias(libs.plugins.google.services) apply false
}
