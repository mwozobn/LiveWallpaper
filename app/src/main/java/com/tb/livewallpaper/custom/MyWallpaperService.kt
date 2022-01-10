package com.tb.livewallpaper.custom

import android.service.wallpaper.WallpaperService
import android.text.TextUtils

import android.media.MediaPlayer

import android.view.SurfaceHolder

import android.preference.PreferenceManager

import android.app.WallpaperManager

import android.content.*
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.util.Log
import java.io.IOException
import java.lang.IllegalArgumentException

import com.tb.livewallpaper.APP
import android.content.Intent

import com.tb.livewallpaper.utils.Constant
import android.content.IntentFilter
import android.content.BroadcastReceiver








class MyWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    private inner class VideoEngine : Engine(),
        OnPreparedListener, OnCompletionListener, MediaPlayer.OnErrorListener {
        private var mPlayer: MediaPlayer? = null
        private var isPapered = false

        override fun onVisibilityChanged(visible: Boolean) {
            Log.d("engine","onVisibilityChanged")
            if (isPapered) {
                if (visible) {
                    mPlayer!!.start()
                } else {
                    mPlayer!!.pause()
                }
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            Log.d("engine","onSurfaceCreated")
            mPlayer = MediaPlayer()
            setVideo()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            Log.d("engine","onSurfaceDestroyed")
            if (mPlayer!!.isPlaying) {
                mPlayer!!.stop()
            }
            mPlayer!!.release()
            mPlayer = null
        }

        override fun onPrepared(mp: MediaPlayer) {
            Log.d("engine","onPrepared")
            isPapered = true
            mp.start()
        }

        override fun onCompletion(mp: MediaPlayer) {
            Log.d("engine","onCompletion")
            closeWallpaper(applicationContext)
        }

        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            Log.d("engine","onError")
            closeWallpaper(applicationContext)
            return true
        }

        fun setVideo() {
            if (TextUtils.isEmpty(videoPath)) {
                closeWallpaper(applicationContext)
                throw IllegalArgumentException("video path is null")
            }
            if (mPlayer != null) {
                mPlayer!!.reset()
                isPapered = false
                try {
                    mPlayer!!.setOnPreparedListener(this)
                    mPlayer!!.setOnCompletionListener(this)
                    mPlayer!!.setOnErrorListener(this)
                    mPlayer!!.setSurface(surfaceHolder.surface)
                    mPlayer!!.setDataSource(videoPath)
                    mPlayer?.isLooping = true
                    mPlayer?.setVolume(0f,0f)
                    mPlayer!!.prepareAsync()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val SERVICE_NAME = "com.tb.livewallpaper.custom.MyWallpaperService"
        private var videoPath:String=""

        fun closeWallpaper(context: Context?) {
            try {
                WallpaperManager.getInstance(context).clear()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun setVideoPath(path:String) {
            closeWallpaper(APP.instance)
            this.videoPath = path
        }
    }
}
