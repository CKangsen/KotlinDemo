package com.threetree.contactbackup.model.contact


import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.IDMappingValue
import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager
import com.threetree.contactbackup.util.ListUtils
import com.threetree.contactbackup.util.LogUtils


class UpdateListConflictCompareModel {

    private val mCacheStatisticsManager: CacheStatisticsManager
    private val mDBManager: GreenDaoDBManager

    init {
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        mDBManager = Factory.get().getDBManager()
    }

    fun doCompare() {
        LogUtils.d(TAG, "  doCompare")
        val serverChangeToLocal = mDBManager.getMappingLocalIdListByServerId(mCacheStatisticsManager.getUpdateServerList())
        //比较本地修改的联系人和云端修改的联系人，得到冲突的修改联系人集合
        val conflictList = ListUtils.getRepetition(mCacheStatisticsManager.getUpdateLocalList(), serverChangeToLocal)

        //排除冲突的修改联系人，得到最后需要更新到云端的联系人集合
        val finalLocalUpdate = ListUtils.getDifferentListInTwoLists(mCacheStatisticsManager.getUpdateLocalList(), conflictList)
        mCacheStatisticsManager.setUpdateLocalList(finalLocalUpdate)
        mCacheStatisticsManager.setConflictUpdateLocalList(conflictList)
        insertUpdateConflictContact(conflictList)
        LogUtils.d(TAG, "  setConflictUpdateLocalList size:" + conflictList.size)
        LogUtils.d(TAG, "  setUpdateLocalList size:" + finalLocalUpdate.size)

        //冲突的修改联系人集合 转化为云端id 集合
        val conflictLocalChangeToServer = mDBManager.getMappingServerIdListByLocalId(conflictList)
        mCacheStatisticsManager.setConflictUpdateServerList(conflictLocalChangeToServer)
        LogUtils.d(TAG, "  setConflictUpdateServerList size:" + conflictLocalChangeToServer.size)
        //排除冲突的修改联系人，得到最后需要更新到本地的联系人集合
        val finalServerUpdate = ListUtils.getDifferentStringListInTwoLists(mCacheStatisticsManager.getUpdateServerList(), conflictLocalChangeToServer)
        mCacheStatisticsManager.setUpdateServerList(finalServerUpdate)
        LogUtils.d(TAG, "  setUpdateServerList size:" + finalServerUpdate.size)

    }

    fun insertUpdateConflictContact(conflictContactIds: List<Int>?) {
        LogUtils.d(TAG, "insertUpdateConflictContact")
        if (conflictContactIds == null || conflictContactIds.size <= 0) {
            LogUtils.d(TAG, "insertUpdateConflictContact conflictContactIds == null || conflictContactIds.size() <= 0")
            return
        }
        for (id in conflictContactIds) {
            mDBManager.insertInUpdateConflictContact(id)
        }
    }

    companion object {

        val TAG = UpdateListConflictCompareModel::class.java.simpleName
    }

}
