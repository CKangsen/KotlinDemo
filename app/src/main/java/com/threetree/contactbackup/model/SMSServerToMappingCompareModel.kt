package com.threetree.contactbackup.model

import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.SmsInfo
import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager

import java.util.ArrayList
import java.util.Hashtable


class SMSServerToMappingCompareModel {

    internal var serverList: List<SmsInfo>
    private var mCacheStatisticsManager: CacheStatisticsManager? = null
    private var mDBManager: GreenDaoDBManager? = null
    private var serverAddList: MutableList<SmsInfo>? = null
    internal var smsInfoHashtable: Hashtable<String, SmsInfo>


    /*
    * 传进两个服务器和本地
    * 进行数据全量匹配
    * name字段和电话号码数组拼成的String求MD5，
    * MD5作为key，数据id+版本号 作为value
    * */
    constructor(serverList: List<SmsInfo>) {
        this.serverList = serverList
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        mDBManager = Factory.get().getDBManager()
    }

    constructor() {
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        mDBManager = Factory.get().getDBManager()
    }

    fun setServerList(serverList: List<SmsInfo>) {
        this.serverList = serverList
    }

    fun doCompare() {
        smsInfoHashtable = createseverMap(serverList)
        compare(smsInfoHashtable)
        mCacheStatisticsManager!!.setSmsServerAddList(serverAddList)
        mCacheStatisticsManager!!.setsmsIntoMappingList(serverAddList)
    }

    private fun createseverMap(smsInfo: List<SmsInfo>): Hashtable<String, SmsInfo> {
        val smsInfoHashtable = Hashtable<String, SmsInfo>()
        for (mSmsInfo in smsInfo) {
            smsInfoHashtable.put(mSmsInfo.getServer_id(), mSmsInfo)
        }
        return smsInfoHashtable
    }

    private fun compare(smsInfoList: Hashtable<String, SmsInfo>) {
        serverAddList = ArrayList<SmsInfo>()
        val idmappinglist = mDBManager!!.getSMSidmappingList()
        val entries = smsInfoList.entries
        val iterator = entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (idmappinglist.containsKey(entry.key)) {
            } else {
                serverAddList!!.add(entry.value)
            }
        }


    }

}
