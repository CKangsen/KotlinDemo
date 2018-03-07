
package com.threetree.contactbackup.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.WindowManager

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration
import java.util.regex.Matcher
import java.util.regex.Pattern

object OsInfoUtil {

    /**
     * 获取本地IP
     *
     * @return
     */
    private val localIpAddress: String
        get() {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress) {
                            val ip = inetAddress.hostAddress.toString()
                            return if (verifi(ip)) {
                                ip
                            } else {
                                "0.0.0.0"
                            }

                        }
                    }
                }
            } catch (ex: SocketException) {
                LogUtils.e("WifiPreference IpAddress", ex.toString())
            }

            return "0.0.0.0"
        }

    /**
     * 获取设备信息
     *
     * @return
     */
    fun buildOsInfo(context: Context?): DeviceInfo {
        val osInfo = DeviceInfo()

        if (context != null) {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            osInfo.setAndroidID(androidId)
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            osInfo.setImei(getRightIMEI(tm.deviceId))
            val imsi = tm.subscriberId
            osInfo.setImsi(imsi)
            if (imsi != null && imsi.length >= 5) {
                osInfo.setMcc(imsi.substring(0, 3))
                osInfo.setMnc(imsi.substring(3, 5))
            }
            osInfo.setIp(localIpAddress)
            osInfo.setUa(android.os.Build.MODEL)
            val versionName = getVersionName(context)
            osInfo.setCver(versionName)
            osInfo.setOsVersion("Android" + android.os.Build.VERSION.RELEASE)
            val brand = android.os.Build.BRAND
            osInfo.setBrand(brand)
            // 初始化分辨率
            val dm = DisplayMetrics()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(dm)
            osInfo.setWidthPixels(dm.widthPixels)
            osInfo.setHeightPixels(dm.heightPixels)

        }
        return osInfo
    }

    /**
     * 验证IP
     *
     * @param ip
     * @return
     */
    private fun verifi(ip: String?): Boolean {
        if (ip == null) {
            return false
        }
        val patter = "\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b"
        val p = Pattern.compile(patter)
        val m = p.matcher(ip)
        return if (m.find()) true else false
    }

    /**
     * 获取版本名
     *
     * @return
     */
    private fun getVersionName(context: Context): String {
        var versionName = ""
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
            if (TextUtils.isEmpty(versionName)) {
                versionName = ""
            }
        } catch (e: Exception) {
            LogUtils.e("VersionInfo", "Exception " + e.message)
        }

        return versionName
    }

    /**
     * get the right pattern imei
     */
    private fun getRightIMEI(imei: String?): String? {
        var imei = imei
        if ("" != imei && null != imei && "null" != imei) {
            val length = imei.length
            for (i in 0 until length) {
                if (Character.isLetter(imei!![i])) {
                    imei = imei.replace(imei[i], '0')
                }
            }
            val imeiPatterLength = 15// imei standard pattern length
            if (length < imeiPatterLength) {
                for (i in 0 until imeiPatterLength) {
                    imei += "9"
                    if (imei!!.length == imeiPatterLength) {
                        break
                    }
                }
            } else if (length > imeiPatterLength) {
                imei = imei!!.substring(0, imeiPatterLength)
            }
        }
        return imei
    }

}
