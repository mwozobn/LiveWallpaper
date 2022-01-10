package com.tb.livewallpaper

import android.Manifest
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.tb.livewallpaper.databinding.AcPreviewBinding
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

import android.graphics.BitmapFactory

import android.app.Activity
import android.content.ComponentCallbacks

import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.permissionx.guolindev.PermissionX
import com.tb.livewallpaper.custom.MyWallpaperService
import com.tb.livewallpaper.data.Wallpaper
import com.tb.livewallpaper.utils.*
import com.tb.livewallpaper.view.MyDialog
import com.tb.livewallpaper.view.MyDialog.Companion.TYPE_APPLYING
import com.tb.livewallpaper.view.MyDialog.Companion.TYPE_DOWNLOADING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStream
import android.app.WallpaperInfo
import android.preference.PreferenceManager

import android.content.SharedPreferences
import com.tb.livewallpaper.custom.MyWallpaperService.Companion.SERVICE_NAME


class PreviewAc : BaseActivity<AcPreviewBinding>(), View.OnClickListener {

    private var wallpaper: Wallpaper? = null
    private var uri: Uri? = null
    private var myDialog: MyDialog? = null

    override fun getViewBinding(): AcPreviewBinding {
        return DataBindingUtil.setContentView(this, R.layout.ac_preview)
    }

    override fun initView() {
        mBinding.btnBack.setOnClickListener(this)
        mBinding.btnApply.setOnClickListener(this)
        mBinding.btnDownload.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launchWhenStarted {
            delay(1000)
            if (!isPause())
                FirebaseUtil.logEvent("live_detail")
        }
    }

    override fun onResume() {
        super.onResume()
        wallpaper = ImgDataHolder.getImgData("wallpaperData")
        if (wallpaper?.type == "video") {
            mBinding.ivPaper.visibility = View.GONE
            mBinding.videoView.visibility = View.VISIBLE
            mBinding.videoView.setVideoPath(wallpaper?.path)
            initVideoView()
        } else {
            mBinding.ivPaper.visibility = View.VISIBLE
            mBinding.videoView.visibility = View.GONE
            Glide.with(this).load(wallpaper?.uri).into(mBinding.ivPaper)
        }
    }

    private fun initVideoView() {
        mBinding.videoView.start()
        mBinding.videoView.setOnCompletionListener {
            it.start()
            it.isLooping = true
        }
    }

    private fun saveVideoPath(path: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(Constant.VIDEO_PATH, path)
        editor.apply()
    }

