package com.threetree.contactbackup.model.contact

import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.ContactBean
import com.threetree.contactbackup.bean.ContactPhoneBean
import com.threetree.contactbackup.bean.IDMappingValue
import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager
import com.threetree.contactbackup.util.GetMD5Utils
import com.threetree.contactbackup.util.IdPatternUtils
import com.threetree.contactbackup.util.ListUtils
import com.threetree.contactbackup.util.LogUtils

import java.util.ArrayList
import java.util.HashMap



class FullDataCompareModel {

    private var localContactList: List<ContactBean>? = null
    private var serverContactList: List<ContactBean>? = null
    private var localNoMatchContactIdList: MutableList<Int>? = null
    private var serverNoMatchContactIdList: MutableList<String>? = null
    private var serverMatchContactIdList: MutableList<String>? = null
    private var localMatchContactIdList: MutableList<Int>? = null
    private var serverContactIdList: MutableList<String>? = null
    private var localContactIdList: MutableList<Int>? = null

    private var compare_type: Int = 0//是否是增量对比

    private val mCacheStatisticsManager: CacheStatisticsManager
    private val mDBManager: GreenDaoDBManager

    /*
    * 传进两个List<ContactBean>
    * 进行数据全量匹配
    * 以服务器协议上5个name字段和电话号码数组拼成的String求MD5，
    * 数据id+版本号作为key， MD5作为value
    * */
    init {
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        mDBManager = Factory.get().getDBManager()

    }

    fun doCompare(a: List<ContactBean>, b: List<ContactBean>, compare_type: Int) {
        //init
        localContactList = a
        serverContactList = b
        this.compare_type = compare_type
        LogUtils.d(TAG, "  doCompare")
        LogUtils.d(TAG, "  Compare Type:" + compare_type)

        serverContactIdList = ArrayList()
        for (contact in serverContactList!!) {
            serverContactIdList!!.add(contact.getServerId())
        }
        localContactIdList = ArrayList()
        for (contact in localContactList!!) {
            localContactIdList!!.add(contact.get_id())

        }

        val initTime = System.currentTimeMillis()
        compare(createLocalContactMap(localContactList), createServerContactMap(serverContactList))
        val doneTime = System.currentTimeMillis()
        LogUtils.d(TAG, "Local size:" + localContactList!!.size + "   " + "Sever size:" + serverContactList!!.size +
                "  Compare Time:" + (doneTime - initTime))



        LogUtils.d(TAG, "localNoMatchContactIdList size:" + localNoMatchContactIdList!!.size + "   " + "serverNoMatchContactIdList size:" + serverNoMatchContactIdList!!.size
        )
        LogUtils.d(TAG, "localNoMatchContactIdList :" + localNoMatchContactIdList!!.toString())
        LogUtils.d(TAG, "serverNoMatchContactIdList :" + serverNoMatchContactIdList!!.toString())
        LogUtils.d(TAG, "localMatchContactIdList :" + localMatchContactIdList!!.toString())
        LogUtils.d(TAG, "serverMatchContactIdList :" + serverMatchContactIdList!!.toString())
        LogUtils.d(TAG, "serverContactIdList :" + serverContactIdList!!.toString())
    }

    private fun createLocalContactMap(contactBeanList: List<ContactBean>): MutableMap<String, String> {
        val contactMap = HashMap<String, String>()
        for (contact in contactBeanList) {
            //            String phoneInfo="-";
            //            if (contact.getPhoneList()!=null){
            //                for (ContactPhoneBean phoneBean:contact.getPhoneList()){
            //                    phoneInfo = phoneInfo+phoneBean.getNumber();
            //                }
            //            }
            //            String namePart = "-";
            //            if (contact.getName() != null){
            //                namePart = namePart+contact.getName().getGiven_name()
            //                        +contact.getName().getFamily_name()
            //                        +contact.getName().getPrefix()
            //                        +contact.getName().getMiddle_name()
            //                        +contact.getName().getSuffix()
            //                        +contact.getName().getDisplay_name();
            //            }
            //            contactMap.put(
            //                    IdPatternUtils.formatLocalId(contact.get_id(),Integer.parseInt(contact.getLocal_version())),
            //                    GetMD5Utils.getMD5(namePart+phoneInfo));
            contactMap.put(
                    IdPatternUtils.formatLocalId(contact.get_id(), Integer.parseInt(contact.getLocal_version())),
                    contact.getMd5())
        }
        LogUtils.d(TAG, "createLocalContactMap  param size:" + contactBeanList.size)
        LogUtils.d(TAG, "createLocalContactMap  result size:" + contactMap.size)
        return contactMap
    }

