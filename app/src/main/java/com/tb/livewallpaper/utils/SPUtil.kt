package com.tb.livewallpaper.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.tb.livewallpaper.APP
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.reflect.KProperty

class SPUtil<T>() {

    private var keyName: String? = null
    private var defaultValue: T? = null

    constructor(keyName: String,defaultValue: T) : this() {
        this.keyName = keyName
        this.defaultValue = defaultValue
    }

    private val prefs: SharedPreferences by lazy {
        APP.instance.applicationContext.getSharedPreferences("default", Context.MODE_PRIVATE)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T  {
//        Log.e("info", "调用$this 的getValue()")
        return findSharedPreference(keyName!!, defaultValue!!)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
//        Log.e("info", "调用$this 的setValue() value参数值为：$value")
        putSharedPreferences(keyName!!, value)
    }



    @Suppress("UNCHECKED_CAST")
    private fun  findSharedPreference(name: String, default: T): T = with(prefs) {
        val res = when (default) {
            is Long -> getLong(name, default)
            is String -> getString(name, default)
            is Int -> getInt(name, default)
            is Boolean -> getBoolean(name, default)
            is Float -> getFloat(name, default)
            else -> getString(name, serialize(default))?.let { deSerialization(it) }
        }
        res as T
    }

    @SuppressLint("CommitPrefEdits")
    private fun  putSharedPreferences(name: String, value: T) = with(prefs.edit()) {
        when (value) {
            is Long -> putLong(name, value)
            is String -> putString(name, value)
            is Int -> putInt(name, value)
            is Boolean -> putBoolean(name, value)
            is Float -> putFloat(name, value)
            else -> putString(name, serialize(value))
        }.apply()
    }



    fun clear() {
        Log.e("info", "调用$this clear()")
        prefs.edit().clear().commit()
    }


    fun remove(key: String) {
        Log.e("info", "调用$this remove()参数值为：$key")
        prefs.edit().remove(key).commit()
    }


    @Throws(Exception::class)
    private fun <T> serialize(obj: T): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(
            byteArrayOutputStream)
        objectOutputStream.writeObject(obj)
        var serStr = byteArrayOutputStream.toString("ISO-8859-1")
        serStr = java.net.URLEncoder.encode(serStr, "UTF-8")
        objectOutputStream.close()
        byteArrayOutputStream.close()
        return serStr
    }

    @Throws(Exception::class)
    private fun <T> deSerialization(str: String): T {
        val redStr = java.net.URLDecoder.decode(str, "UTF-8")
        val byteArrayInputStream = ByteArrayInputStream(
            redStr.toByteArray(charset("ISO-8859-1")))
        val objectInputStream = ObjectInputStream(
            byteArrayInputStream)
        val obj = objectInputStream.readObject() as T
        objectInputStream.close()
        byteArrayInputStream.close()
        return obj
    }


}
