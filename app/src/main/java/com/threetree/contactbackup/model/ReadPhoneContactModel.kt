package com.threetree.contactbackup.model

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.support.v4.content.ContextCompat
import android.widget.FrameLayout

import com.threetree.contactbackup.Factory
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
import com.threetree.contactbackup.util.GetMD5Utils
import com.threetree.contactbackup.util.LogUtils
import com.threetree.contactbackup.util.PermissionUtils

import java.util.ArrayList
import java.util.Hashtable


class ReadPhoneContactModel(private val mResolver: ContentResolver) {
    private val RawContactsUri = ContactsContract.RawContacts.CONTENT_URI
    private val DataContactsUri = ContactsContract.Data.CONTENT_URI
    private val DataCallLogUri = CallLog.Calls.CONTENT_URI

    private val phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
    private val emailUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI


    private var mContactBeanList: MutableList<ContactBean>? = null
    private var mContactIdAndVerHashtable: Hashtable<Int, Int>? = null


    /**
     * 获取手机上的所有联系人对应的Id，Version构成的Hashtable
     *
     * 返回Hashtable<Integer></Integer>,Integer> K指Id， V指Version
     */
    val contactIdAndVerHashtable: Hashtable<Int, Int>?
        get() {
            getPhoneContactsData()
            return mContactIdAndVerHashtable
        }

    /**
     * 获取手机上的所有联系人（所有数据，用于上传）
     *
     * 返回List<ContactBean>
    </ContactBean> */
    val contactAllInfo: List<ContactBean>?
        get() {
            val initTime = System.currentTimeMillis()
            getPhoneContactsAllData()

            LogUtils.d("ReadPhoneContactModel", " getContactAllInfo  List<ContactBean> Time:" + (System.currentTimeMillis() - initTime))
            return mContactBeanList
        }

    /**
     * 获取手机上的所有联系人（仅包含名称，手机号，用于匹配）
     *
     * 返回List<ContactBean>
    </ContactBean> */
    val contactInfo: List<ContactBean>?
        get() {
            val initTime = System.currentTimeMillis()
            getPhoneContactsData()

            LogUtils.d("ReadPhoneContactModel", " getContactInfo  List<ContactBean> Time:" + (System.currentTimeMillis() - initTime))
            return mContactBeanList
        }

    /**
     * 根据提供的idList获取联系人
     *
     * 返回List<ContactBean>
    </ContactBean> */
    fun getContactInfoByIdList(idList: List<Int>?): List<ContactBean> {
        if (mContactBeanList == null || mContactBeanList!!.size <= 0) {
            getPhoneContactsData()
        }

        val newContactBeanList = ArrayList<ContactBean>()

        if (idList != null && idList.size > 0) {
            for (contactBean in mContactBeanList!!) {
                if (idList.contains(contactBean.get_id())) {
                    newContactBeanList.add(contactBean)
                }
            }
        }

        return newContactBeanList
    }

    /**
     * 根据提供的idList获取联系人
     *
     * 返回List<ContactBean>
    </ContactBean> */
    fun getContactInfoAllDataByIdList(idList: List<Int>?): List<ContactBean> {
        if (mContactBeanList == null || mContactBeanList!!.size <= 0) {
            getPhoneContactsAllData()
        }

        val newContactBeanList = ArrayList<ContactBean>()

        if (idList != null && idList.size > 0) {
            for (contactBean in mContactBeanList!!) {
                if (idList.contains(contactBean.get_id())) {
                    newContactBeanList.add(contactBean)
                }
            }
        }

        return newContactBeanList
    }


