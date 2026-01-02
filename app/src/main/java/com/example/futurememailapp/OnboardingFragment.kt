package com.example.futurememailapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * OnboardingFragment
 *
 * 功能說明：
 * 此 Fragment 為 Onboarding 導覽的「單一頁面」呈現元件，
 * 由 ViewPager2 + FragmentStateAdapter 動態建立多頁。
 *
 * 此頁面會：
 * 1) 從 arguments 取得標題、描述、圖片資源 ID
 * 2) 顯示文字（tvTitle、tvDescription）
 * 3) 依是否有圖片（imageResId != 0）調整版面：
 *    - 有圖片：顯示 ImageView、移除灰色背景、文字靠左
 *    - 無圖片（例如第一頁）：隱藏 ImageView、文字置中、整體垂直置中
 *
 * UI 對應：
 * - fragment_onboarding.xml
 *   - tvTitle：標題
 *   - tvDescription：描述
 *   - imgTutorialIcon：導覽圖片（可選）
 */
class OnboardingFragment : Fragment() {

    /**
     * onCreateView
     *
     * 功能：
     * 建立此 Fragment 的畫面，並載入對應的 XML layout。
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 載入 fragment_onboarding.xml，回傳 View 給系統呈現
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    /**
     * onViewCreated
     *
     * 功能：
     * View 建立完成後呼叫，適合在此做：
     * - findViewById 綁定 UI 元件
     * - 從 arguments 取得資料並更新畫面
     * - 根據資料動態調整 UI（顯示/隱藏、對齊方式等）
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =========================
        // 1) 從 arguments 取得傳入資料
        // =========================
        // arguments 通常由 newInstance() 建立並綁定（見 companion object）
        val title = arguments?.getString(ARG_TITLE)
        val description = arguments?.getString(ARG_DESCRIPTION)

        // 圖片資源 ID（Drawable）
        // 若未傳入或為 0，代表該頁不顯示圖片（例如第一頁歡迎頁）
        val imageResId: Int = arguments?.getInt(ARG_IMAGE_RES, 0) ?: 0

        // =========================
        // 2) 找到畫面上的 UI 元件
        // =========================
        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val imageView = view.findViewById<ImageView>(R.id.imgTutorialIcon)

        // =========================
        // 3) 將文字資料顯示到畫面上
        // =========================
        tvTitle.text = title
        tvDescription.text = description

        // =========================
        // 4) 根據是否有圖片，動態調整版面
        // =========================
        if (imageResId != 0) {
            // ---------- 有圖片的情況 ----------
            // 例如：步驟教學頁（tutorial_step1/2/3）

            // 顯示 ImageView 並設定圖片資源
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(imageResId)

            // 移除 ImageView 原本在 XML 內給的灰底（避免圖片上還有底色）
            imageView.setBackgroundResource(0)

            // 有圖片時：文字改為靠左對齊（更像一般教學文案版面）
            tvTitle.gravity = android.view.Gravity.START
            tvDescription.gravity = android.view.Gravity.START

        } else {
            // ---------- 沒圖片的情況 ----------
            // 例如：第一頁歡迎頁（imageResId = 0）
            // 目標：畫面更像「歡迎頁」，文字置中、乾淨

            // 隱藏圖片區塊，避免佔用空間
            imageView.visibility = View.GONE

            // 文字置中，提升視覺聚焦
            tvTitle.gravity = android.view.Gravity.CENTER
            tvDescription.gravity = android.view.Gravity.CENTER

            // 讓 LinearLayout 內元件整體垂直置中
            // view 可能是 ViewGroup，且你的 layout 實際上是 LinearLayout
            (view as? ViewGroup)?.let { layout ->
                if (layout is android.widget.LinearLayout) {
                    layout.gravity = android.view.Gravity.CENTER
                }
            }
        }
    }

    companion object {
        // =========================
        // arguments 的 key 定義
        // =========================
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_IMAGE_RES = "image_res"

        /**
         * newInstance（工廠方法）
         *
         * 功能說明：
         * 建立 Fragment 實例並透過 Bundle 傳入參數，
         * 避免直接使用建構子傳參造成 Fragment 重建時資料遺失。
         *
         * @param title 導覽頁標題
         * @param description 導覽頁描述
         * @param imageResId 圖片資源 ID（0 表示不顯示圖片）
         */
        fun newInstance(title: String, description: String, imageResId: Int): OnboardingFragment {
            val fragment = OnboardingFragment()

            // 使用 Bundle 存放參數，並指定給 fragment.arguments
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DESCRIPTION, description)
            args.putInt(ARG_IMAGE_RES, imageResId)

            fragment.arguments = args
            return fragment
        }
    }
}
