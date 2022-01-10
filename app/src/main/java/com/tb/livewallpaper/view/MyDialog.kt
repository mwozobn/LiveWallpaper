package com.tb.livewallpaper.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.tb.livewallpaper.R


class MyDialog(private val type: Int) : DialogFragment() {

    private lateinit var tvIng: TextView
    private lateinit var ivIng: ImageView
    private val dotText = arrayOf(".", "..", "...")

    companion object {
        const val TYPE_APPLYING = 0
        const val TYPE_DOWNLOADING = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE,R.style.MyLoadingDialog)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvIng = view.findViewById(R.id.tv_ing)
        ivIng = view.findViewById(R.id.iv_ing)

        if (type == TYPE_APPLYING) {
            tvIng.text = "Applying..."
            ivIng.setImageResource(R.drawable.ic_applying)
            rotateImg()
        }else{
            tvDownloading()
            ivIng.setImageResource(R.drawable.ic_downloading)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_dialog_applying, container, false)
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val params = window?.attributes
        params?.dimAmount = 0.0f
        params?.width = dip2px(180F)
        dialog?.window?.decorView?.background = ColorDrawable(Color.TRANSPARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        params?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        window?.attributes = params
        dialog?.setCanceledOnTouchOutside(false)

    }

    private fun dip2px(dpValue: Float): Int {
        val scale = this.resources.displayMetrics.density;
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun rotateImg() {
        val animator = ObjectAnimator.ofFloat(ivIng, "rotation", 0.0F, 360.0F)
        animator.apply {
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
        }
        animator.start()
    }

    @SuppressLint("SetTextI18n")
    private fun tvDownloading() {
        val animator = ValueAnimator.ofInt(0, 3)
        animator.apply {
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                val i = animator.animatedValue as Int
                tvIng.text = "Downloading" + dotText[i % dotText.size]
            }

        }
        animator.start()
    }


}