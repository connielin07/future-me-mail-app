// settings.gradle.kts
// 功能說明：
// 此檔案為整個 Gradle 專案的「初始化設定檔」，
// 負責定義：
// 1. Plugin 的解析來源（pluginManagement）
// 2. 專案依賴套件的倉庫來源（dependencyResolutionManagement）
// 3. 專案名稱與包含的模組（modules）
//
// 這個檔案會在 Gradle 啟動時最先被讀取。

pluginManagement {
    repositories {

        // Google 官方 Maven 倉庫
        // - 提供 Android Gradle Plugin、AndroidX、Google 官方套件
        // - content 區塊限制只解析特定 group，提升解析效率與安全性
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // Maven Central
        // - 大多數第三方開源函式庫的主要來源
        mavenCentral()

        // Gradle Plugin Portal
        // - 提供 Gradle 官方與社群 Plugin
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {

    // repositoriesMode 設定為 FAIL_ON_PROJECT_REPOS
    // - 強制所有 module 只能使用這裡定義的 repositories
    // - 避免各 module 各自宣告倉庫，造成版本不一致或安全風險
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {

        // Google 官方倉庫
        // - AndroidX、Material、Firebase 等官方套件來源
        google()

        // Maven Central
        // - 主流第三方函式庫來源
        mavenCentral()

        // JitPack 倉庫
        // - 用於引入 GitHub 上尚未發佈至 Maven Central 的函式庫
        // - 本專案用於 MaterialCalendarView 等第三方套件
        maven { url = uri("https://jitpack.io") } // 新增 JitPack 倉庫
    }
}

// rootProject.name：專案名稱
// - 顯示於 Android Studio / Gradle 專案結構中
rootProject.name = "FutureMeMailApp"

// include：指定此專案包含的模組
// - 目前僅包含 app 模組
include(":app")
