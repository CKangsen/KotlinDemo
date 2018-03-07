package com.threetree.contactbackup.presenter

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils


import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.R
import com.threetree.contactbackup.base.BasePresenter
import com.threetree.contactbackup.bean.CallLogBean
import com.threetree.contactbackup.bean.ContactBean
import com.threetree.contactbackup.bean.IDMappingValue
import com.threetree.contactbackup.bean.SmsInfo
import com.threetree.contactbackup.constant.WakaIcloudConstant
import com.threetree.contactbackup.db.GreenDaoDBManager
import com.threetree.contactbackup.model.CallLogLocalToMappingCompareModel
import com.threetree.contactbackup.model.CallLogRestoreModel
import com.threetree.contactbackup.model.CallLogServerToMappingCompareModel
import com.threetree.contactbackup.model.CheckUpdaeModel
import com.threetree.contactbackup.model.ContactRestoreModel
import com.threetree.contactbackup.model.DataCenterModel
import com.threetree.contactbackup.model.ICheckUpdateModel
import com.threetree.contactbackup.model.LoginModel
import com.threetree.contactbackup.model.ReadPhoneCallLogModel
import com.threetree.contactbackup.model.ReadPhoneContactModel
import com.threetree.contactbackup.model.ReadPhoneSMSModel
import com.threetree.contactbackup.model.SMSRestoreModel
import com.threetree.contactbackup.model.SMSServerToMappingCompareModel
import com.threetree.contactbackup.model.SMSlocalToCacheCompareModel
import com.threetree.contactbackup.model.contact.FullDataCompareModel
import com.threetree.contactbackup.model.contact.LocalDeleteVersionConsistentCompareModel
import com.threetree.contactbackup.model.contact.LocalToMappingCompareModel
import com.threetree.contactbackup.model.contact.LocalUpdateConflictCompareByDbModel
import com.threetree.contactbackup.model.contact.ServerToMappingCompareModel
import com.threetree.contactbackup.model.contact.UpdateListConflictCompareModel
import com.threetree.contactbackup.model.listener.DataCenterListener
import com.threetree.contactbackup.presenter.view.IMainView
import com.threetree.contactbackup.ui.WakaIcloudMainFragment
import com.threetree.contactbackup.util.DateUtils
import com.threetree.contactbackup.util.IdPatternUtils
import com.threetree.contactbackup.util.JsonUtils
import com.threetree.contactbackup.util.LogUtils
import com.threetree.contactbackup.util.NetWorkUtil
import com.threetree.contactbackup.util.PermissionUtils
import com.greendao.gen.IDMapping
import com.threetree.contactbackup.ApplicationPrefsManager

import java.util.ArrayList
import java.util.Hashtable

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import com.threetree.contactbackup.constant.WakaIcloudConstant.REQ_CODE_UNNETWORK


class MainPresenter : BasePresenter<IMainView>() {
    private val loginModel: LoginModel?
    private val mDataCenterModel: DataCenterModel
    private val mContentResolver: ContentResolver
    private val iCheckUpdateModel: ICheckUpdateModel
    private val mDBManager: GreenDaoDBManager
    private val mCacheStatisticsManager: CacheStatisticsManager
    private val mApplicationPrefsManager: ApplicationPrefsManager

    private val mSMSServerToMappingCompareModel: SMSServerToMappingCompareModel
    private val mSMSlocalToCacheCompareModel: SMSlocalToCacheCompareModel
    private val mSMSRestoreModel: SMSRestoreModel

    private val mCallLogServerToMappingCompareModel: CallLogServerToMappingCompareModel
    private val mCallLogLocalToMappingCompareModel: CallLogLocalToMappingCompareModel
    private val mCallLogRestoreModel: CallLogRestoreModel

    //短信
    private var serverSMSversion: Long = 0
    private var SMSserverList: List<SmsInfo> = ArrayList<SmsInfo>()
    private var SMSlocalList: List<SmsInfo> = ArrayList<SmsInfo>()
    //通话记录
    private var serverCallversion: Long = 0
    private var CallserverList: List<CallLogBean> = ArrayList<CallLogBean>()
    private var CalllocalList: List<CallLogBean> = ArrayList<CallLogBean>()

    private var syncFlag = -1

