package com.example.futurememailapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class OnboardingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 從 arguments  取得資料
        val title = arguments?.getString(ARG_TITLE)
        val description = arguments?.getString(ARG_DESCRIPTION)
        val imageResId: Int = arguments?.getInt(ARG_IMAGE_RES, 0) ?: 0

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val imageView = view.findViewById<ImageView>(R.id.imgTutorialIcon)

        tvTitle.text = title
        tvDescription.text = description

        // 設定圖片與版面調整
        if (imageResId != 0) {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(imageResId)
            imageView.setBackgroundResource(0) // 移除灰色背景
            
            // 有圖片時，文字靠左對齊
            tvTitle.gravity = android.view.Gravity.START
            tvDescription.gravity = android.view.Gravity.START
        } else {
            // 沒有圖片時 (例如第一頁)，隱藏圖片並將文字置中
            imageView.visibility = View.GONE
            
            tvTitle.gravity = android.view.Gravity.CENTER
            tvDescription.gravity = android.view.Gravity.CENTER
            
            // 讓 LinearLayout 內的所有元件垂直置中
            (view as? ViewGroup)?.let { layout ->
                if (layout is android.widget.LinearLayout) {
                    layout.gravity = android.view.Gravity.CENTER
                }
            }
        }
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        private const val ARG_IMAGE_RES = "image_res"

        // 建立 Fragment 實例的工廠方法
        fun newInstance(title: String, description: String, imageResId: Int): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DESCRIPTION, description)
            args.putInt(ARG_IMAGE_RES, imageResId)
            fragment.arguments = args
            return fragment
        }
    }
}
