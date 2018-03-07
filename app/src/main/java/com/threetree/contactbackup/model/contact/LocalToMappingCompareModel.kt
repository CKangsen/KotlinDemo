package com.threetree.contactbackup.model.contact

import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.util.LogUtils

import java.util.ArrayList
import java.util.Hashtable



class LocalToMappingCompareModel {

    private var addList: MutableList<Int>? = null
    private var deleteList: MutableList<Int>? = null
    private var updateList: MutableList<Int>? = null

    private var mLocalHashtable: Hashtable<Int, Int>? = null
    private var mMappingHashtable: Hashtable<Int, Int>? = null

    fun doCompare(localHashtable: Hashtable<Int, Int>, mappingHashtable: Hashtable<Int, Int>) {
        LogUtils.d(TAG, "  doCompare")
        //init
        addList = ArrayList()
        deleteList = ArrayList()
        updateList = ArrayList()
        mLocalHashtable = localHashtable
        mMappingHashtable = mappingHashtable

        compare(mLocalHashtable, mMappingHashtable)
        val cacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        cacheStatisticsManager.setAddLocalList(addList)
        cacheStatisticsManager.setDeleteLocalList(deleteList)
        cacheStatisticsManager.setUpdateLocalList(updateList)
        LogUtils.d(TAG, " setAddLocalList size:" + addList!!.size)
        LogUtils.d(TAG, " setDeleteLocalList size:" + deleteList!!.size)
        LogUtils.d(TAG, " setUpdateLocalList size:" + updateList!!.size)
        val initTime = System.currentTimeMillis()
        LogUtils.d(TAG, " doCompare Time:" + (System.currentTimeMillis() - initTime))
    }

    private fun compare(localHashtable: Hashtable<Int, Int>?, mappingHashtable: Hashtable<Int, Int>?) {
        val entries = localHashtable!!.entries
        val iterator = entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (mappingHashtable!!.containsKey(entry.key)) {
                if (entry.value !== mappingHashtable[entry.key]) {
                    updateList!!.add(entry.key)
                }
            } else {
                addList!!.add(entry.key)
            }
        }

        val entries1 = mappingHashtable!!.entries
        val iterator1 = entries1.iterator()
        while (iterator1.hasNext()) {
            val entry = iterator1.next()
            if (localHashtable.containsKey(entry.key)) {

            } else {
                deleteList!!.add(entry.key)
            }
        }

    }

    companion object {

        val TAG = LocalToMappingCompareModel::class.java.simpleName
    }
}
