package com.threetree.contactbackup

import android.content.Context
import android.content.SharedPreferences


class ApplicationPrefsManager internal constructor(private val mContext: Context) {

    var server_big_version: Int
        get() = getInt(SERVER_BIG_VERSION, 0)
        set(server_big_version) = putInt(SERVER_BIG_VERSION, server_big_version)


    /**
     * 获取openid
     * @return
     */
    val openid: Long
        get() = getLong(OPEN_ID, 0)

    /**
     * 获取服务器最新的短信 时间
     * @return
     */
    /**
     * 保存服务器最新的短信 时间
     */
    var smSlatestcall: Long
        get() = getLong(LATESETCALL, -1)
        set(callts) = putLong(LATESETCALL, callts)

    /**
     * 获取服务器最新的通话记录 时间
     * @return
     */
    /**
     * 保存服务器最新的通话记录 时间
     */
    var calllatestcall: Long
        get() = getLong(CALL_LATESETCALL, -1)
        set(callts) = putLong(CALL_LATESETCALL, callts)

    /**
     * 是否打开同步通讯录开关
     * @return
     */
    /**
     * 设置同步通讯录开关
     * @param isSyncOn
     */
    var isSyncContacts: Boolean
        get() = getBoolean(SYNC_CONTACTS, true)
        set(isSyncOn) {
            putBoolean(SYNC_CONTACTS, isSyncOn)
            if (!isSyncOn) {
                Factory.get().getCacheStatisticsManager().clearContactCompareStatistics()
            }
        }
    var isSyncMessages: Boolean
        get() = getBoolean(SYNC_MESSAGES, true)
        set(isSyncOn) {
            putBoolean(SYNC_MESSAGES, isSyncOn)
            if (!isSyncOn) {
                Factory.get().getCacheStatisticsManager().clearSMSCompareStatistics()
            }
        }
    var isSyncPhone: Boolean
        get() = getBoolean(SYNC_PHONE, true)
        set(isSyncOn) {
            putBoolean(SYNC_PHONE, isSyncOn)
            if (!isSyncOn) {
                Factory.get().getCacheStatisticsManager().clearCallLogCompareStatistics()
            }
        }

    var localCurrentContactCount: Int
        get() = getInt(LOCAL_CURRENT_CONTACT_COUNT, 0)
        set(count) = putInt(LOCAL_CURRENT_CONTACT_COUNT, count)

    var cloudCurrentContactCount: Int
        get() = getInt(CLOUD_CURRENT_CONTACT_COUNT, 0)
        set(count) = putInt(CLOUD_CURRENT_CONTACT_COUNT, count)

    var lastSyncSuccessTimestamp: Long
        get() = getLong(LAST_SYNC_SUCCESS_TIMESTAMP, 0)
        set(timestamp) = putLong(LAST_SYNC_SUCCESS_TIMESTAMP, timestamp)

    /**
     * 保存openid
     */
    fun setOpenId(openId: Long) {
        putLong(OPEN_ID, openId)
    }


    fun getInt(key: String, defaultValue: Int): Int {
        val prefs = mContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        val prefs = mContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val prefs = mContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(key, defaultValue)
    }

    fun getString(key: String, defaultValue: String): String? {
        val prefs = mContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, defaultValue)
    }

    fun putInt(key: String, value: Int) {
        val prefs = mContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun putLong(key: String, value: Long) {
        val prefs = mContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        val prefs = mContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun putString(key: String, value: String) {
        val prefs = mContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun remove(key: String) {
        val prefs = mContext.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(key)
        editor.apply()
    }

    companion object {

        val SHARED_PREFERENCES_NAME = "wakaicloud"
        val SERVER_BIG_VERSION = "server_big_version"//记录服务器上的大版本号
        val OPEN_ID = "openid"

        /*同步联系人开关*/
        private val SYNC_CONTACTS = "sync_contacts"
        /*同步短信开关*/
        private val SYNC_MESSAGES = "sync_messages"
        /*同步通话记录开关*/
        private val SYNC_PHONE = "sync_phone"
        /*最近一次成功同步的時間戳*/
        private val LAST_SYNC_SUCCESS_TIMESTAMP = "last_sync_success_timestamp"
        /*本地現有的聯繫人數目*/
        private val LOCAL_CURRENT_CONTACT_COUNT = "local_current_contact_count"
        /*雲端現有的聯繫人數目*/
        private val CLOUD_CURRENT_CONTACT_COUNT = "cloud_current_contact_count"

        private val LATESETCALL = "latestcallts"

        private val CALL_LATESETCALL = "call_latestcallts"
        private var instance: ApplicationPrefsManager? = null

        fun getInstance(context: Context): ApplicationPrefsManager {
            if (instance == null) {
                instance = ApplicationPrefsManager(context)
            }
            return instance
        }
    }


}
