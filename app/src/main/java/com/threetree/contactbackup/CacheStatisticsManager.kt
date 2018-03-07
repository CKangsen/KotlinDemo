package com.threetree.contactbackup


import com.threetree.contactbackup.bean.CallLogBean
import com.threetree.contactbackup.bean.ContactBean
import com.threetree.contactbackup.bean.IDMappingValue
import com.threetree.contactbackup.bean.SmsInfo
import com.threetree.contactbackup.util.LogUtils

import java.util.Hashtable



class CacheStatisticsManager {

    /**
     * 联系人
     */
    /*本地新增 id List*/
    var addLocalList: List<Int>? = null
    /*本地删除 id List*/
    var deleteLocalList: List<Int>? = null
    /*本地修改 id List*/
    var updateLocalList: List<Int>? = null

    /*云端新增 id List*/
    var addServerList: List<String>? = null
    /*云端删除 id List*/
    var deleteServerList: List<String>? = null
    /*云端修改 id List*/
    var updateServerList: List<String>? = null
    /*云端恢复 恢复标记删除的联系人 id List*/
    var rollbackServerList: List<String>? = null
    /*云端下拉的联系人数据集合*/
    var serverContactList: List<ContactBean>? = null
    /*云端下拉的联系人数据HashTable集合  K：serverid,V：ContactBean*/
    var serverContactDataHashtable: Hashtable<String, ContactBean>? = null
    /*云端下拉的云端标记删除的联系人数据集合*/
    var serverDeleteContactList: List<ContactBean>? = null

    /*本地删除 （云端修改冲突） server id List*/
    var deleteLocalNeedUpdateVList: List<String>? = null

    /*本地冲突修改 id List*/
    var conflictUpdateLocalList: List<Int>? = null
    /*云端冲突修改 id List*/
    var conflictUpdateServerList: List<String>? = null

    /*本地新增与云端删除表冲突 云端删除表的id List*/
    var addLocalConflictDeleteServerList: List<String>? = null

    /*本地现有的联系人数目*/
    var local_current_count: Int = 0
        set(local_current_count) {
            field = local_current_count
            LogUtils.d(TAG, " setLocal_current_count:" + local_current_count)
        }
    /*云端现有的联系人数目*/
    var icloud_current_count: Int = 0

    /*缓存联系人的大版本号*/
    var server_big_version: Int = 0

    /**
     * 通话记录
     */
    /*
    通话记录服务器新增的、要备份到本地的
     */
    var callLogServerAddList: List<CallLogBean>? = null


    /**
     * 通话记录服务器有 对比表没有的  最后插入到对比表的数据
     */
    var callIntoMappingList: MutableList<CallLogBean>? = null

    /*
      本地新增的、要上传到服务器的
    */
    var callLogUploadList: List<CallLogBean>? = null
        get() = if (field != null)
            field
        else
            null


    /**
     * 短息
     */
    /*
    短信服务器新增的、要备份到本地的、成功要插入中间表
     */
    var smsServerAddList: List<SmsInfo>? = null
        get() = if (field != null)
            field
        else
            null


    /**
     * 短信记录服务器有 对比表没有的  最后插入到对比表的数据
     */
    private var smsIntoMappingList: MutableList<SmsInfo>? = null

    /*
      本地新增的、要上传到服务器的
    */
    private var smsUploadList: List<SmsInfo>? = null

    /**
     * 上传新增联系人的本地IDMappingValue(local id+v,md5) 的集合缓存，与返回的服务器id+version匹配
     * 用于更新映射表
     */
    var localMapValueForAddContact: List<IDMappingValue>? = null
    /**
     * 上传修改联系人的本地IDMappingValue(包含全属性) 的集合缓存
     * 用于更新映射表
     */
    var localMapValueForUpdateContact: List<IDMappingValue>? = null
    /**
     * 上传删除联系人的IDMappingValue(包含server id+v,md5) 的集合缓存
     * 用于删除映射表
     */
    var localMapValueForDeleteContact: List<IDMappingValue>? = null
    /**
     * 上传删除联系人的IDMappingValue(包含server id+v,md5,serverLatestV) 的集合缓存
     * 用于删除映射表
     */
    var localMapValueForDeleteContactNeedUpdateV: List<IDMappingValue>? = null


    /**
     * 全数据匹配时缓存的映射表关系
     * 用于插入映射表
     */
    var mappingByAllCompare: List<IDMappingValue>? = null
    /**
     * 本地新增与云端新增 全数据匹配时缓存的映射表关系
     * 用于插入映射表
     */
    var mappingByIncrementalCompare: List<IDMappingValue>? = null

