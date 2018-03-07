package com.threetree.contactbackup.db

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.OperationApplicationException
import android.database.Cursor
import android.database.sqlite.SQLiteTransactionListener
import android.net.Uri
import android.os.RemoteException
import android.provider.CallLog
import android.provider.ContactsContract
import android.text.TextUtils

import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.CallLogBean
import com.threetree.contactbackup.bean.ContactBean
import com.threetree.contactbackup.bean.ContactEmailBean
import com.threetree.contactbackup.bean.ContactImBean
import com.threetree.contactbackup.bean.ContactNameBean
import com.threetree.contactbackup.bean.ContactNickNameBean
import com.threetree.contactbackup.bean.ContactOrganizationBean
import com.threetree.contactbackup.bean.ContactPhoneBean
import com.threetree.contactbackup.bean.ContactPostalAddressBean
import com.threetree.contactbackup.bean.ContactSipAddressBean
import com.threetree.contactbackup.bean.ContactWebsiteBean
import com.threetree.contactbackup.bean.IDMappingValue
import com.threetree.contactbackup.bean.SmsInfo
import com.threetree.contactbackup.util.GetMD5Utils
import com.threetree.contactbackup.util.IdPatternUtils
import com.threetree.contactbackup.util.JsonUtils
import com.threetree.contactbackup.util.LogUtils

import java.util.ArrayList
import java.util.Arrays
import java.util.Hashtable

import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import com.threetree.contactbackup.ApplicationPrefsManager.OPEN_ID

/**
 * Created by pradmin on 2017/7/14.
 */

class DBManager internal constructor(internal var mContext: Context) {

    private val mDBWrapper: DBWrapper
    private val mContentResolver: ContentResolver
    internal var vOpenid: String


