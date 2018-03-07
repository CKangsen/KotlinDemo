package com.threetree.contactbackup.model.contact

import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager
import com.threetree.contactbackup.util.LogUtils

import java.util.ArrayList
import java.util.Hashtable


class ServerToMappingCompareModel {

    private var addList: MutableList<String>? = null
    private var deleteList: MutableList<String>? = null
    private var updateList: MutableList<String>? = null

    private var mServerHashtable: Hashtable<String, Int>? = null
    private var mMappingHashtable: Hashtable<String, Int>? = null

    private val mDBManager: GreenDaoDBManager

    init {
        mDBManager = Factory.get().getDBManager()
    }

    fun doCompare(serverHashtable: Hashtable<String, Int>, mappingHashtable: Hashtable<String, Int>) {
        LogUtils.d(TAG, "  doCompare")
        //init
        addList = ArrayList()
        deleteList = ArrayList()
        updateList = ArrayList()
        mServerHashtable = serverHashtable
        mMappingHashtable = mappingHashtable

        compare(mServerHashtable, mMappingHashtable)
        val cacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        cacheStatisticsManager.setAddServerList(addList)
        cacheStatisticsManager.setDeleteServerList(deleteList)
        cacheStatisticsManager.setUpdateServerList(updateList)
        LogUtils.d(TAG, " setAddServerList size:" + addList!!.size)
        LogUtils.d(TAG, " setDeleteServerList size:" + deleteList!!.size)
        LogUtils.d(TAG, " setUpdateServerList size:" + updateList!!.size)
        val initTime = System.currentTimeMillis()
        LogUtils.d(TAG, " doCompare Time:" + (System.currentTimeMillis() - initTime))
    }

    private fun compare(serverHashtable: Hashtable<String, Int>?, mappingHashtable: Hashtable<String, Int>?) {
        val entries = serverHashtable!!.entries
        val iterator = entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (mappingHashtable!!.containsKey(entry.key)) {
                if (entry.value !== mappingHashtable[entry.key]) {
                    updateList!!.add(entry.key)
                    mDBManager.updateLatestVInMappingByServerid(entry.key, entry.value)
                }
            } else {
                addList!!.add(entry.key)
            }
        }

        val entries1 = mappingHashtable!!.entries
        val iterator1 = entries1.iterator()
        while (iterator1.hasNext()) {
            val entry = iterator1.next()
            if (serverHashtable.containsKey(entry.key)) {

            } else {
                deleteList!!.add(entry.key)
            }
        }

    }

    companion object {

        val TAG = ServerToMappingCompareModel::class.java.simpleName
    }
}
