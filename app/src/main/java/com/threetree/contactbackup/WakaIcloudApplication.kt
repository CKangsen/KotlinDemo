package com.threetree.contactbackup

import android.app.Application
import android.content.Context
import android.os.Environment


import com.threetree.contactbackup.constant.BuildApkConfig

import com.threetree.contactbackup.util.DeviceInfo
import com.threetree.contactbackup.util.OsInfoUtil


import java.io.File
import java.util.Locale
import java.util.TimeZone

/**
 * Created by pradmin on 2017/7/10.
 */

class WakaIcloudApplication : Application(), HttpSysListener {
    private var mApplicationPrefsManager: ApplicationPrefsManager? = null

    val wkcInstance: WKCInstance?
        get() = mWKCInstance

    override fun onCreate() {
        super.onCreate()
        mHttpSysListener = this

        FactoryImpl.register(this.applicationContext)


    }

    p

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun HttpSysMsgProc(msg: Int, wparam: Any, lParam: Int): Boolean {
        return false
    }

    companion object {
        val TAG = WakaIcloudApplication::class.java.simpleName

        private var mWKCInstance: WKCInstance? = null
        private var mHttpSysListener: HttpSysListener? = null


    }
}