    private fun getPhoneContactsData() {

        val allinitTime = System.currentTimeMillis()
        var alldoneTime: Long = 0

        var initTime: Long = 0
        var doneTime: Long = 0

        mContactIdAndVerHashtable = Hashtable()
        mContactBeanList = ArrayList<ContactBean>()
        //联系人集合
        val contactsCursor = mResolver.query(DataContactsUri,
                selectContactColumms, null, null, " contact_id desc")
        //" account_name = ? ", new String[]{"Phone"},

        var name = ContactNameBean()
        var phone_list: MutableList<ContactPhoneBean> = ArrayList<ContactPhoneBean>()

        var contactBean = ContactBean()

        var temp_contact_id = 0
        var temp_version = 0
        while (contactsCursor!!.moveToNext()) {
            val contact_id = contactsCursor.getInt(CONTACT_ID_INDEX)
            val version = contactsCursor.getInt(VERSION_INDEX)
            if (contactsCursor.isFirst) {
                temp_contact_id = contact_id
                temp_version = version
            }
            if (contact_id == temp_contact_id) {

            } else {
                contactBean.set_id(temp_contact_id)
                contactBean.setLocal_version(temp_version.toString() + "")
                contactBean.setName(name)
                contactBean.setPhoneList(phone_list)
                mContactIdAndVerHashtable!!.put(temp_contact_id, temp_version)
                //设置md5
                var phoneInfo = "-"
                if (contactBean.getPhoneList() != null) {
                    for (phoneBean in contactBean.getPhoneList()) {
                        phoneInfo = phoneInfo + phoneBean.getNumber()
                    }
                }
                var namePart = "-"
                if (contactBean.getName() != null) {
                    namePart = (namePart + contactBean.getName().getGiven_name()
                            + contactBean.getName().getFamily_name()
                            + contactBean.getName().getPrefix()
                            + contactBean.getName().getMiddle_name()
                            + contactBean.getName().getSuffix())
                }
                val md5 = GetMD5Utils.getMD5(namePart + phoneInfo)
                contactBean.setMd5(md5)

                mContactBeanList!!.add(contactBean)
                doneTime = System.currentTimeMillis()
                //LogUtils.d("getPhoneContactsData", " getPhoneContactsData One Time:"+(doneTime-initTime));

                temp_contact_id = contact_id
                temp_version = version
                contactBean = ContactBean()
                name = ContactNameBean()
                phone_list = ArrayList<ContactPhoneBean>()
                initTime = System.currentTimeMillis()
            }
            val mimeType = contactsCursor.getString(MIMETYPE_INDEX)

            if (mimeType == ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) {
                name = ContactNameBean(
                        contactsCursor.getInt(CONTACT_DATA2_INDEX), //ContactsContract.CommonDataKinds.Phone.TYPE
                        contactsCursor.getString(CONTACT_DATA3_INDEX), //Phone.LABEL
                        contactsCursor.getString(CONTACT_DATA1_INDEX), //StructuredName.DISPLAY_NAME
                        contactsCursor.getString(CONTACT_DATA2_INDEX), //StructuredName.GIVEN_NAME
                        contactsCursor.getString(CONTACT_DATA3_INDEX), //StructuredName.FAMILY_NAME
                        contactsCursor.getString(CONTACT_DATA5_INDEX), //StructuredName.MIDDLE_NAME
                        contactsCursor.getString(CONTACT_DATA4_INDEX), //StructuredName.PREFIX
                        contactsCursor.getString(CONTACT_DATA6_INDEX), //StructuredName.SUFFIX
                        contactsCursor.getString(CONTACT_DATA7_INDEX), //StructuredName.PHONETIC_GIVEN_NAME
                        contactsCursor.getString(CONTACT_DATA8_INDEX), //StructuredName.PHONETIC_MIDDLE_NAME
                        contactsCursor.getString(CONTACT_DATA9_INDEX), //StructuredName.PHONETIC_FAMILY_NAME
                        contactsCursor.getString(CONTACT_DATA10_INDEX), //StructuredName.FULL_NAME_STYLE
                        contactsCursor.getString(CONTACT_DATA11_INDEX))//StructuredName.PHONETIC_NAME_STYLE

            } else if (mimeType == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
                phone_list.add(
                        ContactPhoneBean(
                                contactsCursor.getInt(CONTACT_DATA2_INDEX), //Phone.TYPE
                                contactsCursor.getString(CONTACT_DATA3_INDEX), //Phone.LABEL
                                contactsCursor.getString(CONTACT_DATA1_INDEX), //Phone.NUMBER
                                contactsCursor.getString(CONTACT_DATA4_INDEX))//Phone.NORMALIZED_NUMBER
                )
            }
            if (contactsCursor.isLast) {
                contactBean.set_id(temp_contact_id)
                contactBean.setLocal_version(temp_version.toString() + "")
                contactBean.setName(name)
                contactBean.setPhoneList(phone_list)
                mContactIdAndVerHashtable!!.put(temp_contact_id, temp_version)
                //设置md5
                var phoneInfo = "-"
                if (contactBean.getPhoneList() != null) {
                    for (phoneBean in contactBean.getPhoneList()) {
                        phoneInfo = phoneInfo + phoneBean.getNumber()
                    }
                }
                var namePart = "-"
                if (contactBean.getName() != null) {
                    namePart = (namePart + contactBean.getName().getGiven_name()
                            + contactBean.getName().getFamily_name()
                            + contactBean.getName().getPrefix()
                            + contactBean.getName().getMiddle_name()
                            + contactBean.getName().getSuffix())
                }
                val md5 = GetMD5Utils.getMD5(namePart + phoneInfo)
                contactBean.setMd5(md5)
                mContactBeanList!!.add(contactBean)
                doneTime = System.currentTimeMillis()
                //LogUtils.d("getPhoneContactsData", " getPhoneContactsData One Time:"+(doneTime-initTime));
            }
        }


        alldoneTime = System.currentTimeMillis()
        Factory.get().getCacheStatisticsManager().setLocal_current_count(mContactIdAndVerHashtable!!.size)
        Factory.get().getApplicationPrefsManager().setLocalCurrentContactCount(mContactIdAndVerHashtable!!.size)
        LogUtils.d("getPhoneContactsData", " getPhoneContactsData All Time:" + (alldoneTime - allinitTime))
    }