    private fun createServerContactMap(contactBeanList: List<ContactBean>): MutableMap<String, String> {
        val contactMap = HashMap<String, String>()
        for (contact in contactBeanList) {
            //            String phoneInfo="-";
            //            if (contact.getPhoneList()!=null){
            //                for (ContactPhoneBean phoneBean:contact.getPhoneList()){
            //                    phoneInfo = phoneInfo+phoneBean.getNumber();
            //                }
            //            }
            //            String namePart = "-";
            //            if (contact.getName() != null){
            //                namePart = namePart+contact.getName().getGiven_name()
            //                        +contact.getName().getFamily_name()
            //                        +contact.getName().getPrefix()
            //                        +contact.getName().getMiddle_name()
            //                        +contact.getName().getSuffix()
            //                        +contact.getName().getDisplay_name();
            //            }
            //            contactMap.put(
            //                    IdPatternUtils.formatServerId(contact.getServerId(),Integer.parseInt(contact.getServer_version())),
            //                    GetMD5Utils.getMD5(namePart+phoneInfo));
            contactMap.put(
                    IdPatternUtils.formatServerId(contact.getServerId(), Integer.parseInt(contact.getServer_version())),
                    contact.getMd5())
        }
        LogUtils.d(TAG, "createServerContactMap  param size:" + contactBeanList.size)
        LogUtils.d(TAG, "createServerContactMap  result size:" + contactMap.size)
        return contactMap
    }

    private fun compare(LocalContactMap: MutableMap<String, String>, ServerContactMap: MutableMap<String, String>) {
        localNoMatchContactIdList = ArrayList()
        serverNoMatchContactIdList = ArrayList()
        serverMatchContactIdList = ArrayList()
        localMatchContactIdList = ArrayList()

        if (LocalContactMap.size == 0) {
            for ((serverid_integrate) in ServerContactMap) {
                val serverid = IdPatternUtils.getIdByParseServerId(serverid_integrate)
                serverNoMatchContactIdList!!.add(serverid)

            }
            setResult()
            return
        }

        if (ServerContactMap.size == 0) {
            for ((localid_integrate) in LocalContactMap) {
                val localid = IdPatternUtils.getIdByParseLocalId(localid_integrate)
                localNoMatchContactIdList!!.add(localid)

            }
            setResult()
            return
        }

        val tempMatchLocal = ArrayList<String>()

        val mappingAllCompare = ArrayList<IDMappingValue>()
        val mappingByIncrementalCompare = ArrayList<IDMappingValue>()

        //外层循环
        out@ for ((localid_integrate, Lmd5) in LocalContactMap) {
            for ((serverid_integrate, Smd5) in ServerContactMap) {
                if (Lmd5 == Smd5) {
                    val localid = IdPatternUtils.getIdByParseLocalId(localid_integrate)
                    val localversion = IdPatternUtils.getVersionByParseLocalId(localid_integrate)
                    val serverid = IdPatternUtils.getIdByParseServerId(serverid_integrate)
                    val serverversion = IdPatternUtils.getVersionByParseServerId(serverid_integrate)
                    LogUtils.d(TAG, "Compared Local id:" + localid + "   local version:" + localversion + "    Sever id:" + serverid
                            + "    server version:" + serverversion)

                    val mappingValue = IDMappingValue(localid, localversion, serverid, serverversion, Lmd5)
                    when (compare_type) {
                        ALL_COMPARE -> mappingAllCompare.add(mappingValue)
                        INCREMENTAL_COMPARE -> mappingByIncrementalCompare.add(mappingValue)
                        INCREMENTAL_DELETETABLE_COMPARE -> {
                        }
                    }//对比类型为与云端删除数据对比，不用缓存插入映射表
                    //对比类型为与云端删除数据对比，不用插入映射表
                    //                    if (compare_type != INCREMENTAL_DELETETABLE_COMPARE){
                    //                        Factory.get().getDBManager().insertInMapping(localid,localversion,serverid,serverversion,Lmd5);
                    //                    }
                    serverMatchContactIdList!!.add(serverid)
                    localMatchContactIdList!!.add(localid)

                    ServerContactMap.remove(serverid_integrate)
                    tempMatchLocal.add(localid_integrate)

                    continue@out
                }

            }
        }

        when (compare_type) {
            ALL_COMPARE -> mCacheStatisticsManager.setMappingByAllCompare(mappingAllCompare)
            INCREMENTAL_COMPARE -> mCacheStatisticsManager.setMappingByIncrementalCompare(mappingByIncrementalCompare)
            INCREMENTAL_DELETETABLE_COMPARE -> {
            }
        }

        for (localid in tempMatchLocal) {
            LocalContactMap.remove(localid)
        }

        for ((localid_integrate) in LocalContactMap) {
            val localid = IdPatternUtils.getIdByParseLocalId(localid_integrate)
            localNoMatchContactIdList!!.add(localid)
        }

        for ((serverid_integrate) in ServerContactMap) {
            val serverid = IdPatternUtils.getIdByParseServerId(serverid_integrate)
            serverNoMatchContactIdList!!.add(serverid)
        }


        setResult()


    }

