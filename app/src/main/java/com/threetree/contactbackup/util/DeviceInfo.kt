
package com.threetree.contactbackup.util

import android.text.TextUtils

/**
 * 设备信息类
 * 
 */

class DeviceInfo {
    /**
     * 本地ip地址
     */
    var ip: String? = null
    /**
     * Android设备唯一ID
     */
    var androidID: String? = null
    var imei: String? = null
        get() = if (TextUtils.isEmpty(field)) {
            if (!TextUtils.isEmpty(androidID)) {
                androidID
            } else "no_imei"
        } else field
    var imsi: String? = null
        get() = if (TextUtils.isEmpty(field)) {
            "no_sim_card"
        } else field
    var brand: String? = null
    var ua: String? = null
    var mcc: String? = null
    var mnc: String? = null
    var mver: String? = null
    /**
     * 当前版本名
     */
    var cver: String? = null
    var osVersion: String? = null
    /**
     * 屏幕分辨率
     */
    var widthPixels: Int = 0
    var heightPixels: Int = 0

    init {
        this.mcc = ""
        this.mnc = ""
        this.imsi = ""
        this.ua = ""
        this.mver = ""
        this.osVersion = ""
        this.widthPixels = 0
        this.heightPixels = 0
    }
}
