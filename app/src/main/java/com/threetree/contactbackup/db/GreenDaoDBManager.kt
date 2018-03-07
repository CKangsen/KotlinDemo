package com.threetree.contactbackup.db

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.ContentValues
import android.content.OperationApplicationException
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
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
import com.threetree.contactbackup.util.LogUtils
import com.greendao.gen.CallLogIdMapping
import com.greendao.gen.CallLogIdMappingDao
import com.greendao.gen.DaoMaster
import com.greendao.gen.DaoSession
import com.greendao.gen.IDMapping
import com.greendao.gen.IDMappingDao
import com.greendao.gen.LocalUpdateConflictContact
import com.greendao.gen.LocalUpdateConflictContactDao
import com.greendao.gen.SMSidMappingDao
import com.greendao.gen.ServerDelete
import com.greendao.gen.ServerDeleteDao

import java.util.ArrayList
import java.util.Hashtable


class GreenDaoDBManager {
    private val daoSession: DaoSession
    private var vOpenid: String? = null
    private val mContentResolver: ContentResolver


    //long initTime = System.currentTimeMillis();
    val mappingLocalPart: Hashtable<Int, Int>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val hashtable = Hashtable<Int, Int>()
            val idMappingList = queryAllInMapping()
            for (idMapping in idMappingList) {
                hashtable.put(idMapping.contact_id, idMapping.local_version)
            }
            LogUtils.d("DBManager", " getMappingLocalPart  Hashtable size:" + hashtable.size)
            return hashtable
        }

    //获取中间表的数量 判断是否初次使用
    val mappingcount: Int
        get() {
            val result: Int
            val idMappingList = queryAllInMapping()
            result = idMappingList.size
            return result
        }


    val mappingServerPart: Hashtable<String, Int>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val hashtable = Hashtable<String, Int>()
            val idMappingList = queryAllInMapping()
            for (idMapping in idMappingList) {
                hashtable.put(idMapping.server_id, idMapping.server_version)
            }
            LogUtils.d("DBManager", " getMappingServerPart  Hashtable size:" + hashtable.size)
            return hashtable
        }

    val isExistContactMapping: Boolean
        get() {
            val count = mappingcount
            return if (count > 0) {
                true
            } else {
                false
            }
        }

    val listServerDeleteContactBeansOnlyIdAndMd5: List<ContactBean>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val mList = ArrayList<ContactBean>()
            val serverDeleteList = daoSession.serverDeleteDao.queryRaw(ServerDeleteDao.Properties.Openid.toString() + "= ?", *arrayOf(vOpenid!! + ""))
            for (serverDelete in serverDeleteList) {
                val serverid_integrated = serverDelete.server_id
                val contactBean = ContactBean()
                contactBean.setServerId(IdPatternUtils.getIdByParseServerId(serverid_integrated))
                contactBean.setServer_version(IdPatternUtils.getVersionByParseServerId(serverid_integrated).toString())
                contactBean.setMd5(serverDelete.md5)
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
            val am = AccountManager.get(Factory.get().getApplicationContext())
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
            val smSidMappingList = querySMSInMapping()
            for (smSidMapping in smSidMappingList) {
                hashtable.put(smSidMapping.getMd5(), smSidMapping.getMd5())
            }

            return hashtable
        }

    val callLogIdMappingList: Hashtable<String, String>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val hashtable = Hashtable<String, String>()
            val callLogIdMappingList = queryCallLogInMapping()
            for (callLogIdMapping in callLogIdMappingList) {
                hashtable.put(callLogIdMapping.md5, callLogIdMapping.md5)
            }

            return hashtable
        }

    val updateConflictContactIds: List<Int>
        get() {
            vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            val conflictContactIds = ArrayList<Int>()
            val localUpdateConflictContactList = daoSession.localUpdateConflictContactDao.queryRaw(
                    LocalUpdateConflictContactDao.Properties.Openid.toString() + "= ?",
                    *arrayOf(vOpenid!! + ""))
            for (localUpdateConflictContact in localUpdateConflictContactList) {
                conflictContactIds.add(Integer.valueOf(localUpdateConflictContact.contact_id!!))
            }

            return conflictContactIds
        }

    init {
        val helper = DaoMaster.DevOpenHelper(Factory.get().getApplicationContext(),
                "waka_icloud.db", null)
        val db = helper.writableDatabase
        val daoMaster = DaoMaster(db)
        daoSession = daoMaster.newSession()
        mContentResolver = Factory.get().getApplicationContext().getContentResolver()
    }


    fun queryAllInMapping(): List<IDMapping> {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        LogUtils.d("DBManager", "queryAllInMapping  vOpenid: " + vOpenid!!)
        return daoSession.idMappingDao.queryRaw(IDMappingDao.Properties.Openid.toString() + "= ?", vOpenid!! + "")
    }


    fun queryRowsInMappingByLocalIDs(localids: List<Int>): List<IDMapping> {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        var selectionArgs = "("
        for (id in localids) {
            selectionArgs = selectionArgs + id + ","
        }
        selectionArgs = selectionArgs.substring(0, selectionArgs.length - 1)
        selectionArgs = selectionArgs + ")"

        return daoSession.idMappingDao.queryRaw(IDMappingDao.Properties.Contact_id.toString() + " in " + selectionArgs + " and " + IDMappingDao.Properties.Openid + "= ?", vOpenid!! + "")
    }

    fun getMappingServerIdListByLocalId(localids: List<Int>?): List<String> {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val serverIdList = ArrayList<String>()
        if (localids == null || localids.size == 0) {
            return serverIdList
        }

        val idMappingList = queryRowsInMappingByLocalIDs(localids)
        for (idMapping in idMappingList) {
            serverIdList.add(idMapping.server_id)
        }
        return serverIdList
    }

    fun getServerIdListByLocalId(localids: List<Int>?): List<String> {
        val serverIdList = ArrayList<String>()
        if (localids == null || localids.size == 0) {
            return serverIdList
        }

        val idMappingList = queryRowsInMappingByLocalIDs(localids)
        for (idMapping in idMappingList) {
            serverIdList.add(IdPatternUtils.formatServerId(idMapping.server_id, idMapping.server_version!!))
        }

        return serverIdList
    }

    fun getIDMappingByLocalId(localids: List<Int>?): List<IDMappingValue> {
        val mappingValues = ArrayList<IDMappingValue>()
        if (localids == null || localids.size == 0) {
            return mappingValues
        }

        val idMappingList = queryRowsInMappingByLocalIDs(localids)
        for (idMapping in idMappingList) {
            val value = IDMappingValue(idMapping.id!!.toInt(),
                    idMapping.local_version,
                    idMapping.server_id,
                    idMapping.server_version,
                    idMapping.md5)
            mappingValues.add(value)
        }
        return mappingValues
    }


    fun queryRowsInMappingByServerIDs(serverids: List<String>): List<IDMapping> {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        var selectionArgs = "( "
        for (id in serverids) {
            selectionArgs = "$selectionArgs'$id',"
        }
        selectionArgs = selectionArgs.substring(0, selectionArgs.length - 1)
        selectionArgs = selectionArgs + " )"

        return daoSession.idMappingDao.queryRaw(IDMappingDao.Properties.Server_id.toString() + " in " + selectionArgs + " and " + IDMappingDao.Properties.Openid + "= ?",
                vOpenid!! + "")
    }

    fun getIDMappingByServerId(serverids: List<String>?): List<IDMappingValue> {
        val mappingValues = ArrayList<IDMappingValue>()
        if (serverids == null || serverids.size == 0) {
            return mappingValues
        }

        val idMappingList = queryRowsInMappingByServerIDs(serverids)
        for (idMapping in idMappingList) {
            val value = IDMappingValue(idMapping.id!!.toInt(),
                    idMapping.local_version,
                    idMapping.server_id,
                    idMapping.server_version,
                    idMapping.md5)
            mappingValues.add(value)
        }
        return mappingValues
    }

    fun getServerIdListWithoutVersionByLocalId(localids: List<Int>?): List<String> {
        val serverIdList = ArrayList<String>()
        if (localids == null || localids.size == 0) {
            return serverIdList
        }

        val idMappingList = queryRowsInMappingByLocalIDs(localids)
        for (idMapping in idMappingList) {
            serverIdList.add(idMapping.server_id)
        }

        LogUtils.d("DBManager", " getServerIdListWithoutVersionByLocalId  serverIdList size:" + serverIdList.size)
        return serverIdList
    }

    fun getMappingLocalIdListByServerId(serverids: List<String>?): List<Int> {
        val initTime = System.currentTimeMillis()
        val localIdList = ArrayList<Int>()
        if (serverids == null || serverids.size == 0) {
            return localIdList
        }
        val idMappingList = queryRowsInMappingByServerIDs(serverids)
        for (idMapping in idMappingList) {
            localIdList.add(idMapping.contact_id)
        }
        LogUtils.d("DBManager", " getMappingServerIdListByLocalId  Hashtable Time:" + (System.currentTimeMillis() - initTime))
        return localIdList
    }

    fun queryRowInMappingByLocalID(local_id: Int): List<IDMapping> {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return daoSession.idMappingDao.queryRaw(IDMappingDao.Properties.Contact_id.toString() + "= ? and " + IDMappingDao.Properties.Openid + "= ?",
                local_id.toString() + "", vOpenid!! + "")
    }

    fun queryRowInMappingByServerID(server_id: String): List<IDMapping> {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return daoSession.idMappingDao.queryRaw(IDMappingDao.Properties.Server_id.toString() + "= ? and " + IDMappingDao.Properties.Openid + "= ?",
                server_id + "", vOpenid!! + ""
        )
    }

    fun insertInServerdelete(id: String, md5: String, data: String): Long {

        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val cv = ServerDelete()
        cv.server_id = id
        cv.md5 = md5
        cv.contact_data = data
        cv.openid = vOpenid
        var row: Long = -1
        row = daoSession.serverDeleteDao.insert(cv)
        LogUtils.d("DBManager", "insertInServerdelete row: $row   server_id: $id")

        return row
    }

    fun batchInsertInServerdelete(server_deleteList: List<ContactBean>?) {

        if (server_deleteList == null && server_deleteList!!.size <= 0) {
            return
        }

        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())

        val count = server_deleteList.size

        val serverDeleteList = ArrayList<ServerDelete>()
        for (contactBean in server_deleteList) {
            val cv = ServerDelete()
            cv.server_id = IdPatternUtils.formatServerId(contactBean.getServerId(), Integer.parseInt(contactBean.getServer_version()))
            cv.md5 = contactBean.getMd5()
            cv.openid = vOpenid
            serverDeleteList.add(cv)

        }
        daoSession.serverDeleteDao.insertInTx(serverDeleteList)

    }

    fun insertInMapping(contact_id: Int, local_version: Int, server_id: String, server_version: Int, md5: String): Long {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())

        val cv = IDMapping()
        cv.contact_id = contact_id
        cv.local_version = local_version
        cv.server_id = server_id
        cv.server_version = server_version
        cv.md5 = md5
        cv.openid = vOpenid
        var row: Long = -1
        row = daoSession.idMappingDao.insert(cv)

        LogUtils.d("DBManager", "insertInMapping : " + row + "  contact_id: " + contact_id + "  server_id: " + server_id
                + "  md5: " + md5 + "  openid:" + vOpenid)

        return row
    }

    fun deleteRowInMappingByLocalID(local_id: Int) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val cv = IDMapping()
        cv.contact_id = local_id
        cv.openid = vOpenid
        daoSession.idMappingDao.delete(cv)
    }

    fun deleteRowInMappingByServerID(server_id: String) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        val cv = IDMapping()
        cv.server_id = server_id
        cv.openid = vOpenid
        daoSession.idMappingDao.delete(cv)
    }

    fun updateRowInMapping(contact_id: Int, local_version: Int, server_id: String, server_version: Int, md5: String) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())

        val cv = IDMapping()
        cv.contact_id = contact_id
        cv.local_version = local_version
        cv.server_id = server_id
        cv.server_version = server_version
        cv.md5 = md5
        cv.openid = vOpenid
        daoSession.idMappingDao.update(cv)

    }

    fun updateLatestVInMappingByServerid(server_id: String, server_latest_version: Int) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())

        val cv = IDMapping()
        cv.server_id = server_id
        cv.server_latest_version = server_latest_version
        cv.openid = vOpenid
        daoSession.idMappingDao.update(cv)

    }

    fun updateRowInMappingByServerid(contact_id: Int, local_version: Int, server_id: String, server_version: Int) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())

        val cv = IDMapping()
        cv.contact_id = contact_id
        cv.local_version = local_version
        cv.server_id = server_id
        cv.server_version = server_version
        cv.openid = vOpenid
        daoSession.idMappingDao.update(cv)
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

    fun insertSMSIDmapping(addList: List<SmsInfo>?) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        if (addList != null && addList.size > 0) {
            val count = addList.size

            val smSidMappingList = ArrayList<SMSidMapping>()
            for (i in addList.indices) {
                var md5 = addList[i].getServer_id()
                if (TextUtils.isEmpty(md5)) {
                    md5 = GetMD5Utils.getMD5(addList[i].getAddress()
                            + addList[i].getDate() + addList[i].getType())
                }


                val cv = SMSidMapping()
                cv.setMd5(md5)
                cv.setOpenid(vOpenid)
                smSidMappingList.add(cv)
            }
            daoSession.smSidMappingDao.insertInTx(smSidMappingList)
        }
    }

    fun querySMSInMapping(): List<SMSidMapping> {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return daoSession.smSidMappingDao.queryRaw(SMSidMappingDao.Properties.Openid.toString() + "= ?",
                vOpenid!! + "")
    }

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

    fun queryCallLogInMapping(): List<CallLogIdMapping> {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        return daoSession.callLogIdMappingDao.queryRaw(CallLogIdMappingDao.Properties.Openid.toString() + "= ?",
                vOpenid!! + "")
    }

    fun insertCallLogIDMapping(addList: List<CallLogBean>?) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
        if (addList != null && addList.size > 0) {
            val count = addList.size

            val callLogIdMappingList = ArrayList<CallLogIdMapping>()
            for (i in addList.indices) {
                val id = addList[i].getServerid()
                val md5 = addList[i].getServerid()

                val cv = CallLogIdMapping()
                cv.md5 = md5
                cv.openid = vOpenid
                callLogIdMappingList.add(cv)
            }
            daoSession.callLogIdMappingDao.updateInTx(callLogIdMappingList)
        }
    }

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
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        } finally {
            return status
        }
    }

    fun insertInUpdateConflictContact(contact_id: Int) {
        vOpenid = String.valueOf(ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())

        val cv = LocalUpdateConflictContact()
        cv.contact_id = contact_id
        cv.openid = vOpenid
        daoSession.localUpdateConflictContactDao.insert(cv)

    }

    companion object {

        private var instance: GreenDaoDBManager? = null

        val GAP_COUNT = 200

        fun getInstance(): GreenDaoDBManager {
            if (instance == null) {
                synchronized(GreenDaoDBManager::class.java) {
                    if (instance == null) {
                        instance = GreenDaoDBManager()
                    }
                }
            }
            return instance
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
