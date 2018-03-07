
package com.threetree.contactbackup.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.NetworkInfo.State

object NetWorkUtil {
    private val TAG = NetWorkUtil::class.java.simpleName

    val CHINA_MOBILE = 1 // 中国移动
    val CHINA_UNICOM = 2 // 中国联通
    val CHINA_TELECOM = 3 // 中国电信

    val SIM_OK = 0
    val SIM_NO = -1
    val SIM_UNKNOW = -2

    var proxy = false

    val CONN_TYPE_WIFI = "wifi"
    val CONN_TYPE_GPRS = "gprs"
    val CONN_TYPE_NONE = "none"

    /**
     * 判断网络连接有效
     *
     * @param context
     * Context对象
     * @return 网络处于连接状态（3g or wifi)
     */
    fun isAvailable(context: Context): Boolean {
        var result = false
        if (getNetworkType(context) >= 0)
            result = true
        return result
    }

    /**
     * 获取网络类型
     *
     * @param context
     * Context对象
     * @return 当前处于连接状态的网络类型
     */
    fun getNetworkType(context: Context): Int {
        val connectivity = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.activeNetworkInfo
            if (info != null && info.isConnectedOrConnecting) {
                return info.type
            }
        }
        return -9
    }

    /**
     * 获取网络类型
     *
     * @param context
     * Context对象
     * @return 当前处于连接状态的网络类型
     */
    fun getNetworkInfo(context: Context): String? {
        var result: String? = null
        val connectivity = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity == null) {
            result = null
        } else {

            val info = connectivity.activeNetworkInfo
            if (info != null) {
                val tem = info.state
                if (tem == State.CONNECTED || tem == State.CONNECTING) {
                    result = (info.typeName + " " + info.subtypeName
                            + info.extraInfo)
                }
            }
        }
        return result
    }

    /**
     * 判断当前的网络状态 wifi或者gprs
     *
     * @param context
     * @return
     */
    fun getNetConnType(context: Context): String {
        // 获得网络连接服务
        val connManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (null == connManager) {
            com.afmobi.tudcsdk.utils.LogUtils.w(TAG, "Network" + "can not get Context.CONNECTIVITY_SERVICE")
            return CONN_TYPE_NONE
        }

        var info: NetworkInfo? = null
        // wifi的网络状态
        info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (null != info) {
            val wifiState = info.state
            if (State.CONNECTED == wifiState) { // 判断是否正在使用WIFI网络
                return CONN_TYPE_WIFI
            }
        } else {
            com.afmobi.tudcsdk.utils.LogUtils.w(TAG, "Network" + "can not get ConnectivityManager.TYPE_WIFI")
        }

        // gprs的网络状态
        info = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (null != info) {
            val mobileState = info.state
            if (State.CONNECTED == mobileState) { // 判断是否正在使用GPRS网络
                return CONN_TYPE_GPRS
            }
        } else {
            com.afmobi.tudcsdk.utils.LogUtils.w(TAG, "Network" + "can not get ConnectivityManager.TYPE_MOBILE")
        }
        return CONN_TYPE_NONE
    }

}  