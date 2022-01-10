package com.tb.livewallpaper

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected open lateinit var mBinding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = getViewBinding()
        setContentView(mBinding.root)
        if (requestedOrientation!= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        initView()

        initData()
    }


    open fun initData() {

    }

    open fun initView() {

    }

    open fun jumpAc(cls: KClass<out Activity>, isFinish: Boolean = false) {
        startActivity(Intent(this, cls.java))
        if (isFinish)
            finish()
        overridePendingTransition(0,0)
    }

    abstract fun getViewBinding(): VB

    private var isPause = false

    open fun isPause() = isPause

    override fun onResume() {
        super.onResume()
        isPause = false
    }

    override fun onPause() {
        super.onPause()
        isPause= true
    }


}