    fun getPhoneContactsAllData() {

        val allinitTime = System.currentTimeMillis()
        var alldoneTime: Long = 0

        var initTime: Long = 0
        var doneTime: Long = 0

        mContactIdAndVerHashtable = Hashtable()
        mContactBeanList = ArrayList<ContactBean>()
        //联系人集合
        val contactsCursor = mResolver.query(DataContactsUri,
                selectContactColumms, null, null, " contact_id desc")

        var name = ContactNameBean()
        var phone_list: MutableList<ContactPhoneBean> = ArrayList<ContactPhoneBean>()

        var nickname = ContactNickNameBean()
        var sipaddress = ContactSipAddressBean()
        var email_list: MutableList<ContactEmailBean> = ArrayList<ContactEmailBean>()
        var organization_list: MutableList<ContactOrganizationBean> = ArrayList<ContactOrganizationBean>()
        var im_list: MutableList<ContactImBean> = ArrayList<ContactImBean>()
        var postaladdress_list: MutableList<ContactPostalAddressBean> = ArrayList<ContactPostalAddressBean>()
        var website_list: MutableList<ContactWebsiteBean> = ArrayList<ContactWebsiteBean>()

        var contactBean = ContactBean()

        var temp_contact_id = 0
        var temp_version = 0
        var temp_raw_id = 0
        var temp_times_contacted = 0
        var temp_starred = 0
        var temp_has_phone_number = 0
        var temp_contact_last_updated_timestamp = 0
        while (contactsCursor!!.moveToNext()) {
            val contact_id = contactsCursor.getInt(CONTACT_ID_INDEX)
            val version = contactsCursor.getInt(VERSION_INDEX)
            val raw_id = contactsCursor.getInt(CONTACT_NAME_RAW_CONTACT_ID_INDEX)
            val times_contacted = contactsCursor.getInt(CONTACT_TIMES_CONTACTED_INDEX)
            val starred = contactsCursor.getInt(CONTACT_STARRED_INDEX)
            val has_phone_number = contactsCursor.getInt(CONTACT_HAS_PHONE_NUMBER_INDEX)
            val contact_last_updated_timestampion = contactsCursor.getInt(CONTACT_LAST_UPDATED_TIMESTAMP_INDEX)
            if (contactsCursor.isFirst) {
                temp_contact_id = contact_id
                temp_version = version
                temp_raw_id = raw_id
                temp_times_contacted = times_contacted
                temp_starred = starred
                temp_has_phone_number = has_phone_number
                temp_contact_last_updated_timestamp = contact_last_updated_timestampion
            }
            if (contact_id == temp_contact_id) {

            } else {
                contactBean.set_id(temp_contact_id)
                contactBean.setLocal_version(temp_version.toString() + "")
                contactBean.setRaw_contact_id(temp_raw_id)
                contactBean.setTimes_contacted(temp_times_contacted)
                contactBean.setStarred(temp_starred)
                contactBean.setHas_phone_number(temp_has_phone_number)
                contactBean.setContact_last_updated_timestamp(temp_contact_last_updated_timestamp)
                contactBean.setName(name)
                contactBean.setPhoneList(phone_list)
                contactBean.setEmailList(email_list)
                contactBean.setOrganizetionList(organization_list)
                contactBean.setImList(im_list)
                contactBean.setNickname(nickname)
                contactBean.setPostalAddressList(postaladdress_list)
                contactBean.setWebsiteList(website_list)
                contactBean.setSipAddress(sipaddress)
                mContactIdAndVerHashtable!!.put(temp_contact_id, temp_version)
                //设置md5
                var phoneInfo = "-"
                if (contactBean.getPhoneList() != null) {
                    for (phoneBean in contactBean.getPhoneList()) {
                        phoneInfo = phoneInfo + phoneBean.getNumber()
                    }
                }
                var namePart = "-"
                if (contactBean.getName() != null) {
                    namePart = (namePart + contactBean.getName().getGiven_name()
                            + contactBean.getName().getFamily_name()
                            + contactBean.getName().getPrefix()
                            + contactBean.getName().getMiddle_name()
                            + contactBean.getName().getSuffix())
                }
                val md5 = GetMD5Utils.getMD5(namePart + phoneInfo)
                contactBean.setMd5(md5)
                mContactBeanList!!.add(contactBean)
                doneTime = System.currentTimeMillis()
                //LogUtils.d("getPhoneContactsData", " getPhoneContactsData One Time:"+(doneTime-initTime));

                temp_contact_id = contact_id
                temp_version = version
                temp_raw_id = raw_id
                temp_times_contacted = times_contacted
                temp_starred = starred
                temp_has_phone_number = has_phone_number
                temp_contact_last_updated_timestamp = contact_last_updated_timestampion
                contactBean = ContactBean()
                name = ContactNameBean()
                phone_list = ArrayList<ContactPhoneBean>()
                nickname = ContactNickNameBean()
                sipaddress = ContactSipAddressBean()
                email_list = ArrayList<ContactEmailBean>()
                organization_list = ArrayList<ContactOrganizationBean>()
                im_list = ArrayList<ContactImBean>()
                postaladdress_list = ArrayList<ContactPostalAddressBean>()
                website_list = ArrayList<ContactWebsiteBean>()
                initTime = System.currentTimeMillis()
            }
            val mimeType = contactsCursor.getString(MIMETYPE_INDEX)

            if (mimeType == ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) {
                name = ContactNameBean(
                        contactsCursor.getInt(CONTACT_DATA2_INDEX), //ContactsContract.CommonDataKinds.Phone.TYPE
                        contactsCursor.getString(CONTACT_DATA3_INDEX), //Phone.LABEL
                        contactsCursor.getString(CONTACT_DATA1_INDEX), //StructuredName.DISPLAY_NAME
                        contactsCursor.getString(CONTACT_DATA2_INDEX), //StructuredName.GIVEN_NAME
                        contactsCursor.getString(CONTACT_DATA3_INDEX), //StructuredName.FAMILY_NAME
                        contactsCursor.getString(CONTACT_DATA5_INDEX), //StructuredName.MIDDLE_NAME
                        contactsCursor.getString(CONTACT_DATA4_INDEX), //StructuredName.PREFIX
                        contactsCursor.getString(CONTACT_DATA6_INDEX), //StructuredName.SUFFIX
                        contactsCursor.getString(CONTACT_DATA7_INDEX), //StructuredName.PHONETIC_GIVEN_NAME
                        contactsCursor.getString(CONTACT_DATA8_INDEX), //StructuredName.PHONETIC_MIDDLE_NAME
                        contactsCursor.getString(CONTACT_DATA9_INDEX), //StructuredName.PHONETIC_FAMILY_NAME
                        contactsCursor.getString(CONTACT_DATA10_INDEX), //StructuredName.FULL_NAME_STYLE
                        contactsCursor.getString(CONTACT_DATA11_INDEX))//StructuredName.PHONETIC_NAME_STYLE

            } else if (mimeType == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
                phone_list.add(
                        ContactPhoneBean(
                                contactsCursor.getInt(CONTACT_DATA2_INDEX), //Phone.TYPE
                                contactsCursor.getString(CONTACT_DATA3_INDEX), //Phone.LABEL
                                contactsCursor.getString(CONTACT_DATA1_INDEX), //Phone.NUMBER
                                contactsCursor.getString(CONTACT_DATA4_INDEX))//Phone.NORMALIZED_NUMBER
                )

            } else if (mimeType == ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) {
                email_list.add(
                        ContactEmailBean(
                                contactsCursor.getInt(CONTACT_DATA2_INDEX), //Email.TYPE
                                contactsCursor.getString(CONTACT_DATA3_INDEX), //Email.LABEL
                                contactsCursor.getString(CONTACT_DATA1_INDEX), //Email.ADDRESS
                                contactsCursor.getString(CONTACT_DATA4_INDEX))//Email.DISPLAY_NAME
                )

            } else if (mimeType == ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE) {
                organization_list.add(
                        ContactOrganizationBean(
                                contactsCursor.getInt(CONTACT_DATA2_INDEX), //Organization.TYPE
                                contactsCursor.getString(CONTACT_DATA3_INDEX), //Organization.LABEL
                                contactsCursor.getString(CONTACT_DATA1_INDEX), //Organization.COMPANY
                                contactsCursor.getString(CONTACT_DATA4_INDEX), //Organization.TITLE
                                contactsCursor.getString(CONTACT_DATA5_INDEX), //Organization.DEPARTMENT
                                contactsCursor.getString(CONTACT_DATA6_INDEX), //Organization.JOB_DESCRIPTION
                                contactsCursor.getString(CONTACT_DATA7_INDEX), //Organization.SYMBOL
                                contactsCursor.getString(CONTACT_DATA8_INDEX), //Organization.PHONETIC_NAME
                                contactsCursor.getString(CONTACT_DATA9_INDEX), //Organization.OFFICE_LOCATION
                                contactsCursor.getString(CONTACT_DATA10_INDEX))//Organization.PHONETIC_NAME_STYLE
                )
                //
            } else if (mimeType == ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE) {
                im_list.add(
                        ContactImBean(
                                contactsCursor.getString(CONTACT_DATA1_INDEX), //Im.DATA
                                contactsCursor.getInt(CONTACT_DATA2_INDEX), //Im.TYPE
                                contactsCursor.getString(CONTACT_DATA3_INDEX), //Im.LABEL
                                contactsCursor.getString(CONTACT_DATA5_INDEX), //Im.PROTOCOL
                                contactsCursor.getString(CONTACT_DATA6_INDEX))//Im.CUSTOM_PROTOCOL
                )
                //
            } else if (mimeType == ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE) {
                nickname = ContactNickNameBean(
                        contactsCursor.getInt(CONTACT_DATA2_INDEX), //Nickname.TYPE
                        contactsCursor.getString(CONTACT_DATA3_INDEX), //Nickname.LABEL
                        contactsCursor.getString(CONTACT_DATA1_INDEX)//Nickname.NAME
                )

            } else if (mimeType == ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE) {
                postaladdress_list.add(
                        ContactPostalAddressBean(
                                contactsCursor.getInt(CONTACT_DATA2_INDEX), //StructuredPostal.LABEL
                                contactsCursor.getString(CONTACT_DATA3_INDEX), //StructuredPostal.LABEL
                                contactsCursor.getString(CONTACT_DATA1_INDEX), //StructuredPostal.FORMATTED_ADDRESS
                                contactsCursor.getString(CONTACT_DATA4_INDEX), //StructuredPostal.STREET
                                contactsCursor.getString(CONTACT_DATA5_INDEX), //StructuredPostal.POBOX
                                contactsCursor.getString(CONTACT_DATA6_INDEX), //StructuredPostal.NEIGHBORHOOD
                                contactsCursor.getString(CONTACT_DATA7_INDEX), //StructuredPostal.CITY
                                contactsCursor.getString(CONTACT_DATA8_INDEX), //StructuredPostal.REGION
                                contactsCursor.getString(CONTACT_DATA9_INDEX), //StructuredPostal.POSTCODE
                                contactsCursor.getString(CONTACT_DATA10_INDEX))//StructuredPostal.COUNTRY
                )
                //
            } else if (mimeType == ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE) {
                website_list.add(
                        ContactWebsiteBean(
                                contactsCursor.getInt(CONTACT_DATA2_INDEX), //Website.TYPE
                                contactsCursor.getString(CONTACT_DATA3_INDEX), //Website.LABEL
                                contactsCursor.getString(CONTACT_DATA1_INDEX))//Website.URL
                )

            } else if (mimeType == ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE) {
                sipaddress = ContactSipAddressBean(
                        contactsCursor.getInt(CONTACT_DATA2_INDEX), //SipAddress.TYPE
                        contactsCursor.getString(CONTACT_DATA3_INDEX), //SipAddress.LABEL
                        contactsCursor.getString(CONTACT_DATA1_INDEX)//SipAddress.SIP_ADDRESS
                )

            }

            if (contactsCursor.isLast) {
                contactBean.set_id(temp_contact_id)
                contactBean.setLocal_version(temp_version.toString() + "")
                contactBean.setRaw_contact_id(temp_raw_id)
                contactBean.setTimes_contacted(temp_times_contacted)
                contactBean.setStarred(temp_starred)
                contactBean.setHas_phone_number(temp_has_phone_number)
                contactBean.setContact_last_updated_timestamp(temp_contact_last_updated_timestamp)
                contactBean.setName(name)
                contactBean.setPhoneList(phone_list)
                contactBean.setEmailList(email_list)
                contactBean.setOrganizetionList(organization_list)
                contactBean.setImList(im_list)
                contactBean.setNickname(nickname)
                contactBean.setPostalAddressList(postaladdress_list)
                contactBean.setWebsiteList(website_list)
                contactBean.setSipAddress(sipaddress)
                mContactIdAndVerHashtable!!.put(temp_contact_id, temp_version)
                //设置md5
                var phoneInfo = "-"
                if (contactBean.getPhoneList() != null) {
                    for (phoneBean in contactBean.getPhoneList()) {
                        phoneInfo = phoneInfo + phoneBean.getNumber()
                    }
                }
                var namePart = "-"
                if (contactBean.getName() != null) {
                    namePart = (namePart + contactBean.getName().getGiven_name()
                            + contactBean.getName().getFamily_name()
                            + contactBean.getName().getPrefix()
                            + contactBean.getName().getMiddle_name()
                            + contactBean.getName().getSuffix())
                }
                val md5 = GetMD5Utils.getMD5(namePart + phoneInfo)
                contactBean.setMd5(md5)
                mContactBeanList!!.add(contactBean)
                doneTime = System.currentTimeMillis()
                //LogUtils.d("getPhoneContactsData", " getPhoneContactsData One Time:"+(doneTime-initTime));
            }
        }

        alldoneTime = System.currentTimeMillis()
        Factory.get().getCacheStatisticsManager().setLocal_current_count(mContactIdAndVerHashtable!!.size)
        Factory.get().getApplicationPrefsManager().setLocalCurrentContactCount(mContactIdAndVerHashtable!!.size)
        LogUtils.d("getPhoneContactsData", " getPhoneContactsData All Time:" + (alldoneTime - allinitTime))
    }

