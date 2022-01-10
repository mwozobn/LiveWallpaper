package com.tb.livewallpaper.utils

import android.content.Context
import android.net.Uri
import com.tb.livewallpaper.data.Wallpaper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

object ImgDataHolder {

    private var imgMap  = mutableMapOf<String,Wallpaper>()

    private var imgList = ArrayList<Wallpaper>()

    fun setData(key:String,wallpaper: Wallpaper){
        imgMap[key] = wallpaper
    }

    fun getImgData(key:String):Wallpaper?{
        return imgMap[key]
    }

    fun getWallData(): ArrayList<Wallpaper> {
        return imgList
    }

     fun loadWallData(ctx:Context){
        imgList.clear()
        val assetsManager = ctx.resources.assets
        var input: InputStream? = null
        try {
            val list = assetsManager.list("img_wall")
            if (list != null) {
                list.shuffle()
                for ((index, item) in list.withIndex()) {
                    input = assetsManager.open("img_wall/$item")
                    val uri = Uri.parse("file:///android_asset/img_wall/$item")
                    val fileFormat = item.substring(item.indexOfLast { it=='.'},item.length)
                    val type = if (item.endsWith(".mp4")) "video" else "img"
                    var path: String? = null
                    if (type == "video") {
                        downloadWallData(ctx,item) {
                            if (!it.isNullOrBlank()) {
                                path = it
                            }
                        }
                    }
                    val wallpaper = Wallpaper(uri, item, type, path,fileFormat)
                    imgList.add(wallpaper)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            input?.close()
        }
    }

     fun downloadWallData(ctx: Context,name: String, callBack: ((path: String?) -> Unit)?=null) {

        val dir =
            File(ctx.filesDir.path + File.separator)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val os: FileOutputStream?
        val input: InputStream?
        try {

            val file = File(dir, name)
            if (!file.exists()) {
                file.createNewFile()
            } else {
                callBack?.invoke(file.path)
                return
            }
            input = ctx.resources.assets.open("img_wall/$name")
            os = FileOutputStream(file)
            val buffer = ByteArray(1444)
            var byteSum = 0
            var byteRead: Int
            while (((input.read(buffer)).also { byteRead = it }) != -1) {
                byteSum += byteRead
                os.write(buffer, 0, byteRead)
            }
            callBack?.invoke(file.path)
        } catch (e: Exception) {
            e.printStackTrace()
            callBack?.invoke(null)
        }

    }

}