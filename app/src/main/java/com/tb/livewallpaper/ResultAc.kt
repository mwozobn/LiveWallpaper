package com.tb.livewallpaper

import android.content.Intent
import androidx.databinding.DataBindingUtil
import com.tb.livewallpaper.databinding.AcResultBinding
import com.tb.livewallpaper.utils.FirebaseUtil
import com.tb.livewallpaper.view.MyDialog.Companion.TYPE_APPLYING

class ResultAc : BaseActivity<AcResultBinding>() {

    override fun getViewBinding(): AcResultBinding {
        return DataBindingUtil.setContentView(this, R.layout.ac_result)
    }

    override fun initView() {
        mBinding.btnBack.setOnClickListener {
            jumpAc(MainActivity::class,true)
        }
    }

    override fun initData() {
        val type = intent.getIntExtra("type", 0)
        var text= ""
        if (type== TYPE_APPLYING){
            text = "Successful Applied"
            FirebaseUtil.logEvent("live_result_al")
        }else{
            text =  "Successful Downloaded"
            FirebaseUtil.logEvent("live_result_dl")
        }
        mBinding.tvResult.text = text
    }

    override fun onBackPressed() {
        jumpAc(MainActivity::class,true)
    }

}