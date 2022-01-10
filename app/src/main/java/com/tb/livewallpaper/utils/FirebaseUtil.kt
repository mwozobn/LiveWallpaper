package com.tb.livewallpaper.utils

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.tb.livewallpaper.APP
import java.util.*

object FirebaseUtil {

    private val fa = FirebaseAnalytics.getInstance(APP.instance)

    init {
        var countryCode by SPUtil("county_code","")
        var firstInApp by SPUtil("first_in",true)
        if (countryCode.isEmpty()){
            countryCode = Locale.getDefault().country
            fa.setUserProperty("person_live",countryCode)
            logEvent("person_live","country",countryCode)
        }
        if (countryCode.isNotEmpty() &&firstInApp){
            logEvent("live_0")
            firstInApp = false
        }
    }

    fun logEvent(event:String,bundle: Bundle?=null){
        Log.i("logEvent", "$event  $bundle")
        fa.logEvent(event,bundle)
    }

    fun logEvent(event: String,key:String,value:String){
        val bundle = Bundle()
        bundle.putString(key, value)
        Log.i("logEvent", "$event  $bundle")
        fa.logEvent(event, bundle)
    }

}