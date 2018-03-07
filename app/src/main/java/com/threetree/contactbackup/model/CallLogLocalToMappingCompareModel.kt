package com.threetree.contactbackup.model

import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.CallLogBean
import com.threetree.contactbackup.db.GreenDaoDBManager

import java.util.ArrayList
import java.util.Hashtable


class CallLogLocalToMappingCompareModel {

    internal lateinit var localList: List<CallLogBean>
    private var mCacheStatisticsManager: CacheStatisticsManager? = null
    private var mDBManager: GreenDaoDBManager? = null
    /*
    * K : md5 , V : CallLogBean
    * */
    internal lateinit var callLogHashtable: Hashtable<String, CallLogBean>
    private var serverAddList: MutableList<CallLogBean>? = null
    private var uploadList: MutableList<CallLogBean>? = null

    private var locallist: MutableList<CallLogBean>? = null

    /*
    *
    *
    * */
    constructor(localList: List<CallLogBean>) {
        this.localList = localList
        mCacheStatisticsManager = Factory.get()!!.cacheStatisticsManager
        mDBManager = Factory.get()!!.dbManager
    }

    constructor() {
        mCacheStatisticsManager = Factory.get()!!.cacheStatisticsManager
        mDBManager = Factory.get()!!.dbManager
    }

    fun setLocalList(localList: List<CallLogBean>) {
        this.localList = localList
    }


    private fun createHashMap(callLogBeanList: List<CallLogBean>?): Hashtable<String, CallLogBean> {
        val callLogHashtable = Hashtable<String, CallLogBean>()
        for (callLogBean in callLogBeanList!!) {
            callLogHashtable.put(callLogBean.serverid, callLogBean)
        }
        return callLogHashtable
    }

    fun doCompare() {
        //        callLogHashtable = createHashMap(localList);
        //        compare(callLogHashtable);

        var mCallLogInfoHashtable = Hashtable<String, CallLogBean>()
        val idmappinglist = mDBManager!!.callLogIdMappingList
        callLogHashtable = createHashMap(localList)
        locallist = ArrayList<CallLogBean>()
        val entries = callLogHashtable.entries
        val iterator = entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (idmappinglist.containsKey(entry.key)) {
            } else {
                locallist!!.add(entry.value)
            }
        }
        if (locallist!!.size > 0) {
            mCallLogInfoHashtable = createHashMap(locallist as ArrayList<CallLogBean>)
        }
        compare(mCallLogInfoHashtable)

        mCacheStatisticsManager!!.callLogServerAddList = serverAddList
        mCacheStatisticsManager!!.callLogUploadList = uploadList
        mCacheStatisticsManager!!.addCallIntoMappingList(uploadList)
    }

    //传入本地的hashtable
    private fun compare(callLogLocalList: Hashtable<String, CallLogBean>) {
        serverAddList = ArrayList<CallLogBean>()
        uploadList = ArrayList<CallLogBean>()
        //缓存有 本地没有的 生成最终要备份到本地的短信 存到中间表serverAddList
        val cacheCallLogHashtable = createHashMap(mCacheStatisticsManager!!.callLogServerAddList)
        if (cacheCallLogHashtable != null && cacheCallLogHashtable.size > 0) {
            val entries = cacheCallLogHashtable.entries
            val iterator = entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (callLogLocalList.containsKey(entry.key)) {

                } else {
                    serverAddList!!.add(entry.value)
                }
            }
        }

        //本地有 缓存表没有的上传到服务器
        val entries1 = callLogLocalList.entries
        val iterator1 = entries1.iterator()
        while (iterator1.hasNext()) {
            val entry = iterator1.next()
            if (cacheCallLogHashtable.size > 0 && cacheCallLogHashtable.containsKey(entry.key)) {

            } else {
                uploadList!!.add(entry.value)
            }
        }

    }
}
