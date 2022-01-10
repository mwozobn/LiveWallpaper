package com.tb.livewallpaper.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.tb.livewallpaper.APP
import java.io.File

object FileUtils {

    fun pathToUri(ctx :Context,path:String):Uri?{
        if (path.isBlank()) return null
        return if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            FileProvider.getUriForFile(ctx.applicationContext,APP.fileProvider, File(path))
        }else{
            Uri.fromFile(File(path))
        }
    }
}