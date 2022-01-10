package com.tb.livewallpaper

import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.tb.livewallpaper.databinding.AcLaunchBinding
import com.tb.livewallpaper.utils.FirebaseUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class LaunchAc : BaseActivity<AcLaunchBinding>() {

    private var anim: ValueAnimator? = null
    private var isHotLaunch = false

    override fun getViewBinding(): AcLaunchBinding {
        return DataBindingUtil.setContentView(this, R.layout.ac_launch)
    }

    override fun initData() {
        isHotLaunch = intent.getBooleanExtra("isHotLaunch", false)
        val event = if (isHotLaunch) "live_2" else "live_1"
        FirebaseUtil.logEvent(event)
    }

    override fun onResume() {
        super.onResume()
        startLaunch()
        Log.d("startActivity","launchAc")
    }

    private fun startLaunch() {

        lifecycleScope.launch {
            startAnim(1000, 2500L)
            if (isHotLaunch) finish()
            else
                jumpAc(MainActivity::class, true)
        }

    }

    private suspend fun startAnim(process: Int, d: Long): Any? =
        suspendCancellableCoroutine { continuation ->
            anim?.cancel()
            anim = ValueAnimator.ofInt(mBinding.pb.progress, process)
            anim?.apply {
                interpolator = if (process == 1000)
                    AccelerateInterpolator()
                else
                    DecelerateInterpolator()
                duration = d
                addUpdateListener {
                    mBinding.pb.progress = animatedValue as Int
                }
                addListener(onEnd = {
                    continuation.resume(null){}
                })
            }
            anim?.start()

        }

    override fun onStop() {
        super.onStop()
        mBinding.pb.progress = 0
    }

    override fun onBackPressed() {
    }


}