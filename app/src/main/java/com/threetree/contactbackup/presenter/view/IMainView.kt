package com.threetree.contactbackup.presenter.view

interface IMainView {
    /**
     * 更新界面上的增删改的统计显示
     */
    fun updateStatistics(countAddLocal: Int, countDeleteLocal: Int, countUpdateLocal: Int, countAddServer: Int, countDeleteSever: Int, countUpdateServer: Int, countLocal: Int)

    fun logninSuccess(isExistTgt: Boolean)

    fun getSMSVersionSuccess(latestbackupts: String, latestcallts: String)

    fun uploadContactsSuccess(progress: Int)

    fun uploadSMSSuccess(progress: Int)

    fun uploadCallSuccess(progress: Int)

    fun oprateFaild(code: Int, errMsg: String)

    /*拉取数据失败 回调*/
    fun loadDataFailed(code: Int, errMsg: String)

    /*上传同步失败 回调*/
    fun uploadSyncFailed(code: Int, errMsg: String)

    fun oprateFaild(errMsg: String)
    fun checkSyncWithCloudFinish()

    fun syncdataFinish()

    fun logOutFailed(code: Int, msg: String)

    fun updateLogOutView()
    fun setUpgradeInfo()

    fun checkCanSyncSuccess(isCanSync: Boolean)

}
