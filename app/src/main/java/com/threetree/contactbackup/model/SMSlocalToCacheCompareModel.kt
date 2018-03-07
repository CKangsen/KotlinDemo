package com.threetree.contactbackup.model

import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.SmsInfo
import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager
import com.threetree.contactbackup.util.GetMD5Utils

import java.util.ArrayList
import java.util.Hashtable



class SMSlocalToCacheCompareModel {
    internal var localList: List<SmsInfo>
    private var mCacheStatisticsManager: CacheStatisticsManager? = null
    private var mDBManager: GreenDaoDBManager? = null
    internal var smsInfoHashtable: Hashtable<String, SmsInfo>
    private var serverAddList: MutableList<SmsInfo>? = null
    private var uploaddList: MutableList<SmsInfo>? = null

    private var locallist: MutableList<SmsInfo>? = null

    /*
    * 传进两个服务器和本地
    * 进行数据全量匹配
    * name字段和电话号码数组拼成的String求MD5，
    * MD5作为key，数据id+版本号 作为value
    * */
    constructor(localList: List<SmsInfo>) {
        this.localList = localList
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        mDBManager = Factory.get().getDBManager()
    }

    constructor() {
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        mDBManager = Factory.get().getDBManager()
    }

    fun setLocalList(localList: List<SmsInfo>) {
        this.localList = localList
    }


    private fun createseverMap(smsInfo: List<SmsInfo>?): Hashtable<String, SmsInfo> {
        val smsInfoHashtable = Hashtable<String, SmsInfo>()
        for (mSmsInfo in smsInfo!!) {
            smsInfoHashtable.put(GetMD5Utils.getMD5(mSmsInfo.getAddress()
                    + mSmsInfo.getDate() + mSmsInfo.getType()), mSmsInfo)
        }
        return smsInfoHashtable
    }

    fun doCompare() {
        var mSMSInfoHashtable = Hashtable<String, SmsInfo>()
        val idmappinglist = mDBManager!!.getSMSidmappingList()
        smsInfoHashtable = createseverMap(localList)
        locallist = ArrayList<SmsInfo>()
        val entries = smsInfoHashtable.entries
        val iterator = entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (idmappinglist.containsKey(entry.key)) {
            } else {
                locallist!!.add(entry.value)
            }
        }
        if (locallist!!.size > 0) {
            mSMSInfoHashtable = createseverMap(locallist)
        }
        compare(mSMSInfoHashtable)
        mCacheStatisticsManager!!.setSmsServerAddList(serverAddList)
        mCacheStatisticsManager!!.setSmsuploadList(uploaddList)
        mCacheStatisticsManager!!.addsmsIntoMappingList(uploaddList)
    }

    //传入本地的hashtable
    private fun compare(smsInfoList: Hashtable<String, SmsInfo>) {
        serverAddList = ArrayList<SmsInfo>()
        uploaddList = ArrayList<SmsInfo>()
        //缓存有 本地没有的 生成最终要备份到本地的短信 存到中间表serverAddList
        val cachesmsInfoHashtable = createseverMap(mCacheStatisticsManager!!.getSmsServerAddList())
        if (cachesmsInfoHashtable != null && cachesmsInfoHashtable.size > 0) {
            val entries = cachesmsInfoHashtable.entries
            val iterator = entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (smsInfoList.containsKey(entry.key)) {

                } else {
                    serverAddList!!.add(entry.value)
                }
            }
        }

        //本地有 缓存表没有的上传到服务器
        val entries1 = smsInfoList.entries
        val iterator1 = entries1.iterator()
        while (iterator1.hasNext()) {
            val entry = iterator1.next()
            if (cachesmsInfoHashtable.size > 0 && cachesmsInfoHashtable.containsKey(entry.key)) {

            } else {
                uploaddList!!.add(entry.value)
            }
        }

    }

}