    /*TUDC请求的权限，未授权部分*/
    var tudcRequestPermissions: Array<String>? = null

    val countOfAddLocalList: Int
        get() = if (addLocalList != null) {
            addLocalList!!.size
        } else {
            0
        }

    val countOfDeleteLocalList: Int
        get() = if (deleteLocalList != null) {
            deleteLocalList!!.size
        } else {
            0
        }

    val countOfDeleteLocalListNeedUpdateV: Int
        get() = if (deleteLocalNeedUpdateVList != null) {
            deleteLocalNeedUpdateVList!!.size
        } else {
            0
        }

    val countOfUpdateLocalList: Int
        get() = if (updateLocalList != null) {
            updateLocalList!!.size
        } else {
            0
        }

    val countOfAddServerList: Int
        get() = if (addServerList != null) {
            addServerList!!.size
        } else {
            0
        }

    val countOfDeleteServerList: Int
        get() = if (deleteServerList != null) {
            deleteServerList!!.size
        } else {
            0
        }

    val countOfUpdateServerList: Int
        get() = if (updateServerList != null) {
            updateServerList!!.size
        } else {
            0
        }

    fun clearCompareStatistics() {
        addLocalList = null
        deleteLocalList = null
        updateLocalList = null

        addServerList = null
        deleteServerList = null
        updateServerList = null
        rollbackServerList = null

        conflictUpdateLocalList = null
        conflictUpdateServerList = null
        localMapValueForAddContact = null
        localMapValueForUpdateContact = null
        localMapValueForDeleteContact = null
        deleteLocalNeedUpdateVList = null

        mappingByAllCompare = null
        mappingByIncrementalCompare = null

        smsUploadList = null
        smsIntoMappingList = null
        this.smsServerAddList = null

        callLogServerAddList = null
        callIntoMappingList = null
        this.callLogUploadList = null
    }

    fun clearContactCompareStatistics() {
        addLocalList = null
        deleteLocalList = null
        updateLocalList = null

        addServerList = null
        deleteServerList = null
        updateServerList = null
        rollbackServerList = null

        conflictUpdateLocalList = null
        conflictUpdateServerList = null
        localMapValueForAddContact = null
        localMapValueForUpdateContact = null
        localMapValueForDeleteContact = null
        deleteLocalNeedUpdateVList = null

        mappingByAllCompare = null
        mappingByIncrementalCompare = null
    }

    fun clearSMSCompareStatistics() {
        smsUploadList = null
        smsIntoMappingList = null
        this.smsServerAddList = null
    }

    fun clearCallLogCompareStatistics() {
        callLogServerAddList = null
        callIntoMappingList = null
        this.callLogUploadList = null
    }

    fun setSmsuploadList(uploadList: List<SmsInfo>) {
        this.smsUploadList = uploadList
    }

    fun getSmsUploadList(): List<SmsInfo>? {
        return if (smsUploadList != null)
            this.smsUploadList
        else
            null
    }

    fun setsmsIntoMappingList(smsIntoMappingList: MutableList<SmsInfo>) {
        this.smsIntoMappingList = smsIntoMappingList
    }

    fun addsmsIntoMappingList(smsIntoMappingList: MutableList<SmsInfo>) {
        if (this.smsIntoMappingList != null) {
            this.smsIntoMappingList!!.addAll(smsIntoMappingList)
        } else
            this.smsIntoMappingList = smsIntoMappingList
    }

    fun getSmsIntoMappingList(): List<SmsInfo>? {
        return if (smsIntoMappingList != null)
            smsIntoMappingList
        else
            null
    }

    fun setCallIntoMappingList(smsIntoMappingList: MutableList<CallLogBean>) {
        this.callIntoMappingList = smsIntoMappingList
    }


    fun addCallIntoMappingList(smsIntoMappingList: MutableList<CallLogBean>?) {
        if (this.callIntoMappingList != null) {
            this.callIntoMappingList!!.addAll(smsIntoMappingList!!)
        } else
            this.callIntoMappingList = smsIntoMappingList
    }

    fun getCallIntoMappingList(): List<CallLogBean>? {
        return if (callIntoMappingList != null)
            callIntoMappingList
        else
            null
    }

    companion object {

        val TAG = CacheStatisticsManager::class.java.simpleName

        private var instance: CacheStatisticsManager? = null

        fun getInstance(): CacheStatisticsManager {
            if (instance == null) {
                instance = CacheStatisticsManager()
            }
            return instance
        }
    }
}