    companion object {

        val TAG = ReadPhoneContactModel::class.java.simpleName

        //  联系人表的uri
        private val contactsUri = ContactsContract.Contacts.CONTENT_URI

        //联系人集合
        val phoneContactCounts: Int
            get() {
                var count = 0
                try {
                    if (ContextCompat.checkSelfPermission(Factory.get().getApplicationContext(), PermissionUtils.PERMISSION_READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        val contactsCursor = Factory.get().getApplicationContext().getContentResolver().query(contactsUri, null, null, null, null)
                        count = contactsCursor!!.getCount()
                        LogUtils.d(TAG, " getPhoneContactsData count : " + count)
                    }
                } catch (e: Exception) {
                    LogUtils.d(TAG, " getPhoneContactsData can't read data : " + e.message.toString())
                }

                return count
            }


        val selectContactColumms = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.RawContacts.VERSION, ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1, ContactsContract.Data.DATA2, ContactsContract.Data.DATA3, ContactsContract.Data.DATA4, ContactsContract.Data.DATA5, ContactsContract.Data.DATA6, ContactsContract.Data.DATA7, ContactsContract.Data.DATA8, ContactsContract.Data.DATA9, ContactsContract.Data.DATA10, ContactsContract.Data.DATA11, ContactsContract.Data.DATA12, ContactsContract.Data.DATA13, ContactsContract.Data.DATA14, ContactsContract.Data.DATA15, ContactsContract.Contacts.NAME_RAW_CONTACT_ID, ContactsContract.Contacts.TIMES_CONTACTED, ContactsContract.Contacts.STARRED, ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)


        val CONTACT_ID_INDEX = 0
        val VERSION_INDEX = 1
        val MIMETYPE_INDEX = 2
        val CONTACT_DATA1_INDEX = 3
        val CONTACT_DATA2_INDEX = 4
        val CONTACT_DATA3_INDEX = 5
        val CONTACT_DATA4_INDEX = 6
        val CONTACT_DATA5_INDEX = 7
        val CONTACT_DATA6_INDEX = 8
        val CONTACT_DATA7_INDEX = 9
        val CONTACT_DATA8_INDEX = 10
        val CONTACT_DATA9_INDEX = 11
        val CONTACT_DATA10_INDEX = 12
        val CONTACT_DATA11_INDEX = 13
        val CONTACT_DATA12_INDEX = 14
        val CONTACT_DATA13_INDEX = 15
        val CONTACT_DATA14_INDEX = 16
        val CONTACT_DATA15_INDEX = 17
        val CONTACT_NAME_RAW_CONTACT_ID_INDEX = 18
        val CONTACT_TIMES_CONTACTED_INDEX = 19
        val CONTACT_STARRED_INDEX = 20
        val CONTACT_HAS_PHONE_NUMBER_INDEX = 21
        val CONTACT_LAST_UPDATED_TIMESTAMP_INDEX = 22
    }

}
