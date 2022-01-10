package com.tb.livewallpaper.data

import android.net.Uri

data class Wallpaper(val uri: Uri,
                     val name: String,
                     val type: String,
                     val path: String?,
                     val fileFormat:String
                     )
