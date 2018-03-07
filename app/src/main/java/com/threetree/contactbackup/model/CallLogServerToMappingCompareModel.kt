package com.threetree.contactbackup.model

import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.CallLogBean
import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager

import java.util.ArrayList
import java.util.Hashtable

class CallLogServerToMappingCompareModel {

    internal lateinit var serverList: List<CallLogBean>
    private var mCacheStatisticsManager: CacheStatisticsManager? = null
    private var mDBManager: GreenDaoDBManager? = null
    private var serverAddList: MutableList<CallLogBean>? = null
    /*
    * K : md5 , V : CallLogBean
    * */
    internal lateinit var callLogHashtable: Hashtable<String, CallLogBean>


    /**
     */
    constructor(serverList: List<CallLogBean>) {
        this.serverList = serverList
        mCacheStatisticsManager = Factory.get()!!.cacheStatisticsManager
        mDBManager = Factory.get()!!.dbManager
    }

    constructor() {
        mCacheStatisticsManager = Factory.get()!!.cacheStatisticsManager
        mDBManager = Factory.get()!!.dbManager
    }

    fun setServerList(serverList: List<CallLogBean>) {
        this.serverList = serverList
    }

    fun doCompare() {
        callLogHashtable = createHashMap(serverList)
        compare(callLogHashtable)
        mCacheStatisticsManager!!.callLogServerAddList = serverAddList
        mCacheStatisticsManager!!.callIntoMappingList = serverAddList

    }

    private fun createHashMap(callLogBeanList: List<CallLogBean>): Hashtable<String, CallLogBean> {
        val callLogHashtable = Hashtable<String, CallLogBean>()
        for (callLogBean in callLogBeanList) {
            callLogHashtable.put(callLogBean.serverid, callLogBean)
        }
        return callLogHashtable
    }


    private fun compare(callLogServerList: Hashtable<String, CallLogBean>) {
        serverAddList = ArrayList<CallLogBean>()
        val idmappinglist = mDBManager!!.smSidmappingList
        val entries = callLogServerList.entries
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