    private val mFullDataCompareModel: FullDataCompareModel
    private val mLocalToMappingCompareModel: LocalToMappingCompareModel
    private val mServerToMappingCompareModel: ServerToMappingCompareModel
    private val mUpdateListConflictCompareModel: UpdateListConflictCompareModel
    private val mLocalUpdateConflictCompareByDbModel: LocalUpdateConflictCompareByDbModel
    private val mLocalDeleteVersionConsistentCompareModel: LocalDeleteVersionConsistentCompareModel
    private val mContext: Context
    internal val MainUiHandler: Handler = object : Handler(Looper.myLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                UPLOAD_CONTACTS_SUCCESS -> if (isViewAttached()) {
                    getView().uploadContactsSuccess(WakaIcloudMainFragment.PERCENTAGE_SYNC_CONTACT_DONE)
                }
                UPLOAD_SMS_SUCCESS -> if (isViewAttached()) {
                    getView().uploadSMSSuccess(WakaIcloudMainFragment.PERCENTAGE_SYNC_SMS_DONE)
                }
                UPLOAD_CALL_SUCCESS -> if (isViewAttached()) {
                    getView().uploadCallSuccess(WakaIcloudMainFragment.PERCENTAGE_SYNC_CALLLOG_DONE)
                }
                SYNC_DATA_FINISH -> if (isViewAttached()) {
                    getView().syncdataFinish()
                }
                UPLOAD_SYNC_FAILED -> if (isViewAttached()) {
                    val code = msg.arg1
                    val errorMsg = if (msg.obj is String) msg.obj as String else null
                    getView().uploadSyncFailed(code, errorMsg)
                }
            }
        }
    }

    init {
        mContentResolver = Factory.get()!!.applicationContext.getContentResolver()
        mDBManager = Factory.get()!!.dbManager
        mCacheStatisticsManager = Factory.get()!!.cacheStatisticsManager
        mApplicationPrefsManager = ApplicationPrefsManager.getInstance(Factory.get()!!.applicationContext)
        mDataCenterModel = DataCenterModel()
        loginModel = LoginModel()
        iCheckUpdateModel = CheckUpdaeModel()
        mSMSServerToMappingCompareModel = SMSServerToMappingCompareModel()
        mSMSlocalToCacheCompareModel = SMSlocalToCacheCompareModel()
        mSMSRestoreModel = SMSRestoreModel()

        mCallLogServerToMappingCompareModel = CallLogServerToMappingCompareModel()
        mCallLogLocalToMappingCompareModel = CallLogLocalToMappingCompareModel()
        mCallLogRestoreModel = CallLogRestoreModel()
        mFullDataCompareModel = FullDataCompareModel()
        mLocalToMappingCompareModel = LocalToMappingCompareModel()
        mServerToMappingCompareModel = ServerToMappingCompareModel()
        mUpdateListConflictCompareModel = UpdateListConflictCompareModel()
        mLocalUpdateConflictCompareByDbModel = LocalUpdateConflictCompareByDbModel()
        mLocalDeleteVersionConsistentCompareModel = LocalDeleteVersionConsistentCompareModel()
        mContext = Factory.get()!!.applicationContext
    }

    fun sentMessageUploadSyncFailed(code: Int, msg: String?) {
        val message = Message()
        message.what = UPLOAD_SYNC_FAILED
        message.arg1 = code
        message.obj = msg
        MainUiHandler.sendMessage(message)
    }

    /**
     * 获取新版本升级信息
     */
    fun getUpgradeInfo() {
        val upgradeInfo = iCheckUpdateModel.getUpgradeInfo()
        if (upgradeInfo != null) {
            getView().setUpgradeInfo()
        }
    }

    /**
     * TUDC 登录
     * @param activity
     */
    fun tudcLogin(activity: Activity) {
        if (ValidateUtil.isExistTgt(false)) {
            TUDCSdkInnerManager.getManager().getStByTgt(object : TudcInnerListener.TudcGetUserStListener() {
                fun onTudcGetUserStSuccess(st: String, uname: String, avatar: String) {
                    if (!TextUtils.isEmpty(st)) {
                        /*TUDC登录成功后 调取App的login*/
                        login(st, true)
                    }
                }

                fun onTudcGetUserStError(i: Int, s: String) {
                    if (isViewAttached()) {
                        getView().oprateFaild(i, s)
                    }
                }
            })

        } else {
            loginModel!!.login(activity, object : DataCenterListener.OnTudcLogninResponListener() {
                fun logninSuccess(st: String) {
                    login(st, false)
                }

                fun logninCancel() {

                }

                fun logninFailed(msg: String) {
                    if (isViewAttached()) {
                        getView().oprateFaild(msg)
                    }
                }
            })
            // TUDCLoginManager.getInstance().logIn(activity, LoginBehavior.SDK_PHONE_IDENTIFYING_CODE_AUTHORIZE, TUDCLoginManager.LOGIN_TYPE_SHOW);
        }
    }

    /*Tudc Login的Activity返回*/
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (loginModel != null) {
            loginModel!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun login(st: String, isExistTgt: Boolean) {
        mDataCenterModel.lognin(st, object : DataCenterListener.OnlogninResponListener() {
            fun logninSuccess() {
                if (isViewAttached()) {
                    getView().logninSuccess(isExistTgt)
                }
            }

            fun logninFailed(Code: Int, msg: String) {
                if (isViewAttached()) {
                    getView().oprateFaild(Code, msg)
                }
            }
        })
    }

    fun getContact(version: Int) {
        mDataCenterModel.getContact(version, object : DataCenterListener.OngetContactResponListener() {
            fun getContactSuccess(server_version: Int, serverContactList: List<ContactBean>,
                                  serverContactIDHashtable: Hashtable<String, Int>,
                                  serverContactDataHashtable: Hashtable<String, ContactBean>,
                                  serverDeleteContactList: List<ContactBean>) {
                //对比
                compareContactSyncWithCloud(server_version, serverContactList, serverContactIDHashtable, serverContactDataHashtable, serverDeleteContactList)
                checkSMSCall()
            }

            fun getContactFailed(Code: Int, msg: String, server_version: Int, serverContactList: List<ContactBean>,
                                 serverContactIDHashtable: Hashtable<String, Int>,
                                 serverContactDataHashtable: Hashtable<String, ContactBean>,
                                 serverDeleteContactList: List<ContactBean>) {
                //对比
                if (server_version != -1) {
                    compareContactSyncWithCloud(server_version, serverContactList, serverContactIDHashtable, serverContactDataHashtable, serverDeleteContactList)
                }
                if (isViewAttached()) {
                    getView().loadDataFailed(Code, msg)
                }
            }
        })
    }

    fun CheckCanSync() {
        if (mApplicationPrefsManager.isSyncContacts()) {
            val version = mCacheStatisticsManager.getServer_big_version()
            mDataCenterModel.getContact(version, object : DataCenterListener.OngetContactResponListener() {
                fun getContactSuccess(server_version: Int, serverContactList: List<ContactBean>, serverContactIDHashtable: Hashtable<String, Int>, serverContactDataHashtable: Hashtable<String, ContactBean>, serverDeleteContactList: List<ContactBean>) {
                    if (server_version == version) {
                        if (isViewAttached()) {
                            getView().checkCanSyncSuccess(true)
                        }
                    } else if (isViewAttached()) {
                        getView().checkCanSyncSuccess(false)
                    }
                }

                fun getContactFailed(Code: Int, msg: String, server_version: Int, serverContactList: List<ContactBean>, serverContactIDHashtable: Hashtable<String, Int>, serverContactDataHashtable: Hashtable<String, ContactBean>, serverDeleteContactList: List<ContactBean>) {

                    sentMessageUploadSyncFailed(Code, msg)
                }
            })
        } else {
            if (isViewAttached()) {
                getView().checkCanSyncSuccess(true)
            }
        }
    }

    fun uploadContact(contactJson: String) {
        mDataCenterModel.uploadContact(contactJson, object : DataCenterListener.OnuploadContactResponListener() {

            fun uploadContactSuccess(addIds: List<String>) {
                insertContactMapping(mCacheStatisticsManager.getLocalMapValueForAddContact(), addIds)
                updateContactMapping(mCacheStatisticsManager.getLocalMapValueForUpdateContact())
                deleteContactMapping(mCacheStatisticsManager.getLocalMapValueForDeleteContact(),
                        mCacheStatisticsManager.getLocalMapValueForDeleteContactNeedUpdateV())


                //设置大版本号 , 由之前获取联系人getContact数据设置的缓存版本号
                mApplicationPrefsManager.setServer_big_version(mCacheStatisticsManager.getServer_big_version() + 1)
                LogUtils.d(TAG, "uploadContactSuccess setServer_big_version:" + mApplicationPrefsManager.getServer_big_version())

                MainUiHandler.sendEmptyMessage(UPLOAD_CONTACTS_SUCCESS)

                if (syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_CONTACT()) {
                    MainUiHandler.sendEmptyMessage(SYNC_DATA_FINISH)
                }
                syncSMSCall()
            }

            fun uploadContactFailed(Code: Int, msg: String) {

                sentMessageUploadSyncFailed(Code, msg)
            }
        })
    }

    fun uploadContact() {
        if (!NetWorkUtil.isAvailable(mContext)) {
            sentMessageUploadSyncFailed(INSTANCE.getREQ_CODE_UNNETWORK(), null)
            return
        }
        val model = ReadPhoneContactModel(mContentResolver)
        val team = model.getContactAllInfo()

        //把匹配上的映射关系插入映射表,在调接口前插入，因为上传数据可能为空就不调上传接口
        val mappingAllCompare = mCacheStatisticsManager.getMappingByAllCompare()
        val mappingByIncrementalCompare = mCacheStatisticsManager.getMappingByIncrementalCompare()
        insertCacheMappingToDB(mappingAllCompare)
        insertCacheMappingToDB(mappingByIncrementalCompare)

        if ((mCacheStatisticsManager.getUpdateLocalList() == null || mCacheStatisticsManager.getUpdateLocalList().size < 1)
                && (mCacheStatisticsManager.getAddLocalList() == null || mCacheStatisticsManager.getAddLocalList().size < 1)
                && (mCacheStatisticsManager.getDeleteLocalList() == null || mCacheStatisticsManager.getDeleteLocalList().size < 1)
                && (mCacheStatisticsManager.getDeleteLocalNeedUpdateVList() == null || mCacheStatisticsManager.getDeleteLocalNeedUpdateVList().size < 1)) {
            //已执行恢复服务器联系人数据到本地，如无上传数据，则设置本地版本号
            val v = Factory.get().getCacheStatisticsManager().getServer_big_version()
            Factory.get().getApplicationPrefsManager().setServer_big_version(v)
            LogUtils.d(TAG, "no uploadContact setServer_big_version:" + v)

            MainUiHandler.sendEmptyMessage(UPLOAD_CONTACTS_SUCCESS)
            if (syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_CONTACT()) {
                MainUiHandler.sendEmptyMessage(SYNC_DATA_FINISH)
            }
            syncSMSCall()
            return
        }

        //把本地获取的修改联系人的数据集合 ，设置服务器的id
        val updateDataList = model.getContactInfoAllDataByIdList(mCacheStatisticsManager.getUpdateLocalList())
        //查询映射表中的服务器id+v
        val serverDataList = mCacheStatisticsManager.getServerContactList()
        for (contactBean in updateDataList) {
            val idMappingList = mDBManager.queryRowInMappingByLocalID(contactBean.get_id())
            val cu = idMappingList.get(0)
            val tempserverid = cu.getServer_id()
            val serverversion = cu.getServer_version()!!.toString()
            contactBean.setServerId(tempserverid)
            contactBean.setServer_version(serverversion)

        }

        if (!NetWorkUtil.isAvailable(mContext)) {
            sentMessageUploadSyncFailed(INSTANCE.getREQ_CODE_UNNETWORK(), null)
            return
        }
        val uploadString = JsonUtils.ContactCreateUpLoadJson(
                model.getContactInfoAllDataByIdList(mCacheStatisticsManager.getAddLocalList()),
                updateDataList,
                mDBManager.getIDMappingByLocalId(mCacheStatisticsManager.getDeleteLocalList()),
                mDBManager.getIDMappingByServerId(mCacheStatisticsManager.getDeleteLocalNeedUpdateVList()),
                mCacheStatisticsManager.getRollbackServerList())
        if ("" == uploadString) {

            MainUiHandler.sendEmptyMessage(UPLOAD_CONTACTS_SUCCESS)
            if (syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_CONTACT()) {
                MainUiHandler.sendEmptyMessage(SYNC_DATA_FINISH)
            }
        } else {
            if (!NetWorkUtil.isAvailable(mContext)) {
                sentMessageUploadSyncFailed(INSTANCE.getREQ_CODE_UNNETWORK(), null)
                return
            }
            uploadContact(uploadString)
        }

    }


    /**
     * 页面启动 就初始化这些数据
     */
    fun checkSyncWithCloud() {
        if (!NetWorkUtil.isAvailable(mContext)) {
            ToastUtil.showToast(Factory.get().getApplicationContext(), mContext.getString(R.string.network_error))
            getView().checkSyncWithCloudFinish()
            return
        }
        syncFlag = getSyncFlag()
        LogUtils.d(TAG, "checkSyncWithCloud syncFlag:" + syncFlag)
        if (syncFlag < 1) {
            getView().checkSyncWithCloudFinish()
            return
        }
        //先检查开关 有无通讯录       有通讯录 等通讯录返回成功后再检测短信和通话记录
        if (mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission()) {
            //获取服务器最新版本
            val contact_server_version = mApplicationPrefsManager.getServer_big_version()
            getContact(contact_server_version)
        } else
        //没有 就直接检测短信和通话记录
        {
            checkSMSCall()
        }


    }

    private fun checkSMSCall() {
        //先检查开关 有无短信记录
        if (mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission()) {
            //获取服务器最后短信时间
            this.getSMSversion()
        }
        //先检查开关 有无通话记录
        if (mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission()) {
            //获取服务器最后通话记录时间
            this.getCallversion()
        }
    }

    //通讯录对比
    fun compareContactSyncWithCloud(server_version: Int, serverContactList: List<ContactBean>,
                                    serverContactIDHashtable: Hashtable<String, Int>,
                                    serverContactDataHashtable: Hashtable<String, ContactBean>,
                                    serverDeleteContactList: List<ContactBean>) {

        LogUtils.d(TAG, "compareContactSyncWithCloud serverContactList size:" + serverContactList.size)
        LogUtils.d(TAG, "compareContactSyncWithCloud serverContactIDHashtable size:" + serverContactIDHashtable.size)
        LogUtils.d(TAG, "compareContactSyncWithCloud serverDeleteContactList size:" + serverDeleteContactList.size)

        //写入云端删除表
        mDBManager.batchInsertInServerdelete(serverDeleteContactList)


        if (serverContactList.size > 0) {
            mApplicationPrefsManager.setCloudCurrentContactCount(serverContactList.size)
            mCacheStatisticsManager.setIcloud_current_count(serverContactList.size)
            LogUtils.d(TAG, "compareContactSyncWithCloud setCloudCurrentContactCount size:" + serverContactList.size)
        }
        if (mApplicationPrefsManager.getServer_big_version() !== server_version && serverContactList.size == 0
                && server_version != -1) {
            //版本号不一样，返回的数据为0，即云端通讯录为0
            mApplicationPrefsManager.setCloudCurrentContactCount(0)
            mCacheStatisticsManager.setIcloud_current_count(0)
            LogUtils.d(TAG, "compareContactSyncWithCloud setCloudCurrentContactCount set 0 size")
        }
        mCacheStatisticsManager.serverContactList = serverContactList
        mCacheStatisticsManager.serverContactDataHashtable = serverContactDataHashtable
        mCacheStatisticsManager.serverDeleteContactList = serverDeleteContactList
        val readPhoneContactModel = ReadPhoneContactModel(mContentResolver)

        val isExistContactMapping = mDBManager.isExistContactMapping

        if (isExistContactMapping) {
            LogUtils.d(TAG, "  isExistContactMapping  ：" + isExistContactMapping)
            if (mApplicationPrefsManager.server_big_version === server_version) {
                LogUtils.d(TAG, "  compareContactSyncWithCloud Server_big_version same " + server_version)
                //版本号相同，只匹配本地部分
                mLocalToMappingCompareModel.doCompare(
                        readPhoneContactModel.getContactIdAndVerHashtable(),
                        mDBManager.getMappingLocalPart())

                //11.1 本地新增数据与云端删除表数据对比
                mFullDataCompareModel.doCompare(
                        readPhoneContactModel.getContactInfoByIdList(mCacheStatisticsManager.getAddLocalList()),
                        mDBManager.getListServerDeleteContactBeansOnlyIdAndMd5(), //云端无数据返回，取本地数据库
                        FullDataCompareModel.Companion.getINCREMENTAL_DELETETABLE_COMPARE())

                //本地修改联系与数据库存储的冲突修改联系人集合对比，排除已经记录过冲突的项
                mLocalUpdateConflictCompareByDbModel.doCompare()

            } else {
                LogUtils.d(TAG, "  compareContactSyncWithCloud Server_big_version not same " + server_version)
                mServerToMappingCompareModel.doCompare(
                        serverContactIDHashtable,
                        mDBManager.getMappingServerPart())
                mLocalToMappingCompareModel.doCompare(
                        readPhoneContactModel.getContactIdAndVerHashtable(),
                        mDBManager.getMappingLocalPart())

                //比较本地删除的数据版本号与云端是否一致,不一致不能提交删除请求
                mLocalDeleteVersionConsistentCompareModel.doCompare(serverContactIDHashtable)

                //8：本地新增数据与云端新增数据对比
                mFullDataCompareModel.doCompare(
                        readPhoneContactModel.getContactInfoByIdList(mCacheStatisticsManager.getAddLocalList()),
                        getListFromServerIDlist(serverContactDataHashtable, mCacheStatisticsManager.getAddServerList()),
                        FullDataCompareModel.Companion.getINCREMENTAL_COMPARE())

                //11.1 本地新增数据与云端删除表数据对比
                mFullDataCompareModel.doCompare(
                        readPhoneContactModel.getContactInfoByIdList(mCacheStatisticsManager.getAddLocalList()),
                        serverDeleteContactList, //mDBManager.getListServerDeleteContactBeansOnlyIdAndMd5(),
                        FullDataCompareModel.Companion.getINCREMENTAL_DELETETABLE_COMPARE())

                //比较本地update和云端update，排除冲突的联系人集合，并更新最后的本地update list和云端update list
                mUpdateListConflictCompareModel.doCompare()

            }
        } else {
            LogUtils.d(TAG, "  isExistContactMapping  ：" + isExistContactMapping)
            val localContactList = readPhoneContactModel.getContactInfo()
            mFullDataCompareModel.doCompare(
                    localContactList,
                    serverContactList,
                    FullDataCompareModel.Companion.getALL_COMPARE())

            //11.1 本地新增数据与云端删除表数据对比
            mFullDataCompareModel.doCompare(
                    readPhoneContactModel.getContactInfoByIdList(mCacheStatisticsManager.getAddLocalList()),
                    serverDeleteContactList, //mDBManager.getListServerDeleteContactBeansOnlyIdAndMd5(),
                    FullDataCompareModel.Companion.getINCREMENTAL_DELETETABLE_COMPARE())
        }

        //缓存设置联系人版本号，在上传成功回调后
        if (server_version == -1) {
            mCacheStatisticsManager.setServer_big_version(mApplicationPrefsManager.getServer_big_version())
        } else {
            mCacheStatisticsManager.setServer_big_version(server_version)
        }

        if (syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_CONTACT()) {
            if (isViewAttached()) {
                getView().checkSyncWithCloudFinish()
            }
        }
    }

    //短信对比
    fun compareSMSSyncWithCloud() {
        SMSlocalList = ReadPhoneSMSModel(mContentResolver).getSmsInfo()
        mSMSServerToMappingCompareModel.setServerList(SMSserverList)
        mSMSServerToMappingCompareModel.doCompare()

        mSMSlocalToCacheCompareModel.setLocalList(SMSlocalList)
        mSMSlocalToCacheCompareModel.doCompare()
        if (syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_SMS() || syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_CONTACT_SMS()) {
            if (isViewAttached()) {
                getView().checkSyncWithCloudFinish()
            }
        }
    }

    //通话记录对比
    fun compareCallLogSyncWithCloud() {
        mCallLogServerToMappingCompareModel.setServerList(CallserverList)
        mCallLogServerToMappingCompareModel.doCompare()
        CalllocalList = ReadPhoneCallLogModel(mContentResolver).getCallLogBeanList()
        mCallLogLocalToMappingCompareModel.setLocalList(CalllocalList)
        mCallLogLocalToMappingCompareModel.doCompare()
        if (isViewAttached()) {
            getView().checkSyncWithCloudFinish()
        }
    }

    /**
     * 从服务器下拉的联系人集合缓存中获取
     * 对应serverid的联系人数据
     */
    private fun getListFromServerIDlist(serverContactDataHashtable: Hashtable<String, ContactBean>, idList: List<String>?): List<ContactBean> {
        val result = ArrayList<ContactBean>()
        if (idList != null && idList.size > 0) {
            for (i in idList.indices) {
                if (serverContactDataHashtable[idList[i]] != null) {
                    result.add(serverContactDataHashtable[idList[i]])
                }
            }
        }
        return result
    }

    private fun insertCacheMappingToDB(mappingList: List<IDMappingValue>?) {
        if (mappingList == null || mappingList.size <= 0) {
            LogUtils.d(TAG, "insertCacheMappingToDB mappingList == null || mappingList.size() <= 0")
            return
        }
        val count = mappingList.size
        LogUtils.d(TAG, "insertCacheMappingToDB Count:" + count)
        for (mappingValue in mappingList) {
            mDBManager.insertInMapping(mappingValue.getContactid(),
                    mappingValue.getContactV(),
                    mappingValue.getServerid(),
                    mappingValue.getServerV(),
                    mappingValue.getMd5())
        }
    }

    private fun insertContactMapping(localMaps: List<IDMappingValue>?, serverIds: List<String>?) {
        LogUtils.d(TAG, "insertContactMapping")
        if (localMaps == null || serverIds == null) {
            LogUtils.d(TAG, "insertContactMapping localMaps $localMaps serverIds $serverIds")
            return
        }
        if (localMaps.size != serverIds.size) {
            LogUtils.d(TAG, "insertContactMapping localMaps.size() != serverIds.size()")
        } else {
            for (i in localMaps.indices) {
                val mapValue = localMaps[i] as IDMappingValue
                val localId = mapValue.getContactid()
                val localVersion = mapValue.getContactV()
                val serverId = IdPatternUtils.getIdByParseServerId(serverIds[i])
                val serverVersion = IdPatternUtils.getVersionByParseServerId(serverIds[i])
                val md5 = mapValue.getMd5()
                mDBManager.insertInMapping(localId, localVersion, serverId, serverVersion, md5)
            }
        }

        //新增云端后，重新设置云端数目缓存记录
        mApplicationPrefsManager.setCloudCurrentContactCount(mApplicationPrefsManager.getCloudCurrentContactCount() + localMaps.size)
    }

    private fun updateContactMapping(updateMap: List<IDMappingValue>?) {
        LogUtils.d(TAG, "updateContactMapping")
        if (updateMap == null || updateMap.size == 0) {
            return
        }
        for (i in updateMap.indices) {
            val mapValue = updateMap[i] as IDMappingValue
            val localId = mapValue.getContactid()
            val localVersion = mapValue.getContactV()
            val serverId = mapValue.getServerid()
            val serverVersion = Factory.get().getCacheStatisticsManager().getServer_big_version() + 1
            val md5 = mapValue.getMd5()
            mDBManager.updateRowInMapping(localId, localVersion, serverId, serverVersion, md5)
        }
    }

    private fun deleteContactMapping(deleteMaps: List<IDMappingValue>?, deleteMapsUpdateV: List<IDMappingValue>?) {
        LogUtils.d(TAG, "deleteContactMapping")
        if (deleteMaps != null && deleteMaps.size > 0) {
            for (i in deleteMaps.indices) {
                val mapValue = deleteMaps[i] as IDMappingValue
                val server_id = mapValue.getServerid()
                //写入在云端删除表
                mDBManager.insertInServerdelete(IdPatternUtils.formatServerId(mapValue.getServerid(), mapValue.getServerV()),
                        mapValue.getMd5(), null)
                mDBManager.deleteRowInMappingByServerID(server_id)
            }

            //删除云端后，重新设置云端数目缓存记录
            var tempCount = mApplicationPrefsManager.getCloudCurrentContactCount() - deleteMaps.size
            if (tempCount < 0) {
                tempCount = 0
            }
            mApplicationPrefsManager.setCloudCurrentContactCount(tempCount)
        }
        if (deleteMapsUpdateV != null && deleteMapsUpdateV.size > 0) {
            for (i in deleteMapsUpdateV.indices) {
                val mapValue = deleteMapsUpdateV[i] as IDMappingValue
                val server_id = mapValue.getServerid()
                //写入在云端删除表
                mDBManager.insertInServerdelete(IdPatternUtils.formatServerId(mapValue.getServerid(), mapValue.getServerLatestV()),
                        mapValue.getMd5(), null)
                mDBManager.deleteRowInMappingByServerID(server_id)
            }

            //删除云端后，重新设置云端数目缓存记录
            var tempCount = mApplicationPrefsManager.getCloudCurrentContactCount() - deleteMapsUpdateV.size
            if (tempCount < 0) {
                tempCount = 0
            }
            mApplicationPrefsManager.setCloudCurrentContactCount(tempCount)
        }

    }


    /**
     * 获取短信的最新短信时间
     */
    fun getSMSversion() {
        SMSserverList = ArrayList<SmsInfo>()
        mDataCenterModel.getSMSVersion(object : DataCenterListener.OngetSMSVersionResponListener() {
            fun getSMSVersionSuccess(latestbackupts: Long, latestcallts: Long) {
                serverSMSversion = latestbackupts
                if (mApplicationPrefsManager.getSMSlatestcall() !== serverSMSversion) {
                    getSMSRecords()
                    //                    mApplicationPrefsManager.setSMSlatestcall(serverSMSversion);
                } else {
                    compareSMSSyncWithCloud()
                }
            }

            fun getSMSVersionFailed(Code: Int, msg: String) {
                if (isViewAttached()) {
                    getView().loadDataFailed(Code, msg)
                }
            }
        })
    }


    /**
     * 从云端获取6个月的短信数据
     */
    fun getSMSRecords() {
        val startTime = DateUtils.getSMSstartTs()
        val endTime = System.currentTimeMillis()
        mDataCenterModel.getSMSRecords(startTime, endTime, object : DataCenterListener.OngetSMSRecordResponListener() {
            fun getSMSRecordSuccess(serverList: List<SmsInfo>) {
                SMSserverList = serverList
                compareSMSSyncWithCloud()
            }

            fun getSMSRecordFailed(Code: Int, msg: String) {
                if (isViewAttached()) {
                    getView().loadDataFailed(Code, msg)
                }
            }
        })
    }

    /**
     * 上传短信到云端
     */
    fun SMSupload() {
        if (!NetWorkUtil.isAvailable(mContext)) {
            sentMessageUploadSyncFailed(INSTANCE.getREQ_CODE_UNNETWORK(), null)
            return
        }
        var smsjson = ""
        if (mCacheStatisticsManager.getSmsUploadList() != null && mCacheStatisticsManager.getSmsUploadList()!!.size > 0) {
            smsjson = JsonUtils.SMSCreateUpLoadJson(mCacheStatisticsManager.getSmsUploadList())
        } else {
            if (isViewAttached()) {
                //备份到本地成功后 把服务器新增的插入到缓存id表
                mDBManager.insertSMSIDmapping(mCacheStatisticsManager.getSmsIntoMappingList())
                MainUiHandler.sendEmptyMessage(UPLOAD_SMS_SUCCESS)
                mApplicationPrefsManager.setSMSlatestcall(serverSMSversion)
            }
            if (syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_SMS() || syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_CONTACT_SMS()) {
                MainUiHandler.sendEmptyMessage(SYNC_DATA_FINISH)
            }
            //先上传完通话记录 再插入本地数据库
            if (mApplicationPrefsManager.isSyncMessages() && mApplicationPrefsManager.isSyncPhone()) {
                mCallLogRestoreModel.setRestoreList(mCacheStatisticsManager.getCallLogServerAddList())
                callLogDoRestore()
            }
            return
        }
        mDataCenterModel.uploadSMS(smsjson, object : DataCenterListener.OnuploadSMSResponListener() {
            fun uploadSMSSuccess(latestbackupts: Long) {
                mApplicationPrefsManager.setSMSlatestcall(latestbackupts)
                if (isViewAttached()) {
                    //备份到本地成功后 把服务器新增的插入到缓存id表
                    mDBManager.insertSMSIDmapping(mCacheStatisticsManager.getSmsIntoMappingList())
                    MainUiHandler.sendEmptyMessage(UPLOAD_SMS_SUCCESS)
                }
                if (syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_SMS() || syncFlag == WakaIcloudConstant.INSTANCE.getFLAG_CONTACT_SMS()) {
                    MainUiHandler.sendEmptyMessage(SYNC_DATA_FINISH)
                }
                //先上传完通话记录 再插入本地数据库
                if (mApplicationPrefsManager.isSyncMessages() && mApplicationPrefsManager.isSyncPhone()) {
                    mCallLogRestoreModel.setRestoreList(mCacheStatisticsManager.getCallLogServerAddList())
                    callLogDoRestore()

                }
            }

            fun uploadSMSFailed(Code: Int, msg: String) {
                if (isViewAttached()) {
                    sentMessageUploadSyncFailed(Code, msg)
                }
                //先上传完通话记录 再插入本地数据库
                if (mApplicationPrefsManager.isSyncMessages() && mApplicationPrefsManager.isSyncPhone()) {
                    mCallLogRestoreModel.setRestoreList(mCacheStatisticsManager.getCallLogServerAddList())
                    callLogDoRestore()

                }
            }
        })
    }

    /**
     * 获取通话记录的版本
     */
    fun getCallversion() {
        CallserverList = ArrayList<CallLogBean>()
        mDataCenterModel.getCallVersion(object : DataCenterListener.OngetCallVersionResponListener() {
            fun getCallVersionSuccess(latestbackupts: Long, latestcallts: Long) {
                serverCallversion = latestbackupts
                if (mApplicationPrefsManager.getCalllatestcall() !== serverCallversion) {
                    getCallRecords()
                    //                    mApplicationPrefsManager.setCalllatestcall(serverCallversion);
                } else {
                    compareCallLogSyncWithCloud()
                }
            }

            fun getCallVersionFailed(Code: Int, msg: String) {
                if (isViewAttached()) {
                    getView().loadDataFailed(Code, msg)
                }
            }
        })
    }


    /**
     * 从云端获取六个月的通话记录
     */
    fun getCallRecords() {
        val startTime = DateUtils.getSMSstartTs()
        val endTime = System.currentTimeMillis()
        mDataCenterModel.getCallRecords(startTime, endTime, object : DataCenterListener.OngetCallRecordResponListener() {
            fun getCallRecordSuccess(serverList: List<CallLogBean>) {
                CallserverList = serverList
                compareCallLogSyncWithCloud()
            }

            fun getCallRecordFailed(Code: Int, msg: String) {
                if (isViewAttached()) {
                    getView().loadDataFailed(Code, msg)
                }
            }
        })
    }


    /**
     * 上传通话记录到云端
     */
    fun Callupload() {
        if (!NetWorkUtil.isAvailable(mContext)) {
            sentMessageUploadSyncFailed(INSTANCE.getREQ_CODE_UNNETWORK(), null)
            return
        }
        var calljson = ""
        if (mCacheStatisticsManager.getCallLogUploadList() != null && mCacheStatisticsManager.getCallLogUploadList()!!.size > 0) {
            calljson = JsonUtils.CallLogCreateUpLoadJson(mCacheStatisticsManager.getCallLogUploadList())
        } else {
            if (isViewAttached()) {
                mDBManager.insertCallLogIDMapping(mCacheStatisticsManager.getCallIntoMappingList())
                MainUiHandler.sendEmptyMessage(UPLOAD_CALL_SUCCESS)
                MainUiHandler.sendEmptyMessage(SYNC_DATA_FINISH)

                mApplicationPrefsManager.setCalllatestcall(serverCallversion)
            }
            return
        }
        mDataCenterModel.uploadCalls(calljson, object : DataCenterListener.OnuploadCallResponListener() {
            fun uploadCallsSuccess(latestbackupts: Long) {
                mApplicationPrefsManager.setCalllatestcall(latestbackupts)
                if (isViewAttached()) {
                    mDBManager.insertCallLogIDMapping(mCacheStatisticsManager.getCallIntoMappingList())
                    MainUiHandler.sendEmptyMessage(UPLOAD_CALL_SUCCESS)
                    MainUiHandler.sendEmptyMessage(SYNC_DATA_FINISH)
                }
            }

            fun uploadCallsFailed(Code: Int, msg: String) {
                if (isViewAttached()) {
                    sentMessageUploadSyncFailed(Code, msg)
                }
            }
        })
    }


    /**
     * 点同步按钮  进行数据同步
     */
    fun synncdata() {
        syncFlag = getSyncFlag()
        LogUtils.d(TAG, "synncdata syncFlag:" + syncFlag)
        if (syncFlag < 1) {
            MainUiHandler.sendEmptyMessage(SYNC_DATA_FINISH)
            return
        }
        if (!NetWorkUtil.isAvailable(mContext)) {
            sentMessageUploadSyncFailed(INSTANCE.getREQ_CODE_UNNETWORK(), null)
            return
        }
        //先上传通讯录完 再插入本地数据库
        if (mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission()) {
            val contactRestoreModel = ContactRestoreModel(
                    getListFromServerIDlist(
                            mCacheStatisticsManager.getServerContactDataHashtable(),
                            mCacheStatisticsManager.getAddServerList()),
                    getListFromServerIDlist(mCacheStatisticsManager.getServerContactDataHashtable(),
                            mCacheStatisticsManager.getUpdateServerList()))
            contactDoRestore(contactRestoreModel)

        } else {
            syncSMSCall()
        }

    }

    private fun contactDoRestore(contactRestoreModel: ContactRestoreModel) {
        Observable.create(ObservableOnSubscribe<Any> { e -> e.onNext("ni") })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : Observer {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(o: Any) {
                        LogUtils.d("CKS", o as String)
                        contactRestoreModel.doRestore(this)
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {
                        LogUtils.d("CKS", "2")
                        uploadContact()
                    }
                })
    }

    private fun smsDoRestore() {
        Observable.create(ObservableOnSubscribe<Any> { e -> e.onNext("test") }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : Observer {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(o: Any) {
                        mSMSRestoreModel.doRestore(this)
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {
                        SMSupload()
                    }
                })
    }

    private fun callLogDoRestore() {
        Observable.create(ObservableOnSubscribe<Any> { e -> e.onNext("test") }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : Observer {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(o: Any) {
                        mCallLogRestoreModel.doRestore(this)
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {
                        Callupload()
                    }
                })
    }

    private fun syncSMSCall() {
        //先上传完短信 再插入本地数据库
        if (mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission()
                && mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission()) {
            mSMSRestoreModel.setrestoreList(mCacheStatisticsManager.getSmsServerAddList())
            smsDoRestore()
        } else {
            if (mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission()) {
                mSMSRestoreModel.setrestoreList(mCacheStatisticsManager.getSmsServerAddList())
                smsDoRestore()
            }
            //先上传完通话记录 再插入本地数据库
            if (mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission()) {
                mCallLogRestoreModel.setRestoreList(mCacheStatisticsManager.getCallLogServerAddList())
                callLogDoRestore()
            }
        }
    }


    fun updateUI() {
        if (getView() != null) {
            getView().updateStatistics(mCacheStatisticsManager.getCountOfAddLocalList(),
                    mCacheStatisticsManager.getCountOfDeleteLocalList() + mCacheStatisticsManager.getCountOfDeleteLocalListNeedUpdateV(),
                    mCacheStatisticsManager.getCountOfUpdateLocalList(),
                    mCacheStatisticsManager.getCountOfAddServerList(),
                    mCacheStatisticsManager.getCountOfDeleteServerList(),
                    mCacheStatisticsManager.getCountOfUpdateServerList(),
                    ReadPhoneContactModel.Companion.getPhoneContactCounts()
            )
        }
    }

    fun getSyncFlag(): Int {
        return if (mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission()
                && !(mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission())
                && mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission()) {
            WakaIcloudConstant.INSTANCE.getFLAG_CONTACT_SMS()
        } else if (!(mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission())
                && !(mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission())
                && mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission()) {
            WakaIcloudConstant.INSTANCE.getFLAG_SMS()
        } else if (!(mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission())
                && !(mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission())
                && !(mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission())) {
            WakaIcloudConstant.INSTANCE.getFLAG_NULL()
        } else if (mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission()
                && mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission()
                && mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission()) {
            WakaIcloudConstant.INSTANCE.getFLAG_CONTACT_SMS_CALL()
        } else if (!(mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission())
                && mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission()
                && !(mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission())) {
            WakaIcloudConstant.INSTANCE.getFLAG_CALL()
        } else if (mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission()
                && mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission()
                && !(mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission())) {
            WakaIcloudConstant.INSTANCE.getFLAG_CONTACT_CALL()
        } else if (!(mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission())
                && mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission()
                && mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission()) {
            WakaIcloudConstant.INSTANCE.getFLAG_SMS_CALL()
        } else if (mApplicationPrefsManager.isSyncContacts() && PermissionUtils.isAuthContactPermission()
                && !(mApplicationPrefsManager.isSyncPhone() && PermissionUtils.isAuthPhonePermission())
                && !(mApplicationPrefsManager.isSyncMessages() && PermissionUtils.isAuthSMSPermission())) {
            WakaIcloudConstant.INSTANCE.getFLAG_CONTACT()
        } else
            -1
    }

    fun logOut() {
        mDataCenterModel.logout(object : DataCenterListener.OnlogOutResponListener() {
            fun logOutSuccess() {
                tudcLogout()
            }

            fun logOutFailed(code: Int, msg: String) {
                if (isViewAttached()) {
                    getView().logOutFailed(code, msg)
                }
            }
        })
    }

    private fun tudcLogout() {
        TUDCSdkInnerManager.getManager().logOut(object : TudcInnerListener.OnLogoutTudcListener() {
            fun onLogoutSuccess() {
                if (isViewAttached()) {
                    getView().updateLogOutView()
                }
            }

            fun onLogoutFail(i: Int, s: String) {
                if (isViewAttached()) {
                    getView().updateLogOutView()
                }
            }
        })
        getView().updateLogOutView()
    }

    companion object {

        val TAG = MainPresenter::class.java.simpleName


        val UPLOAD_CONTACTS_SUCCESS = 1
        val UPLOAD_SMS_SUCCESS = 2
        val UPLOAD_CALL_SUCCESS = 3
        val SYNC_DATA_FINISH = 4
        val UPLOAD_SYNC_FAILED = SYNC_DATA_FINISH + 1
    }

}