    private fun setResult() {
        when (compare_type) {
            ALL_COMPARE -> {
                mCacheStatisticsManager.setAddLocalList(localNoMatchContactIdList)
                mCacheStatisticsManager.setAddServerList(serverNoMatchContactIdList)
                LogUtils.d(TAG, "ALL_COMPARE  setAddLocalList size:" + mCacheStatisticsManager.getAddLocalList().size)
                LogUtils.d(TAG, "ALL_COMPARE  setAddServerList size:" + mCacheStatisticsManager.getAddServerList().size)
            }
            INCREMENTAL_COMPARE -> {
                mCacheStatisticsManager.setAddLocalList(
                        ListUtils.getDifferentListInTwoLists(mCacheStatisticsManager.getAddLocalList(), localMatchContactIdList))
                mCacheStatisticsManager.setAddServerList(
                        ListUtils.getDifferentStringListInTwoLists(mCacheStatisticsManager.getAddServerList(), serverMatchContactIdList))
                LogUtils.d(TAG, "INCREMENTAL_COMPARE  setAddLocalList size:" + mCacheStatisticsManager.getAddLocalList().size)
                LogUtils.d(TAG, "INCREMENTAL_COMPARE  setAddServerList size:" + mCacheStatisticsManager.getAddServerList().size)
            }
            INCREMENTAL_DELETETABLE_COMPARE -> {
                //本地新增与云端删除缓存的对比，排除在云端删除缓存中有的（即伪新增），剩下的即本地新增
                mCacheStatisticsManager.setAddLocalList(
                        ListUtils.getDifferentListInTwoLists(mCacheStatisticsManager.getAddLocalList(), localMatchContactIdList))
                mCacheStatisticsManager.setAddLocalConflictDeleteServerList(serverMatchContactIdList)
                LogUtils.d(TAG, "INCREMENTAL_DELETETABLE_COMPARE  setAddLocalList size:" + mCacheStatisticsManager.getAddLocalList().size)
                LogUtils.d(TAG, "INCREMENTAL_DELETETABLE_COMPARE  setAddLocalConflictDeleteServerList size:" + serverMatchContactIdList!!.size)

                //本地删除与云端删除缓存对比，排除在云端删除缓存中有的（即伪删除），剩下的即本地需提交的删除列表
                val deleteLocal = mCacheStatisticsManager.getDeleteLocalList()
                if (deleteLocal != null && deleteLocal!!.size > 0) {

                    val deleteLocalChangeToServer = mDBManager.getServerIdListWithoutVersionByLocalId(deleteLocal)
                    val hasInDelete = ArrayList<String>()

                    for (i in deleteLocalChangeToServer.indices) {
                        if (serverContactIdList!!.contains(deleteLocalChangeToServer.get(i))) {
                            hasInDelete.add(deleteLocalChangeToServer.get(i))
                        }
                    }
                    if (hasInDelete != null && hasInDelete.size > 0) {
                        val finalDeleteLocalChangeToserver = ListUtils.getDifferentStringListInTwoLists(deleteLocalChangeToServer, hasInDelete)

                        val finalDeleteLocal = mDBManager.getMappingLocalIdListByServerId(finalDeleteLocalChangeToserver)
                        mCacheStatisticsManager.setDeleteLocalList(finalDeleteLocal)
                        LogUtils.d(TAG, "INCREMENTAL_DELETETABLE_COMPARE  setDeleteLocalList finalDeleteLocal size:" + finalDeleteLocal.size)
                        for (i in hasInDelete.indices) {
                            mDBManager.deleteRowInMappingByServerID(hasInDelete[i])
                        }

                    }
                }

                //本地修改与云端删除缓存对比，排除在云端删除缓存中有的，剩下的即本地需提交的修改列表
                val updateLocal = mCacheStatisticsManager.getUpdateLocalList()
                if (updateLocal != null && updateLocal!!.size > 0) {

                    val updateLocalChangeToServer = mDBManager.getServerIdListWithoutVersionByLocalId(updateLocal)
                    val hasInDelete = ArrayList<String>()

                    for (i in updateLocalChangeToServer.indices) {
                        if (serverContactIdList!!.contains(updateLocalChangeToServer.get(i))) {
                            hasInDelete.add(updateLocalChangeToServer.get(i))
                        }
                    }
                    if (hasInDelete != null && hasInDelete.size > 0) {
                        val finalUpdateLocalChangeToserver = ListUtils.getDifferentStringListInTwoLists(updateLocalChangeToServer, hasInDelete)

                        val finalUpdateLocal = mDBManager.getMappingLocalIdListByServerId(finalUpdateLocalChangeToserver)
                        mCacheStatisticsManager.setUpdateLocalList(finalUpdateLocal)
                        LogUtils.d(TAG, "INCREMENTAL_DELETETABLE_COMPARE  setUpdateLocalList setUpdateLocalList size:" + finalUpdateLocal.size)
                        //                        for (int i = 0; i < hasInDelete.size(); i++) {
                        //                            mDBManager.deleteRowInMappingByServerID(hasInDelete.get(i));
                        //                        }

                    }
                }
            }
        }
    }

    companion object {

        val TAG = FullDataCompareModel::class.java.simpleName
        val ALL_COMPARE = 0//全量对比 (本地全量数据与云端非删除态全量数据对比)
        val INCREMENTAL_COMPARE = 1//增量对比 （本地新增数据与云端新增数据对比，由映射表得出两端新增数据）
        val INCREMENTAL_DELETETABLE_COMPARE = 2//本地新增与云端删除表对比
    }
}