    /**
     * 取出映射表中本地部分id
     *
     * @return Hashtable<Integer></Integer>,Integer> K：contact_id V:version
     */
    //long initTime = System.currentTimeMillis();
    val mappingLocalPart: Hashtable<Int, Int>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val hashtable = Hashtable<Int, Int>()
            val localCursor = queryAllInMapping()
            while (localCursor.moveToNext()) {
                hashtable.put(localCursor.getInt(IDMappingDB.CONTACT_ID_INDEX),
                        localCursor.getInt(IDMappingDB.LOCAL_VERSION_INDEX))
            }
            LogUtils.d("DBManager", " getMappingLocalPart  Hashtable size:" + hashtable.size)
            return hashtable
        }


    //获取中间表的数量 判断是否初次使用
    val mappingcount: Int
        get() {
            val result: Int
            val localCursor = queryAllInMapping()
            result = localCursor.count
            return result
        }

    /**
     * 取出映射表中云端部分id
     *
     * @return Hashtable<Integer></Integer>,Integer> K：server_id V:version
     */
    val mappingServerPart: Hashtable<String, Int>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val hashtable = Hashtable<String, Int>()
            val localCursor = queryAllInMapping()
            while (localCursor.moveToNext()) {
                hashtable.put(localCursor.getString(IDMappingDB.SERVER_INDEX),
                        localCursor.getInt(IDMappingDB.SERVER_VERSION_INDEX))
            }
            LogUtils.d("DBManager", " getMappingServerPart  Hashtable size:" + hashtable.size)
            return hashtable
        }


    /**
     * 当前账号是否存在映射
     *
     */
    val isExistContactMapping: Boolean
        get() {
            val count = queryAllInMapping().count
            return if (count > 0) {
                true
            } else {
                false
            }
        }

    val hashServerDeleteContactBeans: Hashtable<String, ContactBean>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val mHashtable = Hashtable()
            val query = mDBWrapper.query(ServerDeleteDB.SERVER_DELETE_TABLE_NAME, arrayOf<String>(ServerDeleteDB.SEVER_ID, ServerDeleteDB.CONTACT_DATA), OPEN_ID + "= ?", arrayOf(vOpenid + ""), null, null, null)
            while (query.moveToNext()) {
                mHashtable.put(query.getString(0),
                        JsonUtils.JsonToContactBean(query.getString(1)))
            }
            return mHashtable
        }

    val listServerDeleteContactBeans: List<ContactBean>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val mList = ArrayList<ContactBean>()
            val query = mDBWrapper.query(ServerDeleteDB.SERVER_DELETE_TABLE_NAME, arrayOf<String>(ServerDeleteDB.CONTACT_DATA), OPEN_ID + "= ?", arrayOf(vOpenid + ""), null, null, null)
            while (query.moveToNext()) {
                mList.add(JsonUtils.JsonToContactBean(query.getString(query.getColumnIndex(ServerDeleteDB.CONTACT_DATA))))
            }
            return mList
        }

    val listServerDeleteContactBeansOnlyIdAndMd5: List<ContactBean>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val mList = ArrayList<ContactBean>()
            val query = mDBWrapper.query(ServerDeleteDB.SERVER_DELETE_TABLE_NAME, arrayOf<String>(ServerDeleteDB.SEVER_ID, ServerDeleteDB.MD5), OPEN_ID + "= ?", arrayOf(vOpenid + ""), null, null, null)
            while (query.moveToNext()) {
                val serverid_integrated = query.getString(0)
                val contactBean = ContactBean()
                contactBean.setServerId(IdPatternUtils.getIdByParseServerId(serverid_integrated))
                contactBean.setServer_version(IdPatternUtils.getVersionByParseServerId(serverid_integrated).toString())
                contactBean.setMd5(query.getString(1))
                mList.add(contactBean)
            }
            return mList
        }


    private
            /**
             * 获取设备上所有的账户信息
             */
    val isZXtype: Boolean
        get() {
            val result = false
            val am = AccountManager.get(mContext)
            val accounts = am.accounts
            for (acct in accounts) {
                if (acct.type == "sprd.com.android.account.phone" && acct.name == "Phone") {
                    return true
                }
            }
            return result
        }

    val smSidmappingList: Hashtable<String, String>
        get() {
            val hashtable = Hashtable<String, String>()
            val localCursor = querySMSInMapping()
            while (localCursor.moveToNext()) {
                hashtable.put(localCursor.getString(1),
                        localCursor.getString(1))
            }
            return hashtable
        }

    /**
     * @return Hashtable<String></String>,String> K : callLog id ，V : md5
     */
    val callLogIdMappingList: Hashtable<String, String>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val hashtable = Hashtable<String, String>()
            val localCursor = queryCallLogInMapping()
            while (localCursor.moveToNext()) {
                hashtable.put(localCursor.getString(2),
                        localCursor.getString(2))
            }
            return hashtable
        }

    val updateConflictContactIds: List<Int>
        get() {
            LogUtils.d(TAG, " getUpdateConflictContactIds ")
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val conflictContactIds = ArrayList<Int>()
            val query = mDBWrapper.query(LocalUpdateConflictContactDB.LOCAL_UPDATE_CONFLICT_TABLE_NAME,
                    arrayOf<String>(LocalUpdateConflictContactDB.CONTACT_ID), OPEN_ID + "= ?", arrayOf(vOpenid + ""), null, null, null)
            while (query.moveToNext()) {
                val id = query.getInt(0)
                conflictContactIds.add(id)
            }
            return conflictContactIds
        }

    init {
        mDBWrapper = DBHelper(mContext).getDatabase()
        mContentResolver = mContext.contentResolver
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(mContext).getOpenid())
    }

    /**
     * 通过localids 获取映射表中云端部分id
     *
     * @return List<String> serverids
     * @params localids
    </String> */
    fun getMappingServerIdListByLocalId(localids: List<Int>?): List<String> {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val serverIdList = ArrayList<String>()
        if (localids == null || localids.size == 0) {
            return serverIdList
        }

        val cursor = queryRowsInMappingByLocalIDs(localids)
        while (cursor.moveToNext()) {
            serverIdList.add(cursor.getString(IDMappingDB.SERVER_INDEX))
        }

        return serverIdList
    }

    fun getServerIdListByLocalId(localids: List<Int>?): List<String> {
        val serverIdList = ArrayList<String>()
        if (localids == null || localids.size == 0) {
            return serverIdList
        }

        val cursor = queryRowsInMappingByLocalIDs(localids)
        while (cursor.moveToNext()) {
            serverIdList.add(IdPatternUtils.formatServerId(cursor.getString(IDMappingDB.SERVER_INDEX),
                    cursor.getInt(IDMappingDB.SERVER_VERSION_INDEX)))
        }

        return serverIdList
    }

    fun getIDMappingByLocalId(localids: List<Int>?): List<IDMappingValue> {
        val mappingValues = ArrayList<IDMappingValue>()
        if (localids == null || localids.size == 0) {
            return mappingValues
        }

        val cursor = queryRowsInMappingByLocalIDs(localids)
        while (cursor.moveToNext()) {
            val value = IDMappingValue(cursor.getInt(IDMappingDB.ID_INDEX),
                    cursor.getInt(IDMappingDB.LOCAL_VERSION_INDEX),
                    cursor.getString(IDMappingDB.SERVER_INDEX),
                    cursor.getInt(IDMappingDB.SERVER_VERSION_INDEX),
                    cursor.getString(IDMappingDB.MD5_INDEX))
            mappingValues.add(value)
        }

        return mappingValues
    }

    fun getIDMappingByServerId(serverids: List<String>?): List<IDMappingValue> {
        val mappingValues = ArrayList<IDMappingValue>()
        if (serverids == null || serverids.size == 0) {
            return mappingValues
        }

        val cursor = queryRowsInMappingByServerIDs(serverids)
        while (cursor.moveToNext()) {
            val value = IDMappingValue(cursor.getInt(IDMappingDB.ID_INDEX),
                    cursor.getInt(IDMappingDB.LOCAL_VERSION_INDEX),
                    cursor.getString(IDMappingDB.SERVER_INDEX),
                    cursor.getInt(IDMappingDB.SERVER_VERSION_INDEX),
                    cursor.getString(IDMappingDB.MD5_INDEX),
                    cursor.getInt(IDMappingDB.SERVER_LATEST_VERSION_INDEX))
            mappingValues.add(value)
        }

        return mappingValues
    }

    fun getServerIdListWithoutVersionByLocalId(localids: List<Int>?): List<String> {
        val serverIdList = ArrayList<String>()
        if (localids == null || localids.size == 0) {
            return serverIdList
        }

        val cursor = queryRowsInMappingByLocalIDs(localids)
        while (cursor.moveToNext()) {
            serverIdList.add(cursor.getString(IDMappingDB.SERVER_INDEX))
        }

        LogUtils.d("DBManager", " getServerIdListWithoutVersionByLocalId  serverIdList size:" + serverIdList.size)
        return serverIdList
    }

    /**
     * serverids 获取映射表中云端部分id
     *
     * @return List<Integer> localids
     * @params serverids
    </Integer> */
    fun getMappingLocalIdListByServerId(serverids: List<String>?): List<Int> {
        val initTime = System.currentTimeMillis()
        val localIdList = ArrayList<Int>()
        if (serverids == null || serverids.size == 0) {
            return localIdList
        }
        val cursor = queryRowsInMappingByServerIDs(serverids)
        while (cursor.moveToNext()) {
            localIdList.add(cursor.getInt(IDMappingDB.CONTACT_ID_INDEX))
        }
        LogUtils.d("DBManager", " getMappingServerIdListByLocalId  Hashtable Time:" + (System.currentTimeMillis() - initTime))
        return localIdList
    }

    fun queryAllInMapping(): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        LogUtils.d("DBManager", "queryAllInMapping  vOpenid: " + vOpenid)
        return mDBWrapper.query(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, null, OPEN_ID + "= ?", arrayOf(vOpenid + ""), null, null, null)
    }


    fun queryRowInMappingByLocalID(local_id: Int): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return mDBWrapper.query(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, null, IDMappingDB.CONTACT_ID + "= ? and " + OPEN_ID + "= ?", arrayOf(local_id.toString() + "", vOpenid + ""), null, null, null)
    }

    fun queryRowInMappingByServerID(server_id: String): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return mDBWrapper.query(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, null, IDMappingDB.SERVER_ID + "= ? and " + OPEN_ID + "= ?", arrayOf(server_id + "", vOpenid + ""), null, null, null)
    }

    fun queryRowsInMappingByLocalIDs(localids: List<Int>): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        var selectionArgs = "("
        for (id in localids) {
            selectionArgs = selectionArgs + id + ","
        }
        selectionArgs = selectionArgs.substring(0, selectionArgs.length - 1)
        selectionArgs = selectionArgs + ")"

        return mDBWrapper.query(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, null, IDMappingDB.CONTACT_ID + " in " + selectionArgs + " and " + OPEN_ID + "= ?", arrayOf(vOpenid + ""), null, null, null)
    }

    fun queryRowsInMappingByServerIDs(serverids: List<String>): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        var selectionArgs = "( "
        for (id in serverids) {
            selectionArgs = "$selectionArgs'$id',"
        }
        selectionArgs = selectionArgs.substring(0, selectionArgs.length - 1)
        selectionArgs = selectionArgs + " )"

        return mDBWrapper.query(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, null, IDMappingDB.SERVER_ID + " in " + selectionArgs + " and " + OPEN_ID + "= ?", arrayOf(vOpenid + ""), null, null, null)
    }


    fun queryRowInServerdeleteByServerID(server_id: String): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return mDBWrapper.query(ServerDeleteDB.SERVER_DELETE_TABLE_NAME, null, ServerDeleteDB.SEVER_ID + "= ?" + " and " + OPEN_ID + "= ?", arrayOf(server_id + "", vOpenid + ""), null, null, null)
    }


    /**
     * 插入服务器删除表
     * @param id
     * @param md5
     * @param data
     * @return
     */
    fun insertInServerdelete(id: String, md5: String, data: String): Long {

        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())

        val cv = ContentValues()
        cv.put(ServerDeleteDB.SEVER_ID, id)
        cv.put(ServerDeleteDB.MD5, md5)
        cv.put(ServerDeleteDB.CONTACT_DATA, data)
        cv.put(OPEN_ID, vOpenid)
        var row: Long = -1
        row = mDBWrapper.insertWithOnConflict(ServerDeleteDB.SERVER_DELETE_TABLE_NAME, null, cv, CONFLICT_IGNORE)
        LogUtils.d("DBManager", "insertInServerdelete row: $row   server_id: $id")

        return row
    }

    /**
     * 批量插入服务器删除表
     *
     * @param server_deleteList
     * @return
     */
    fun batchInsertInServerdelete(server_deleteList: List<ContactBean>?) {

        if (server_deleteList == null && server_deleteList!!.size <= 0) {
            return
        }

        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())

        val count = server_deleteList.size
        mDBWrapper.beginTransactionWithListener(object : SQLiteTransactionListener {
            override fun onBegin() {
                LogUtils.d("DBManager", "batchInsertInServerdelete onBegin")
            }

            override fun onCommit() {
                LogUtils.d("DBManager", "batchInsertInServerdelete onCommit Count:" + count)
            }

            override fun onRollback() {
                LogUtils.d("DBManager", "batchInsertInServerdelete onRollback")
            }
        })
        for (contactBean in server_deleteList) {
            val cv = ContentValues()
            cv.put(ServerDeleteDB.SEVER_ID, IdPatternUtils.formatServerId(contactBean.getServerId(), Integer.parseInt(contactBean.getServer_version())))
            cv.put(ServerDeleteDB.MD5, contactBean.getMd5())
            cv.put(OPEN_ID, vOpenid)
            var row: Long = -1
            row = mDBWrapper.insertWithOnConflict(ServerDeleteDB.SERVER_DELETE_TABLE_NAME, null, cv, CONFLICT_IGNORE)
            //LogUtils.d("DBManager", "insertInServerdelete row: " + row + "   server_id: " + contactBean.getServerId());
        }
        mDBWrapper.endTransaction()

    }

    fun getServerDeleteContactBeanByid(server_id: String): ContactBean {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        var mContactBean = ContactBean()
        val query = mDBWrapper.query(ServerDeleteDB.SERVER_DELETE_TABLE_NAME, arrayOf<String>(ServerDeleteDB.CONTACT_DATA), ServerDeleteDB.SEVER_ID + "= ? and " + OPEN_ID + "= ?", arrayOf(server_id + "", vOpenid + ""), null, null, null)
        if (query.moveToNext()) {
            mContactBean = JsonUtils.JsonToContactBean(query.getString(query.getColumnIndex(ServerDeleteDB.CONTACT_DATA)))
        }
        return mContactBean
    }

    fun insertInMapping(contact_id: Int, local_version: Int, server_id: String, server_version: Int, md5: String): Long {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val cv = ContentValues()
        cv.put(IDMappingDB.CONTACT_ID, contact_id)
        cv.put(IDMappingDB.LOCAL_VERSION, local_version)
        cv.put(IDMappingDB.SERVER_ID, server_id)
        cv.put(IDMappingDB.SERVER_VERSION, server_version)
        cv.put(IDMappingDB.MD5, md5)
        cv.put(OPEN_ID, vOpenid)
        var row: Long = -1

        row = mDBWrapper.insertWithOnConflict(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, null, cv, CONFLICT_IGNORE)
        LogUtils.d("DBManager", "insertInMapping : " + row + "  contact_id: " + contact_id + "  server_id: " + server_id
                + "  md5: " + md5 + "  openid:" + vOpenid)

        return row
    }

    fun deleteRowInMappingByLocalID(local_id: Int): Long {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val row = mDBWrapper.delete(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, IDMappingDB.CONTACT_ID + "= ? and " + OPEN_ID + "= ?", arrayOf(local_id.toString() + "", vOpenid + ""))
        LogUtils.d("DBManager", "deleteRowInMappingByLocalID : $row  id:$local_id")
        return row
    }

    fun deleteRowInMappingByServerID(server_id: String): Long {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val row = mDBWrapper.delete(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, IDMappingDB.SERVER_ID + "= ? and " + OPEN_ID + "= ?", arrayOf(server_id + "", vOpenid + ""))
        LogUtils.d("DBManager", "deleteRowInMappingByServerID : $row  id:$server_id")
        return row
    }

    fun updateRowInMapping(contact_id: Int, local_version: Int, server_id: String, server_version: Int, md5: String): Long {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val cv = ContentValues()
        cv.put(IDMappingDB.CONTACT_ID, contact_id)
        cv.put(IDMappingDB.LOCAL_VERSION, local_version)
        cv.put(IDMappingDB.SERVER_ID, server_id)
        cv.put(IDMappingDB.SERVER_VERSION, server_version)
        cv.put(IDMappingDB.MD5, md5)
        val row = mDBWrapper.update(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, cv, IDMappingDB.CONTACT_ID + "= ? and " + IDMappingDB.SERVER_ID + "= ? and " + OPEN_ID + "= ?",
                arrayOf(contact_id.toString() + "", server_id + "", vOpenid + ""))
        LogUtils.d("DBManager", "updateRowInMapping : " + row
                + "  contact id:" + contact_id + "  local_version:" + local_version + "  server id:" + server_id + "  server_version:" + server_version)
        return row
    }

    fun updateLatestVInMappingByServerid(server_id: String, server_latest_version: Int): Long {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val cv = ContentValues()
        cv.put(IDMappingDB.SERVER_ID, server_id)
        cv.put(IDMappingDB.SERVER_LATEST_VERSION, server_latest_version)
        val row = mDBWrapper.update(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, cv, IDMappingDB.SERVER_ID + "= ? and " + OPEN_ID + "= ?",
                arrayOf(server_id + "", vOpenid + ""))
        LogUtils.d("DBManager", "updateLatestVInMappingByServerid : " + row
                + "  server id:" + server_id + "  server_latest_version:" + server_latest_version)
        return row
    }

    fun updateRowInMappingByServerid(contact_id: Int, local_version: Int, server_id: String, server_version: Int): Long {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val cv = ContentValues()
        cv.put(IDMappingDB.CONTACT_ID, contact_id)
        cv.put(IDMappingDB.LOCAL_VERSION, local_version)
        cv.put(IDMappingDB.SERVER_ID, server_id)
        cv.put(IDMappingDB.SERVER_VERSION, server_version)
        val row = mDBWrapper.update(IDMappingDB.CONTACT_MAPPING_TABLE_NAME, cv, IDMappingDB.SERVER_ID + "= ? and " + OPEN_ID + "= ?",
                arrayOf(server_id + "", vOpenid + ""))
        LogUtils.d("DBManager", "updateRowInMappingByServerid : " + row
                + "  contact id:" + contact_id + "  local_version:" + local_version + "  server id:" + server_id + "  server_version:" + server_version)
        return row
    }


    fun batchInsertContactsIntoPhone(contactBeanList: List<ContactBean>?) {

        if (contactBeanList == null || contactBeanList.size == 0) {
            LogUtils.d("DBManager", " batchInsertContactsIntoPhone contactBeanList is null or size = 0.")
            return
        }

        //每次批量插入GAP_COUNT = 200 个联系人，如果数据过大，会发生TransactionTooLargeException，无法执行插入
        var gapCount = 0
        var totalCount = 0
        var timesOfGapCount = 0

        var operations = ArrayList<ContentProviderOperation>()

        var rawContactInsertIndex = 0
        var resultIndex: ArrayList<Int>? = ArrayList()

        val iszx = isZXtype
        for (contactBean in contactBeanList) {
            rawContactInsertIndex = operations.size // 有了它才能给真正的实现批量添加
            resultIndex!!.add(rawContactInsertIndex + 1)
            // 添加Google账号，这里值为null，表示不添加
            var operation1: ContentProviderOperation? = null
            if (iszx) {
                operation1 = ContentProviderOperation.newInsert(RAW_CONTACTS_URI)
                        .withValue("account_name", "Phone")//
                        .withValue("account_type", "sprd.com.android.account.phone")//
                        .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                        .withYieldAllowed(true)
                        .build()
            } else {
                operation1 = ContentProviderOperation.newInsert(RAW_CONTACTS_URI)
                        .withValue("account_name", "Phone")//
                        .withValue("account_type", "Local Phone Account")//
                        .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                        .withYieldAllowed(true)
                        .build()
            }


            operations.add(operation1)

            // 添加data表中name字段
            val name = contactBean.getName()
            if (name != null) {
                val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                        // 第二个参数int previousResult:表示上一个操作的位于operations的第0个索引，
                        // 所以能够将上一个操作返回的raw_contact_id作为该方法的参数
                        .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.Data.DATA1, name!!.getDisplay_name())
                        .withValue(ContactsContract.Data.DATA2, name!!.getGiven_name())
                        .withValue(ContactsContract.Data.DATA3, name!!.getFamily_name())
                        .withValue(ContactsContract.Data.DATA5, name!!.getMiddle_name())
                        .withValue(ContactsContract.Data.DATA4, name!!.getPrefix())
                        .withValue(ContactsContract.Data.DATA6, name!!.getSuffix())
                        .withValue(ContactsContract.Data.DATA7, name!!.getPhonetic_given_name())
                        .withValue(ContactsContract.Data.DATA8, name!!.getPhonetic_middle_name())
                        .withValue(ContactsContract.Data.DATA9, name!!.getPhonetic_family_name())
                        .withValue(ContactsContract.Data.DATA10, name!!.getFull_name_style())
                        .withValue(ContactsContract.Data.DATA11, name!!.getPhonetic_name_style())
                        .withYieldAllowed(true)
                        .build()
                operations.add(tempOperation)
            }


            // 添加data表中phone字段
            val phone_list = contactBean.getPhoneList()
            if (phone_list != null) {
                for (contactPhoneBean in phone_list!!) {
                    if (contactPhoneBean.getCustom() != null) {
                        val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                                .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.Data.DATA1, contactPhoneBean.getNumber())
                                .withValue(ContactsContract.Data.DATA2, contactPhoneBean.getType())
                                .withValue(ContactsContract.Data.DATA3, contactPhoneBean.getCustom())
                                .withValue(ContactsContract.Data.DATA4, contactPhoneBean.getNormalized_number())
                                .withYieldAllowed(true)
                                .build()
                        operations.add(tempOperation)
                    } else {
                        val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                                .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.Data.DATA1, contactPhoneBean.getNumber())
                                .withValue(ContactsContract.Data.DATA2, contactPhoneBean.getType())
                                .withValue(ContactsContract.Data.DATA3, contactPhoneBean.getCustom())
                                .withValue(ContactsContract.Data.DATA4, contactPhoneBean.getNormalized_number())
                                .withYieldAllowed(true)
                                .build()
                        operations.add(tempOperation)
                    }
                }
            }

            // 添加data表中email字段
            val email_list = contactBean.getEmailList()
            if (email_list != null) {
                for (contactEmailBean in email_list!!) {
                    val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                            .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.Data.DATA1, contactEmailBean.getAddress())
                            .withValue(ContactsContract.Data.DATA2, contactEmailBean.getType())
                            .withValue(ContactsContract.Data.DATA3, contactEmailBean.getLabel())
                            .withValue(ContactsContract.Data.DATA4, contactEmailBean.getDispaly_name())
                            .withYieldAllowed(true)
                            .build()
                    operations.add(tempOperation)
                }
            }

            // 添加data表中Organization字段
            val organization_list = contactBean.getOrganizetionList()
            if (organization_list != null) {
                for (contactOrganizationBean in organization_list!!) {
                    val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                            .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.Data.DATA1, contactOrganizationBean.getCompany())
                            .withValue(ContactsContract.Data.DATA2, contactOrganizationBean.getType())
                            .withValue(ContactsContract.Data.DATA3, contactOrganizationBean.getLabel())
                            .withValue(ContactsContract.Data.DATA4, contactOrganizationBean.getTitle())
                            .withValue(ContactsContract.Data.DATA5, contactOrganizationBean.getDepartment())
                            .withValue(ContactsContract.Data.DATA6, contactOrganizationBean.getJob_description())
                            .withValue(ContactsContract.Data.DATA7, contactOrganizationBean.getSymbol())
                            .withValue(ContactsContract.Data.DATA8, contactOrganizationBean.getPhonetic_name())
                            .withValue(ContactsContract.Data.DATA9, contactOrganizationBean.getOffice_location())
                            .withValue(ContactsContract.Data.DATA10, contactOrganizationBean.getPhonetic_name_style())
                            .withYieldAllowed(true)
                            .build()
                    operations.add(tempOperation)
                }
            }

            // 添加data表中Im字段
            val im_list = contactBean.getImList()
            if (im_list != null) {
                for (imBean in im_list!!) {
                    val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                            .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.Data.DATA1, imBean.getData())
                            .withValue(ContactsContract.Data.DATA2, imBean.getType())
                            .withValue(ContactsContract.Data.DATA3, imBean.getLabel())
                            .withValue(ContactsContract.Data.DATA5, imBean.getProtocol())
                            .withValue(ContactsContract.Data.DATA6, imBean.getCustom_protocol())
                            .withYieldAllowed(true)
                            .build()
                    operations.add(tempOperation)
                }
            }

            // 添加data表中Nickname字段
            val nickNameBean = contactBean.getNickname()
            if (nickNameBean != null) {
                val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                        .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.Data.DATA1, nickNameBean!!.getName())
                        .withValue(ContactsContract.Data.DATA2, nickNameBean!!.getType())
                        .withValue(ContactsContract.Data.DATA3, nickNameBean!!.getLabel())
                        .withYieldAllowed(true)
                        .build()
                operations.add(tempOperation)
            }

            // 添加data表中PostalAddress字段
            val postalAddress_list = contactBean.getPostalAddressList()
            if (postalAddress_list != null) {
                for (contactPostalAddressBean in postalAddress_list!!) {
                    val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                            .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.Data.DATA1, contactPostalAddressBean.getFormatted_address())
                            .withValue(ContactsContract.Data.DATA2, contactPostalAddressBean.getType())
                            .withValue(ContactsContract.Data.DATA3, contactPostalAddressBean.getLabel())
                            .withValue(ContactsContract.Data.DATA4, contactPostalAddressBean.getStreet())
                            .withValue(ContactsContract.Data.DATA5, contactPostalAddressBean.getPobox())
                            .withValue(ContactsContract.Data.DATA6, contactPostalAddressBean.getNeighborhood())
                            .withValue(ContactsContract.Data.DATA7, contactPostalAddressBean.getCity())
                            .withValue(ContactsContract.Data.DATA8, contactPostalAddressBean.getRegion())
                            .withValue(ContactsContract.Data.DATA9, contactPostalAddressBean.getPostcode())
                            .withValue(ContactsContract.Data.DATA10, contactPostalAddressBean.getCountry())
                            .withYieldAllowed(true)
                            .build()
                    operations.add(tempOperation)
                }
            }

            // 添加data表中Website字段
            val website_list = contactBean.getWebsiteList()
            if (website_list != null) {
                for (contactWebsiteBean in website_list!!) {
                    val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                            .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.Data.DATA1, contactWebsiteBean.getUrl())
                            .withValue(ContactsContract.Data.DATA2, contactWebsiteBean.getType())
                            .withValue(ContactsContract.Data.DATA3, contactWebsiteBean.getLabel())
                            .withYieldAllowed(true)
                            .build()
                    operations.add(tempOperation)
                }
            }

            // 添加data表中sipaddress字段
            val contactSipAddressBean = contactBean.getSipAddress()
            if (contactSipAddressBean != null) {
                val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                        .withValueBackReference("raw_contact_id", rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.Data.DATA1, contactSipAddressBean!!.getSip_address())
                        .withValue(ContactsContract.Data.DATA2, contactSipAddressBean!!.getType())
                        .withValue(ContactsContract.Data.DATA3, contactSipAddressBean!!.getLabel())
                        .withYieldAllowed(true)
                        .build()
                operations.add(tempOperation)
            }

            gapCount++
            totalCount++

            if (gapCount != 0 && gapCount % GAP_COUNT == 0 && totalCount <= contactBeanList.size) {

                val cpResult: Array<ContentProviderResult>
                try {
                    cpResult = mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

                    if (resultIndex != null && resultIndex.size > 0) {
                        for (i in resultIndex.indices) {
                            val uri = cpResult[resultIndex[i]].uri
                            val cu = mContentResolver.query(uri, arrayOf("contact_id", "version"), null, null, null)
                            cu!!.moveToNext()
                            val contactid = cu.getInt(0)
                            val version = cu.getInt(1)
                            val contactBeanTemp = contactBeanList[timesOfGapCount * GAP_COUNT + i]
                            //插入联系人到本地成功后，更新映射表
                            insertInMapping(contactid, version, contactBeanTemp.getServerId(),
                                    Integer.parseInt(contactBeanTemp.getServer_version()), contactBeanTemp.getMd5())
                        }
                    } else {
                        LogUtils.d("DBManager", "1 batchInsertContactsIntoPhone resultIndex.size()" + resultIndex.size)
                    }
                    timesOfGapCount++

                } catch (e: Exception) {
                    e.printStackTrace()
                    LogUtils.d("DBManager", "1 batchInsertContactsIntoPhone Exception :" + e.message)
                } finally {
                    gapCount = 0
                    operations = ArrayList()
                    resultIndex = ArrayList()
                }

            } else if (gapCount != 0 && totalCount == contactBeanList.size) {
                val cpResult: Array<ContentProviderResult>
                try {
                    cpResult = mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

                    if (resultIndex != null && resultIndex.size > 0) {
                        for (i in resultIndex.indices) {
                            val uri = cpResult[resultIndex[i]].uri
                            val cu = mContentResolver.query(uri, arrayOf("contact_id", "version"), null, null, null)
                            cu!!.moveToNext()
                            val contactid = cu.getInt(0)
                            val version = cu.getInt(1)
                            val contactBeanTemp = contactBeanList[timesOfGapCount * GAP_COUNT + i]
                            //插入联系人到本地成功后，更新映射表
                            insertInMapping(contactid, version, contactBeanTemp.getServerId(),
                                    Integer.parseInt(contactBeanTemp.getServer_version()), contactBeanTemp.getMd5())
                        }
                    } else {
                        LogUtils.d("DBManager", "2 batchInsertContactsIntoPhone resultIndex.size()" + resultIndex.size)
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                    LogUtils.d("DBManager", "2 batchInsertContactsIntoPhone Exception :" + e.message)
                } finally {
                    gapCount = 0
                    operations = ArrayList()
                    resultIndex = ArrayList()
                }
            }
        }


    }

    /**
     * 插入一条联系人信息到手机
     *
     * @return String contactid+version 拼成的字串
     * 更新映射表
     */
    fun insertOneContactIntoPhone(contactBean: ContactBean): String {

        val operations = ArrayList<ContentProviderOperation>()

        // 添加Google账号，这里值为null，表示不添加
        val operation1 = ContentProviderOperation.newInsert(RAW_CONTACTS_URI)
                .withValue("account_name", "Phone")//
                .withValue("account_type", "Local Phone Account")//
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED)
                .withYieldAllowed(true)
                .build()

        operations.add(operation1)

        // 添加data表中name字段
        val name = contactBean.getName()
        if (name != null) {
            val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                    // 第二个参数int previousResult:表示上一个操作的位于operations的第0个索引，
                    // 所以能够将上一个操作返回的raw_contact_id作为该方法的参数
                    .withValueBackReference("raw_contact_id", 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.Data.DATA1, name!!.getDisplay_name())
                    .withValue(ContactsContract.Data.DATA2, name!!.getGiven_name())
                    .withValue(ContactsContract.Data.DATA3, name!!.getFamily_name())
                    .withValue(ContactsContract.Data.DATA5, name!!.getMiddle_name())
                    .withValue(ContactsContract.Data.DATA4, name!!.getPrefix())
                    .withValue(ContactsContract.Data.DATA6, name!!.getSuffix())
                    .withValue(ContactsContract.Data.DATA7, name!!.getPhonetic_given_name())
                    .withValue(ContactsContract.Data.DATA8, name!!.getPhonetic_middle_name())
                    .withValue(ContactsContract.Data.DATA9, name!!.getPhonetic_family_name())
                    .withValue(ContactsContract.Data.DATA10, name!!.getFull_name_style())
                    .withValue(ContactsContract.Data.DATA11, name!!.getPhonetic_name_style())
                    .withYieldAllowed(true)
                    .build()
            operations.add(tempOperation)
        }


        // 添加data表中phone字段
        val phone_list = contactBean.getPhoneList()
        if (phone_list != null) {
            for (contactPhoneBean in phone_list!!) {
                if (contactPhoneBean.getCustom() != null) {
                    val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                            .withValueBackReference("raw_contact_id", 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.Data.DATA1, contactPhoneBean.getNumber())
                            .withValue(ContactsContract.Data.DATA2, contactPhoneBean.getType())
                            .withValue(ContactsContract.Data.DATA3, contactPhoneBean.getCustom())
                            .withValue(ContactsContract.Data.DATA4, contactPhoneBean.getNormalized_number())
                            .withYieldAllowed(true)
                            .build()
                    operations.add(tempOperation)
                } else {
                    val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                            .withValueBackReference("raw_contact_id", 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.Data.DATA1, contactPhoneBean.getNumber())
                            .withValue(ContactsContract.Data.DATA2, contactPhoneBean.getType())
                            .withValue(ContactsContract.Data.DATA3, contactPhoneBean.getCustom())
                            .withValue(ContactsContract.Data.DATA4, contactPhoneBean.getNormalized_number())
                            .withYieldAllowed(true)
                            .build()
                    operations.add(tempOperation)
                }
            }
        }

        // 添加data表中email字段
        val email_list = contactBean.getEmailList()
        if (email_list != null) {
            for (contactEmailBean in email_list!!) {
                val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                        .withValueBackReference("raw_contact_id", 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.Data.DATA1, contactEmailBean.getAddress())
                        .withValue(ContactsContract.Data.DATA2, contactEmailBean.getType())
                        .withValue(ContactsContract.Data.DATA3, contactEmailBean.getLabel())
                        .withValue(ContactsContract.Data.DATA4, contactEmailBean.getDispaly_name())
                        .withYieldAllowed(true)
                        .build()
                operations.add(tempOperation)
            }
        }

        // 添加data表中Organization字段
        val organization_list = contactBean.getOrganizetionList()
        if (organization_list != null) {
            for (contactOrganizationBean in organization_list!!) {
                val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                        .withValueBackReference("raw_contact_id", 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.Data.DATA1, contactOrganizationBean.getCompany())
                        .withValue(ContactsContract.Data.DATA2, contactOrganizationBean.getType())
                        .withValue(ContactsContract.Data.DATA3, contactOrganizationBean.getLabel())
                        .withValue(ContactsContract.Data.DATA4, contactOrganizationBean.getTitle())
                        .withValue(ContactsContract.Data.DATA5, contactOrganizationBean.getDepartment())
                        .withValue(ContactsContract.Data.DATA6, contactOrganizationBean.getJob_description())
                        .withValue(ContactsContract.Data.DATA7, contactOrganizationBean.getSymbol())
                        .withValue(ContactsContract.Data.DATA8, contactOrganizationBean.getPhonetic_name())
                        .withValue(ContactsContract.Data.DATA9, contactOrganizationBean.getOffice_location())
                        .withValue(ContactsContract.Data.DATA10, contactOrganizationBean.getPhonetic_name_style())
                        .withYieldAllowed(true)
                        .build()
                operations.add(tempOperation)
            }
        }

        // 添加data表中Im字段
        val im_list = contactBean.getImList()
        if (im_list != null) {
            for (imBean in im_list!!) {
                val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                        .withValueBackReference("raw_contact_id", 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.Data.DATA1, imBean.getData())
                        .withValue(ContactsContract.Data.DATA2, imBean.getType())
                        .withValue(ContactsContract.Data.DATA3, imBean.getLabel())
                        .withValue(ContactsContract.Data.DATA5, imBean.getProtocol())
                        .withValue(ContactsContract.Data.DATA6, imBean.getCustom_protocol())
                        .withYieldAllowed(true)
                        .build()
                operations.add(tempOperation)
            }
        }

        // 添加data表中Nickname字段
        val nickNameBean = contactBean.getNickname()
        if (nickNameBean != null) {
            val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                    .withValueBackReference("raw_contact_id", 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.Data.DATA1, nickNameBean!!.getName())
                    .withValue(ContactsContract.Data.DATA2, nickNameBean!!.getType())
                    .withValue(ContactsContract.Data.DATA3, nickNameBean!!.getLabel())
                    .withYieldAllowed(true)
                    .build()
            operations.add(tempOperation)
        }

        // 添加data表中PostalAddress字段
        val postalAddress_list = contactBean.getPostalAddressList()
        if (postalAddress_list != null) {
            for (contactPostalAddressBean in postalAddress_list!!) {
                val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                        .withValueBackReference("raw_contact_id", 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.Data.DATA1, contactPostalAddressBean.getFormatted_address())
                        .withValue(ContactsContract.Data.DATA2, contactPostalAddressBean.getType())
                        .withValue(ContactsContract.Data.DATA3, contactPostalAddressBean.getLabel())
                        .withValue(ContactsContract.Data.DATA4, contactPostalAddressBean.getStreet())
                        .withValue(ContactsContract.Data.DATA5, contactPostalAddressBean.getPobox())
                        .withValue(ContactsContract.Data.DATA6, contactPostalAddressBean.getNeighborhood())
                        .withValue(ContactsContract.Data.DATA7, contactPostalAddressBean.getCity())
                        .withValue(ContactsContract.Data.DATA8, contactPostalAddressBean.getRegion())
                        .withValue(ContactsContract.Data.DATA9, contactPostalAddressBean.getPostcode())
                        .withValue(ContactsContract.Data.DATA10, contactPostalAddressBean.getCountry())
                        .withYieldAllowed(true)
                        .build()
                operations.add(tempOperation)
            }
        }

        // 添加data表中Website字段
        val website_list = contactBean.getWebsiteList()
        if (website_list != null) {
            for (contactWebsiteBean in website_list!!) {
                val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                        .withValueBackReference("raw_contact_id", 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.Data.DATA1, contactWebsiteBean.getUrl())
                        .withValue(ContactsContract.Data.DATA2, contactWebsiteBean.getType())
                        .withValue(ContactsContract.Data.DATA3, contactWebsiteBean.getLabel())
                        .withYieldAllowed(true)
                        .build()
                operations.add(tempOperation)
            }
        }

        // 添加data表中sipaddress字段
        val contactSipAddressBean = contactBean.getSipAddress()
        if (contactSipAddressBean != null) {
            val tempOperation = ContentProviderOperation.newInsert(DATA_CONTACTS_URI)
                    .withValueBackReference("raw_contact_id", 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.Data.DATA1, contactSipAddressBean!!.getSip_address())
                    .withValue(ContactsContract.Data.DATA2, contactSipAddressBean!!.getType())
                    .withValue(ContactsContract.Data.DATA3, contactSipAddressBean!!.getLabel())
                    .withYieldAllowed(true)
                    .build()
            operations.add(tempOperation)
        }

        var status = false
        val cpResult: Array<ContentProviderResult>
        var localId = ""
        try {
            cpResult = mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            //            Uri uri = cpResult[cpResult.length-1].uri;
            val uri = cpResult[1].uri
            val cu = mContentResolver.query(uri, arrayOf("contact_id", "version"), null, null, null)
            cu!!.moveToNext()
            val contactid = cu.getInt(0)
            val version = cu.getInt(1)
            localId = IdPatternUtils.formatLocalId(contactid, version)
            status = true
            LogUtils.d("DBManager", " insertOneContactIntoPhone success :" + localId)
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
            LogUtils.d("DBManager", " insertOneContactIntoPhone fail :" + contactBean.getName().toJsonString())
        } finally {
            return localId
        }
    }

    /**
     * 批量删除联系人
     * @param deleteConatctIdList 需要删除的联系人的contact id集合
     */
    fun batchDeleteContacts(deleteConatctIdList: List<Int>?) {

        if (deleteConatctIdList == null || deleteConatctIdList.size == 0) {
            LogUtils.d("DBManager", " batchDeleteContacts deleteConatctIdList is null or size = 0.")
            return
        }

        val ops = ArrayList<ContentProviderOperation>()
        for (contactId in deleteConatctIdList) {

            //根据姓名求id
            val cursor = mContentResolver.query(RAW_CONTACTS_URI, arrayOf(ContactsContract.Data._ID),
                    "contact_id=?", arrayOf(contactId.toString() + ""), null)
            if (cursor!!.moveToFirst()) {
                val id = cursor.getInt(0)
                //根据id删除data中的相应数据

                ops.add(ContentProviderOperation.newDelete(RAW_CONTACTS_URI)
                        .withSelection("contact_id=?", arrayOf(contactId.toString() + ""))
                        .withYieldAllowed(true)
                        .build())
                ops.add(ContentProviderOperation.newDelete(DATA_CONTACTS_URI)
                        .withSelection("raw_contact_id=?", arrayOf(id.toString() + ""))
                        .withYieldAllowed(true)
                        .build())
            }
        }
        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            LogUtils.d("DBManager", " batchDeleteContacts deleteConatctIdList size:" + deleteConatctIdList.size + "  " + deleteConatctIdList.toString())
            for (contactid in deleteConatctIdList) {
                deleteRowInMappingByLocalID(contactid)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        } catch (e: OperationApplicationException) {
            e.printStackTrace()
        }

    }

    fun updateOneContactIntoPhone(contactBean: ContactBean) {
        //从云端更新联系人到本地
        //先删除本地联系人，再插入该联系人云端数据，最后更新映射表
        val serverid = contactBean.getServerId()
        val serverversion = contactBean.getServer_version()
        val cu = queryRowInMappingByServerID(contactBean.getServerId())
        if (cu.count > 0) {
            cu.moveToNext()
            val contactid = cu.getInt(cu.getColumnIndex(IDMappingDB.CONTACT_ID))
            batchDeleteContacts(Arrays.asList(contactid))
            val localid = insertOneContactIntoPhone(contactBean)
            //插入联系人到本地成功后，插入映射表（因为前面已删除映射，所以插入新的映射）
            if (!TextUtils.isEmpty(localid)) {
                val tempcontactid = IdPatternUtils.getIdByParseLocalId(localid)
                val templocalversion = IdPatternUtils.getVersionByParseLocalId(localid)
                insertInMapping(tempcontactid, templocalversion, serverid, Integer.parseInt(serverversion), contactBean.getMd5())
            }
        }

    }

    fun batchUpdateContactsIntoPhone(contactBeanList: List<ContactBean>?) {

        if (contactBeanList == null || contactBeanList.size == 0) {
            LogUtils.d("DBManager", " batchUpdateContactsIntoPhone contactBeanList is null or size = 0.")
            return
        }

        val serverids = ArrayList<String>()
        for (contactBean in contactBeanList) {
            serverids.add(contactBean.getServerId())
        }
        val localids = getMappingLocalIdListByServerId(serverids)
        batchDeleteContacts(localids)
        batchInsertContactsIntoPhone(contactBeanList)
    }


    fun queryRowInSMSIDmappingByMD5(md5: String): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return mDBWrapper.query(SMSidMappingDB.SMS_IDMAPPING_TABLE_NAME, null, SMSidMappingDB.MD5 + "= ? and " + OPEN_ID + "= ?", arrayOf(md5 + "", vOpenid + ""), null, null, null)
    }

    /**
     * 插入短信ID表
     * @param addList 插入列表
     * @return
     */
    fun insertSMSIDmapping(addList: List<SmsInfo>?) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        if (addList != null && addList.size > 0) {
            val count = addList.size
            mDBWrapper.beginTransactionWithListener(object : SQLiteTransactionListener {
                override fun onBegin() {
                    LogUtils.d("DBManager", "insertSMSIDmapping onBegin")
                }

                override fun onCommit() {
                    LogUtils.d("DBManager", "insertSMSIDmapping onCommit Count:" + count)
                }

                override fun onRollback() {
                    LogUtils.d("DBManager", "insertSMSIDmapping onRollback")
                }
            })
            for (i in addList.indices) {
                var md5 = addList[i].getServer_id()
                if (TextUtils.isEmpty(md5)) {
                    md5 = GetMD5Utils.getMD5(addList[i].getAddress()
                            + addList[i].getDate() + addList[i].getType())
                }

                val cv = ContentValues()
                cv.put(SMSidMappingDB.MD5, md5)
                cv.put(OPEN_ID, vOpenid)
                var row: Long = -1

                row = mDBWrapper.insertWithOnConflict(SMSidMappingDB.SMS_IDMAPPING_TABLE_NAME, null, cv, CONFLICT_IGNORE)
                //LogUtils.d("DBManager", "insertSMSIDmapping : " + row +  "  md5: " + md5);
            }
            mDBWrapper.endTransaction()
        }
    }


    fun querySMSInMapping(): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return mDBWrapper.query(SMSidMappingDB.SMS_IDMAPPING_TABLE_NAME, null, OPEN_ID + "= ?", arrayOf(vOpenid + ""), null, null, null)
    }


    /**
     * 插入短信表
     * @return
     */
    fun insertSMS(restoreList: List<SmsInfo>) {
        val uri = Uri.parse("content://sms/")
        for (i in restoreList.indices) {
            val cv = ContentValues()
            cv.put("address", restoreList[i].getAddress())
            cv.put("date", restoreList[i].getDate())
            cv.put("body", restoreList[i].getBody())
            cv.put("type", restoreList[i].getType())
            cv.put("type", restoreList[i].getType())
            try {
                mContentResolver.insert(uri, cv)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    fun queryCallLogInMapping(): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return mDBWrapper.query(CallLogIdMappingDB.CALLLOG_IDMAPPING_TABLE_NAME, null, OPEN_ID + "= ?", arrayOf(vOpenid + ""), null, null, null)
    }

    /**
     * 插入通话记录ID表
     * @param addList 插入列表
     * @return
     */
    fun insertCallLogIDMapping(addList: List<CallLogBean>?) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        if (addList != null && addList.size > 0) {
            val count = addList.size
            mDBWrapper.beginTransactionWithListener(object : SQLiteTransactionListener {
                override fun onBegin() {
                    LogUtils.d("DBManager", "insertCallLogIDMapping onBegin")
                }

                override fun onCommit() {
                    LogUtils.d("DBManager", "insertCallLogIDMapping onCommit Count:" + count)
                }

                override fun onRollback() {
                    LogUtils.d("DBManager", "insertCallLogIDMapping onRollback")
                }
            })
            for (i in addList.indices) {
                val id = addList[i].getServerid()
                val md5 = addList[i].getServerid()
                val cv = ContentValues()
                //                cv.put(CallLogIdMappingDB._ID, id);
                cv.put(CallLogIdMappingDB.MD5, md5)
                cv.put(OPEN_ID, vOpenid)
                var row: Long = -1

                row = mDBWrapper.insertWithOnConflict(CallLogIdMappingDB.CALLLOG_IDMAPPING_TABLE_NAME, null, cv, CONFLICT_IGNORE)
                //LogUtils.d("DBManager","insertCallLogIDMapping : "+row+"  _id: "+id+"  md5: "+md5);
            }
            mDBWrapper.endTransaction()
        }
    }

    fun queryRowInCallLogIDMappingByMD5(md5: String): Cursor {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return mDBWrapper.query(CallLogIdMappingDB.CALLLOG_IDMAPPING_TABLE_NAME, null, CallLogIdMappingDB.MD5 + "= ? and " + OPEN_ID + "= ?", arrayOf(md5 + "", vOpenid + ""), null, null, null)
    }

    /**
     * 插入一条联系人信息到手机
     *
     * @return true 插入成功，false 插入失败
     */
    fun insertOneCallLogIntoPhone(callLogBean: CallLogBean): Boolean {

        val operations = ArrayList<ContentProviderOperation>()

        //        Cursor cu = mContentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{"raw_contact_id","_id"},
        //                "data1 = ?",new String[]{callLogBean.getNumber()}," contact_id desc");
        //
        //        int raw_contact_id = 0;
        //        int data_id = 0;
        //        if (cu.getCount()>0){
        //            cu.moveToNext();
        //            raw_contact_id = cu.getInt(0);
        //            data_id = cu.getInt(1);
        //        }

        //查询获取本地匹配的联系人姓名
        val uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + callLogBean.getNumber())
        val resolver = mContentResolver
        val cursor = resolver.query(uri, arrayOf("display_name"), null, null, " _id desc")
        var name = ""
        if (cursor!!.moveToFirst()) {
            name = cursor.getString(0)
            LogUtils.i(TAG, "insertOneCallLogIntoPhone match name:" + name)
        }
        cursor.close()

        // 添加通话记录
        val operation1 = ContentProviderOperation.newInsert(CallLog.Calls.CONTENT_URI)
                .withValue(CallLog.Calls.NUMBER, callLogBean.getNumber())//
                .withValue(CallLog.Calls.DATE, callLogBean.getDate())//
                .withValue(CallLog.Calls.TYPE, callLogBean.getType())//
                .withValue(CallLog.Calls.NUMBER_PRESENTATION, callLogBean.getPresentation())//
                .withValue(CallLog.Calls.DURATION, callLogBean.getDuration())//
                //.withValue(CallLog.Calls.DATA_USAGE, callLogBean.getData_usage())//
                //.withValue(CallLog.Calls.FEATURES, callLogBean.getFeatures())//
                .withValue(CallLog.Calls.CACHED_NAME, name/*callLogBean.getCachename()*/)//
                .withValue(CallLog.Calls.CACHED_NUMBER_TYPE, callLogBean.getCachenumbertype())//
                .withValue(CallLog.Calls.CACHED_NUMBER_LABEL, callLogBean.getCachenumberlabel())//
                .withValue(CallLog.Calls.COUNTRY_ISO, callLogBean.getCountryiso())//
                .withValue(CallLog.Calls.IS_READ, callLogBean.getIs_read())//
                .withValue(CallLog.Calls.GEOCODED_LOCATION, callLogBean.getGeocoded_location())//
                //.withValue("raw_contact_id",raw_contact_id)//TODO
                //.withValue("data_id",data_id)//
                .withYieldAllowed(true)
                .build()

        operations.add(operation1)

        var status = false
        try {
            mContentResolver.applyBatch(CallLog.AUTHORITY, operations)
            status = true
            LogUtils.d(TAG, " insertOneCallLogIntoPhone success :" + callLogBean.toJsonString())
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
            LogUtils.d(TAG, " insertOneCallLogIntoPhone fail :" + callLogBean.toJsonString())
        } finally {
            return status
        }
    }

    /**
     * 插入修改冲突的联系人id
     *
     * TODO：现修改冲突的联系人不做处理，暂保存，后期若处理，还需执行相应移除数据操作
     */
    fun insertInUpdateConflictContact(contact_id: Int): Long {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val cv = ContentValues()
        cv.put(IDMappingDB.CONTACT_ID, contact_id)
        cv.put(OPEN_ID, vOpenid)
        var row: Long = -1
        row = mDBWrapper.insertWithOnConflict(LocalUpdateConflictContactDB.LOCAL_UPDATE_CONFLICT_TABLE_NAME, null, cv, CONFLICT_IGNORE)
        LogUtils.d(TAG, "insertInUpdateConflictContact : $row  contact_id: $contact_id")

        return row
    }

    companion object {

        val TAG = DBManager::class.java.simpleName

        val GAP_COUNT = 200
        private var DBManagerInstance: DBManager? = null

        fun getInstance(context: Context): DBManager {
            if (DBManagerInstance == null) {
                DBManagerInstance = DBManager(context)
            }
            return DBManagerInstance
        }

        //  联系人表的uri
        var CONTACTS_URI = ContactsContract.Contacts.CONTENT_URI
        var RAW_CONTACTS_URI = ContactsContract.RawContacts.CONTENT_URI
        var DATA_CONTACTS_URI = ContactsContract.Data.CONTENT_URI
        var DATA_CALLLOG_URI = CallLog.Calls.CONTENT_URI
        var PHONE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        var EMAIL_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI
    }
}