    override fun onClick(v: View?) {
        when (v) {
            mBinding.btnBack -> this.onBackPressed()
            mBinding.btnApply -> {
                FirebaseUtil.logEvent("live_detail_al")
                APP.instance.setJumpLaunchLoading(true)
                myDialog = MyDialog(TYPE_APPLYING)
                myDialog?.show(supportFragmentManager, "applying")
                if (wallpaper?.type == "video" && !isPause()) {
                    wallpaper?.path?.let { MyWallpaperService.setVideoPath(it) }
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this, MyWallpaperService::class.java))
                    launcher.launch(intent)
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(1000)
                        if (!isPause()) {
                            applyWallpaper()
                        }
                    }
                }
            }
            mBinding.btnDownload -> {
                FirebaseUtil.logEvent("live_detail_dl")
                myDialog = MyDialog(TYPE_DOWNLOADING)
                myDialog?.show(supportFragmentManager, "downloading")
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(1000)
                    downloadWallpaper {
                        if (it && !isPause()) {
                            val intent = Intent(this@PreviewAc, ResultAc::class.java)
                            intent.putExtra("type", TYPE_DOWNLOADING)
                            startActivity(intent)
                            Log.d("startActivity", "ResultAc")
                        }
                    }
                }
            }
        }
    }

    private fun applyWallpaper() {

        try {
            ImgDataHolder.downloadWallData(this, wallpaper?.name!!) {
                uri =  FileUtils.pathToUri(this, it!!)
                when {
                    RomUtils.isHuawei() -> {
                        setHuawei()
                    }
                    RomUtils.isXiaomi() -> {
                        setXiaomi()
                    }
                    RomUtils.isOppo() -> {
                        setOppo()
                    }
                    RomUtils.isVivo() -> {
                        setVivo()
                    }
                    else -> {
                        setOthers()
                    }
                }
                myDialog?.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("setWallpaper_e", e.message.toString())
        }

    }

    private var filePath = ""
    var os: FileOutputStream? = null
    var input: InputStream? = null

    private suspend fun downloadWallpaper(callbacks: ((downloaded: Boolean) -> Unit)? = null) {

        withContext(Dispatchers.IO) {
            PermissionX.init(this@PreviewAc).permissions(
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
                        val fileName = if (wallpaper?.type == "video") {
                            System.currentTimeMillis().toString() + ".mp4"
                        } else {
                            val endType = wallpaper?.fileFormat ?: ".png"
                            System.currentTimeMillis().toString() + endType
                        }
                        val dir =
                            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + File.separator + "/live_wallpaper/")
                        if (!dir.exists()) {
                            dir.mkdir()
                        }

                        try {
                            val file = File(dir, fileName)
                            if (!file.exists()) {
                                file.createNewFile()
                            }
                            filePath = file.path
                            os = FileOutputStream(file)
                            input = resources.assets.open("img_wall/${wallpaper?.name}")
                            val buffer = ByteArray(1444)
                            var byteSum = 0
                            var byteRead: Int = 0
                            while (((input?.read(buffer)).also {
                                    if (it != null) {
                                        byteRead = it
                                    }
                                }) != -1) {
                                byteSum += byteRead //字节数 文件大小
                                os?.write(buffer, 0, byteRead)
                            }

                            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            val uri = Uri.fromFile(file)
                            intent.data = uri
                            if (!isPause()) {
                                this@PreviewAc.sendBroadcast(intent)
                            }
                            callbacks?.invoke(true)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            e.message?.let { Log.d("applyWallpaper", it) }
                            callbacks?.invoke(false)
                        } finally {
                            os?.flush()
                            os?.close()
                            myDialog?.dismiss()
                        }
                    }
                }
        }
    }

    override fun onStop() {
        myDialog?.dismiss()
        super.onStop()
        os?.flush()
        os?.close()
        input?.close()

    }

    private fun setHuawei() {
        try {
            val componentName =
                ComponentName("com.android.gallery3d", "com.android.gallery3d.app.Wallpaper")
            intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("mimeType", "image/*")
            intent.component = componentName
            launcher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            setDefaultWallpaper()
        }

    }

    private fun setXiaomi() {
        try {
            val componentName = ComponentName(
                "com.android.thememanager",
                "com.android.thememanager.activity.WallpaperDetailActivity"
            )
            val intent = Intent("miui.intent.action.START_WALLPAPER_DETAIL")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("mimeType", "image/*")
            intent.component = componentName
            launcher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            setDefaultWallpaper()
        }

    }

    private fun setVivo() {
        try {
            val componentName =
                ComponentName("com.vivo.gallery", "com.android.gallery3d.app.Wallpaper")
            intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, "image/*")
            intent.putExtra("mimeType", "image/*")
            intent.component = componentName
            launcher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            setDefaultWallpaper()
        }

    }

    private fun setOppo() {
        try {
            val componentName =
                ComponentName("com.coloros.gallery3d", "com.oppo.gallery3d.app.Wallpaper")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, "image/")
            intent.putExtra("mimeType", "image/*")
            intent.component = componentName
            launcher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            setDefaultWallpaper()
        }

    }

    private fun setOthers() {
        try {
            val intent = WallpaperManager.getInstance(this).getCropAndSetWallpaperIntent(uri)
            launcher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            setDefaultWallpaper()
        }
    }

    private fun setDefaultWallpaper() {
        try {
            val bitmap = BitmapFactory.decodeFile(filePath);
            WallpaperManager.getInstance(this).setBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        FirebaseUtil.logEvent("live_return")
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            myDialog?.dismiss()
            val intent = Intent(this, ResultAc::class.java)
            intent.putExtra("type", TYPE_APPLYING)
            startActivity(intent)
        }
}