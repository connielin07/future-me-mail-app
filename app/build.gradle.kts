// app/build.gradle.kts
// 功能說明：
// 這是「App 模組」的 Gradle 設定檔，負責：
// 1) 套用需要的 Plugins（Android/Kotlin/Firebase）
// 2) 設定 SDK 版本、打包資訊、BuildConfig、Build Types 等
// 3) 宣告第三方與 AndroidX 依賴（Retrofit、FCM、Material CalendarView 等）

plugins {
    // Android Application Plugin：讓此 module 可以被打包成 APK / AAB
    alias(libs.plugins.android.application)

    // Kotlin Android Plugin：支援 Kotlin 開發 Android App
    alias(libs.plugins.kotlin.android)

    // Google Services Plugin：解析 google-services.json，產生 Firebase 所需設定
    alias(libs.plugins.google.services)
}

android {
    // namespace：R、BuildConfig 等產物所屬的命名空間（與 package 類似但用途不同）
    namespace = "com.example.futurememailapp"

    // compileSdk：用哪一版 Android SDK 來「編譯」專案（可使用該版本的 API）
    // 你這裡寫法是 DSL 形式：version = release(36)（偏新版 DSL/寫法）
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        // applicationId：實際安裝到裝置上的 App ID（上架/辨識用）
        applicationId = "com.example.futurememailapp"

        // minSdk：最低支援版本（Android 9 / API 28）
        // 代表低於 28 的裝置無法安裝
        minSdk = 28

        // targetSdk：宣告你針對哪個版本做相容性測試與行為適配
        targetSdk = 36

        // 版本資訊：上架或版本控管用
        versionCode = 1
        versionName = "1.0"

        // Android Instrumentation Test 的執行器（androidTest）
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // buildConfigField：產生 BuildConfig.BACKEND_BASE_URL
            // 用途：把後端 API Base URL 寫進 BuildConfig，程式可直接取用
            // 優點：避免在程式碼到處硬編碼 URL，並可依 buildType 切換
            buildConfigField("String", "BACKEND_BASE_URL", "\"https://futuremail.cocotoget.com/\"")
        }

        release {
            // isMinifyEnabled：是否啟用 R8/Proguard 壓縮與混淆
            // 目前 false：release 不混淆，便於除錯；但上線時通常會評估開啟
            isMinifyEnabled = false

            // Proguard/R8 規則檔
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // release 同樣提供 BACKEND_BASE_URL
            // 若未來要分環境（dev/staging/prod）可在這裡改成不同 URL
            buildConfigField("String", "BACKEND_BASE_URL", "\"https://futuremail.cocotoget.com/\"")
        }
    }

    compileOptions {
        // Java 編譯目標版本：使用 Java 11 語法/目標碼
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        // Kotlin JVM target：對齊 Java 11
        jvmTarget = "11"
    }

    buildFeatures {
        // buildConfig = true：確保會產生 BuildConfig 類別（供 buildConfigField 使用）
        buildConfig = true
    }
}

dependencies {
    // ---------- AndroidX 基礎元件 ----------
    implementation(libs.androidx.core.ktx)          // Kotlin 擴充，常用工具/extension
    implementation(libs.androidx.appcompat)         // AppCompat 相容性支援
    implementation(libs.material)                   // Material Design 元件（你也下面又加一個 material 版本）
    implementation(libs.androidx.activity)          // Activity 元件
    implementation(libs.androidx.constraintlayout)  // ConstraintLayout 版面配置

    // ---------- Lifecycle（KTX） ----------
    // lifecycle-runtime-ktx：支援 lifecycleScope、repeatOnLifecycle 等
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // ViewModel / LiveData：用於狀態管理與 UI 資料觀察（MVVM 常用）
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // activity-ktx：提供 by viewModels() 等 Kotlin 擴充
    implementation("androidx.activity:activity-ktx:1.11.0") // for by viewModels()

    // ---------- Retrofit / OkHttp（串接後端 API） ----------
    // retrofit：HTTP API Client
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // converter-gson：將 JSON 轉成 Kotlin/Java 物件（Gson）
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // logging-interceptor：網路請求/回應 log（除錯非常好用）
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ---------- Firebase（FCM 推播） ----------
    // Firebase BOM：統一 Firebase 家族套件版本（避免版本不一致衝突）
    implementation(platform(libs.firebase.bom))

    // Firebase Cloud Messaging（KTX）：推播通知/Token 等
    implementation(libs.firebase.messaging)

    // ---------- 測試 ----------
    testImplementation(libs.junit)                  // 單元測試 JUnit4
    androidTestImplementation(libs.androidx.junit)  // Android Instrumentation Test
    androidTestImplementation(libs.androidx.espresso.core) // UI 測試 Espresso

    // ---------- Material 重複宣告（注意：這裡是額外硬編碼版本） ----------
    // 你上面已經 implementation(libs.material)（對應 version catalog 的 1.13.0）
    // 這行又額外加 1.12.0，可能造成版本不一致與依賴解析結果不直覺
    // 目前照你的要求：不修改，只註解提醒用途/影響
    implementation("com.google.android.material:material:1.12.0")

    // ---------- 日曆功能 ----------
    // MaterialCalendarView：Overview 的月曆 UI 元件
    implementation("io.github.prolificinteractive:material-calendarview:2.0.1")

    // ThreeTenABP：日期時間函式庫（Joda/Java time backport）
    // 常搭配 calendar 或日期運算使用
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.0")
}
