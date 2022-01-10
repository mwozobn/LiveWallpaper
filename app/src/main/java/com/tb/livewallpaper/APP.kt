package com.tb.livewallpaper

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle

class APP : Application() {

    companion object {

        private var app: APP? = null

        val instance: APP
            get() = app!!

        const val fileProvider = "com.tb.livewallpaper.fileprovider"
    }

    init {
        app = this
    }

    private var acCount = 0

    var hotStartMain = false

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }


    private val lifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
            if (acCount == 0) {
                if (activity !is LaunchAc && !jumpLaunchLoading) {
                    val intent = Intent(activity, LaunchAc::class.java)
                    intent.putExtra("isHotLaunch", true)
                    activity.startActivity(intent)
                }

                if (jumpLaunchLoading){
                    jumpLaunchLoading = false
                }

                if (activity is MainActivity) {
                    hotStartMain = true
                }
            }
            acCount++
        }

        override fun onActivityStopped(activity: Activity) {
            acCount--
            if (acCount == 0) {
                if (activity is LaunchAc) {
                    activity.finish()
                }
            }
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }

    }

    private var jumpLaunchLoading = false

    fun setJumpLaunchLoading(jump:Boolean){
        this.jumpLaunchLoading =jump
    }

    fun getJumpLaunchLoading() = jumpLaunchLoading

}