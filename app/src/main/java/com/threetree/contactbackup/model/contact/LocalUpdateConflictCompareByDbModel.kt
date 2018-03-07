package com.threetree.contactbackup.model.contact

import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager
import com.threetree.contactbackup.util.ListUtils
import com.threetree.contactbackup.util.LogUtils



class LocalUpdateConflictCompareByDbModel {

    private val mCacheStatisticsManager: CacheStatisticsManager
    private val mDBManager: GreenDaoDBManager

    init {
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        mDBManager = Factory.get().getDBManager()
    }

    fun doCompare() {
        LogUtils.d(TAG, "  doCompare")
        val conflictContactIdsInDB = mDBManager.getUpdateConflictContactIds()
        if (conflictContactIdsInDB == null || conflictContactIdsInDB!!.size <= 0) {
            LogUtils.d(TAG, "  conflictContactIdsInDB size<=0 or null")
            return
        }
        //比较本地修改的联系人和数据库存储的冲突修改联系人，得到新增加冲突的修改联系人集合
        val conflictList = ListUtils.getRepetition(mCacheStatisticsManager.getUpdateLocalList(), conflictContactIdsInDB)

        //排除冲突的修改联系人，得到最后需要更新到云端的联系人集合
        val finalLocalUpdate = ListUtils.getDifferentListInTwoLists(mCacheStatisticsManager.getUpdateLocalList(), conflictList)
        mCacheStatisticsManager.setUpdateLocalList(finalLocalUpdate)
        LogUtils.d(TAG, "  setUpdateLocalList size:" + finalLocalUpdate.size)

    }

    companion object {

        val TAG = LocalUpdateConflictCompareByDbModel::class.java.simpleName
    }
}
