package com.threetree.contactbackup.model.contact

import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager
import com.threetree.contactbackup.util.IdPatternUtils
import com.threetree.contactbackup.util.ListUtils
import com.threetree.contactbackup.util.LogUtils

import java.util.ArrayList
import java.util.Hashtable

/**
 * 比较本地删除的数据版本号与云端是否一致
 * 一致可提交删除请求，不一致不能提交删除请求
 *
 * 本地删除与云端修改存在冲突，优先级为选择删除操作
 *
 *
 */

class LocalDeleteVersionConsistentCompareModel {

    private var mServerHashtable: Hashtable<String, Int>? = null

    private val mCacheStatisticsManager: CacheStatisticsManager
    private val mDBManager: GreenDaoDBManager

    init {
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        mDBManager = Factory.get().getDBManager()

    }

    fun doCompare(serverHashtable: Hashtable<String, Int>) {
        LogUtils.d(TAG, "  doCompare")

        mServerHashtable = serverHashtable
        if (mServerHashtable == null || mServerHashtable!!.size <= 0) {
            LogUtils.d(TAG, "  mServerHashtable == null || mServerHashtable.size()<=0")
            return
        }

        val serverDeleteIds_integrated = mDBManager.getServerIdListByLocalId(mCacheStatisticsManager.getDeleteLocalList())
        val noConsistentIds = ArrayList<String>()
        for (idAndV in serverDeleteIds_integrated) {
            val serverid = IdPatternUtils.getIdByParseServerId(idAndV)
            val serverV = IdPatternUtils.getVersionByParseServerId(idAndV)
            if (mServerHashtable!!.containsKey(serverid)) {
                if (mServerHashtable!![serverid] == serverV) {
                    //版本一致，可以提交到删除请求
                } else {
                    //版本不一致，不可以提交到删除请求，从缓存和映射表移除
                    noConsistentIds.add(serverid)
                }
            }
        }

        if (noConsistentIds.size > 0) {
            val noConsistentIdsChangeToLocal = mDBManager.getMappingLocalIdListByServerId(noConsistentIds)
            val finalDeleteLocalIds = ListUtils.getDifferentListInTwoLists(mCacheStatisticsManager.getDeleteLocalList(), noConsistentIdsChangeToLocal)
            mCacheStatisticsManager.setDeleteLocalList(finalDeleteLocalIds)
            LogUtils.d(TAG, "  setDeleteLocalList final size:" + finalDeleteLocalIds.size)

            val finalUpdateServerIds = ListUtils.getDifferentStringListInTwoLists(mCacheStatisticsManager.getUpdateServerList(), noConsistentIds)
            mCacheStatisticsManager.setUpdateServerList(finalUpdateServerIds)
            LogUtils.d(TAG, "  setUpdateServerList  final size:" + finalUpdateServerIds.size)

            mCacheStatisticsManager.setDeleteLocalNeedUpdateVList(noConsistentIds)
            LogUtils.d(TAG, "  setDeleteLocalNeedUpdateVList final size:" + noConsistentIds.size)
            //            for (String serverid:noConsistentIds) {
            //                mDBManager.deleteRowInMappingByServerID(serverid);
            //            }
        }


    }

    companion object {

        val TAG = LocalDeleteVersionConsistentCompareModel::class.java.simpleName
    }
}
