package com.tb.livewallpaper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.permissionx.guolindev.PermissionX
import com.tb.livewallpaper.data.Wallpaper
import com.tb.livewallpaper.databinding.AcMainBinding
import com.tb.livewallpaper.utils.FirebaseUtil
import com.tb.livewallpaper.utils.GridSpacingItemDecoration
import com.tb.livewallpaper.utils.ImgDataHolder
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Appendable
import java.lang.Exception

class MainActivity : BaseActivity<AcMainBinding>() {

    private var imgList = ArrayList<Wallpaper>()

    override fun getViewBinding(): AcMainBinding {
        return DataBindingUtil.setContentView(this, R.layout.ac_main)
    }

    override fun initView() {

        mBinding.rvWall.layoutManager = GridLayoutManager(this, 2)
        mBinding.rvWall.addItemDecoration(GridSpacingItemDecoration(this))
        mBinding.rvWall.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        if (APP.instance.hotStartMain) {
            APP.instance.hotStartMain = false
        } else {
            FirebaseUtil.logEvent("live_home")
        }
    }

    override fun initData() {

        PermissionX.init(this).permissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList, "Core fundamental are based on these permissions", "OK", "Cancel"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "You need to allow necessary permissions in Settings manually", "OK", "Cancel")
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    var list = ImgDataHolder.getWallData()
                    if (list.size <= 0)
                        ImgDataHolder.loadWallData(this)
                    list = ImgDataHolder.getWallData()
                    adapter.setData(list)
                }
            }
    }

    private val adapter = object : RecyclerView.Adapter<MainViewHolder>() {

        private var dataList = ArrayList<Wallpaper>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
            val itemView =
                LayoutInflater.from(this@MainActivity).inflate(R.layout.item_rv_main, parent, false)
            return MainViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MainViewHolder, position: Int) {

            holder.imgView.setOnClickListener {
                FirebaseUtil.logEvent("live_click")
                val intent = Intent(this@MainActivity, PreviewAc::class.java)
                ImgDataHolder.setData("wallpaperData", dataList[position])
                this@MainActivity.startActivity(intent)
            }

            val roundedCorner = RoundedCorners(50)
            val option = RequestOptions.bitmapTransform(roundedCorner)
            val imgSrc =
                if (dataList[position].type == "video") dataList[position].path else dataList[position].uri
            Glide.with(this@MainActivity).load(imgSrc)
                .apply(option)
                .into(holder.imgView)

            if (dataList[position].type == "video") {
                holder.icLive.visibility = View.VISIBLE
            } else {
                holder.icLive.visibility = View.INVISIBLE
            }

        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setData(list: ArrayList<Wallpaper>) {
            this.dataList = list
            notifyDataSetChanged()
        }

    }

    inner class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgView: ImageView = itemView.findViewById(R.id.iv_item)
        val icLive: ImageView = itemView.findViewById(R.id.iv_live)
    }


}