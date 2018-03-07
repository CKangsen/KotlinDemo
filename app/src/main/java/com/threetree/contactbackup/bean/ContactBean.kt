package com.threetree.contactbackup.bean


import com.threetree.contactbackup.util.IdPatternUtils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人信息类
 */

class ContactBean {

    /**
     * server端数据id (数据id+版本 合成)
     */
    var serverId: String? = null
    /**
     * 联系人id
     */
    var _id: Int = 0
    /**
     * 原生联系人id
     */
    var raw_contact_id: Int = 0
    /**
     * 联系次数
     */
    var times_contacted: Int = 0
    /**
     * 上一次联系时间
     */
    var last_time_contacted: Long = 0
        private set
    /**
     * 联系人是否加星标
     */
    var isStarred: Int = 0
    /**
     * 联系人是否至少有一个手机号
     */
    var isHas_phone_number: Int = 0
    /**
     * 联系人上一次更新时间
     */
    var contact_last_updated_timestamp: Long = 0
        private set

    /**
     * 联系人名称
     */
    var name: ContactNameBean? = null
    /**
     * 联系人昵称
     */
    var nickname: ContactNickNameBean? = null
    /**
     * Sip地址
     */
    var sipAddress: ContactSipAddressBean? = null
    /**
     * 邮箱列表
     */
    var emailList: List<ContactEmailBean>? = null
    /**
     * Im通讯工具列表
     */
    var imList: List<ContactImBean>? = null
    /**
     * 组织列表
     */
    var organizetionList: List<ContactOrganizationBean>? = null
    /**
     * 电话列表
     */
    var phoneList: List<ContactPhoneBean>? = null
    /**
     * （邮政）地址列表
     */
    var postalAddressList: List<ContactPostalAddressBean>? = null
    /**
     * 网址列表
     */
    var websiteList: List<ContactWebsiteBean>? = null

    /**
     * 本地数据版本
     */
    var local_version: String? = null
    /**
     * 服务器数据版本
     */
    var server_version: String? = null
    /**
     * 一条通讯录的MD5值
     */
    var md5: String? = null

    constructor(_id: Int, raw_contact_id: Int, times_contacted: Int, last_time_contacted: Long, starred: Int, has_phone_number: Int, contact_last_updated_timestamp: Long, name: ContactNameBean, nickname: ContactNickNameBean, sipAddress: ContactSipAddressBean, emailList: List<ContactEmailBean>, imList: List<ContactImBean>, organizetionList: List<ContactOrganizationBean>, phoneList: List<ContactPhoneBean>, postalAddressList: List<ContactPostalAddressBean>, websiteList: List<ContactWebsiteBean>) {
        this._id = _id
        this.raw_contact_id = raw_contact_id
        this.times_contacted = times_contacted
        this.last_time_contacted = last_time_contacted
        this.isStarred = starred
        this.isHas_phone_number = has_phone_number
        this.contact_last_updated_timestamp = contact_last_updated_timestamp
        this.name = name
        this.nickname = nickname
        this.sipAddress = sipAddress
        this.emailList = emailList
        this.imList = imList
        this.organizetionList = organizetionList
        this.phoneList = phoneList
        this.postalAddressList = postalAddressList
        this.websiteList = websiteList
    }

    constructor() {}

    fun setLast_time_contacted(last_time_contacted: Int) {
        this.last_time_contacted = last_time_contacted.toLong()
    }

    fun setContact_last_updated_timestamp(contact_last_updated_timestamp: Int) {
        this.contact_last_updated_timestamp = contact_last_updated_timestamp.toLong()
    }


    fun toJsonString(): String {
        return toJson().toString()
    }

    fun toJson(): JSONObject {
        val contact_json = JSONObject()
        val data_json = JSONObject()

        var name_json = JSONObject()
        var nickname_json = JSONObject()
        var sipaddress_json = JSONObject()
        val phone_list_json = JSONArray()
        val email_list_json = JSONArray()
        val organization_list_json = JSONArray()
        val im_list_json = JSONArray()
        val postaladdress_list_json = JSONArray()
        val website_list_json = JSONArray()
        try {
            for (emailBean in emailList!!) {
                email_list_json.put(emailBean.toJson())
            }
            for (organizationBean in organizetionList!!) {
                organization_list_json.put(organizationBean.toJson())
            }
            for (imBean in imList!!) {
                im_list_json.put(imBean.toJson())
            }
            for (postalAddressBean in postalAddressList!!) {
                postaladdress_list_json.put(postalAddressBean.toJson())
            }
            for (websiteBean in websiteList!!) {
                website_list_json.put(websiteBean.toJson())
            }
            name_json = this.name!!.toJson()
            nickname_json = this.nickname!!.toJson()
            sipaddress_json = this.sipAddress!!.toJson()

            data_json.put(DATA_ID, this._id)
            data_json.put(DATA_RAW_CONTACT_ID, this.raw_contact_id)
            data_json.put(DATA_TIMES_CONTACTED, this.times_contacted)
            data_json.put(DATA_LAST_TIME_CONTACTED, this.last_time_contacted)
            data_json.put(DATA_STARRED, this.isStarred)
            data_json.put(DATA_HAS_PHONE_NUMBER, this.isHas_phone_number)
            data_json.put(DATA_CONTACT_LAST_UPDATED_TIMESTAMP, this.contact_last_updated_timestamp)
            data_json.put(DATA_NAME, name_json)
            data_json.put(DATA_NICKNAME, nickname_json)
            data_json.put(DATA_SIPADDRESS, sipaddress_json)
            data_json.put(DATA_EMAIL_LIST, email_list_json)
            data_json.put(DATA_ORGANIZETION_LIST, organization_list_json)
            data_json.put(DATA_IM_LIST, im_list_json)
            data_json.put(DATA_POSTALADDRESS_LIST, postaladdress_list_json)
            data_json.put(DATA_WEBSITE_LIST, website_list_json)

            contact_json.put(GIVEN_NAME, this.name!!.getGiven_name())
            contact_json.put(FMAILY_NAME, this.name!!.getFamily_name())
            contact_json.put(PREFIX_NAME, this.name!!.getPrefix())
            contact_json.put(MIDDLE_NAME, this.name!!.getMiddle_name())
            contact_json.put(SUFFIX_NAME, this.name!!.getSuffix())
            for (phoneBean in phoneList!!) {
                phone_list_json.put(phoneBean.toJson())
            }
            contact_json.put(PHONE_NUMBERS, phone_list_json)
            contact_json.put(DATA, data_json.toString())


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return contact_json
    }

    fun toJsonStringWithServerId(): String {

        return toJsonWithServerId().toString()
    }

    fun toJsonWithServerId(): JSONObject {
        val contact_json = JSONObject()
        val data_json = JSONObject()

        var name_json = JSONObject()
        var nickname_json = JSONObject()
        var sipaddress_json = JSONObject()
        val phone_list_json = JSONArray()
        val email_list_json = JSONArray()
        val organization_list_json = JSONArray()
        val im_list_json = JSONArray()
        val postaladdress_list_json = JSONArray()
        val website_list_json = JSONArray()
        try {
            for (emailBean in emailList!!) {
                email_list_json.put(emailBean.toJson())
            }
            for (organizationBean in organizetionList!!) {
                organization_list_json.put(organizationBean.toJson())
            }
            for (imBean in imList!!) {
                im_list_json.put(imBean.toJson())
            }
            for (postalAddressBean in postalAddressList!!) {
                postaladdress_list_json.put(postalAddressBean.toJson())
            }
            for (websiteBean in websiteList!!) {
                website_list_json.put(websiteBean.toJson())
            }
            name_json = this.name!!.toJson()
            nickname_json = this.nickname!!.toJson()
            sipaddress_json = this.sipAddress!!.toJson()

            data_json.put(DATA_ID, this._id)
            data_json.put(DATA_RAW_CONTACT_ID, this.raw_contact_id)
            data_json.put(DATA_TIMES_CONTACTED, this.times_contacted)
            data_json.put(DATA_LAST_TIME_CONTACTED, this.last_time_contacted)
            data_json.put(DATA_STARRED, this.isStarred)
            data_json.put(DATA_HAS_PHONE_NUMBER, this.isHas_phone_number)
            data_json.put(DATA_CONTACT_LAST_UPDATED_TIMESTAMP, this.contact_last_updated_timestamp)
            data_json.put(DATA_NAME, name_json)
            data_json.put(DATA_NICKNAME, nickname_json)
            data_json.put(DATA_SIPADDRESS, sipaddress_json)
            data_json.put(DATA_EMAIL_LIST, email_list_json)
            data_json.put(DATA_ORGANIZETION_LIST, organization_list_json)
            data_json.put(DATA_IM_LIST, im_list_json)
            data_json.put(DATA_POSTALADDRESS_LIST, postaladdress_list_json)
            data_json.put(DATA_WEBSITE_LIST, website_list_json)

            contact_json.put(ID, IdPatternUtils.formatServerId(this.serverId, Integer.parseInt(this.server_version)))
            contact_json.put(GIVEN_NAME, this.name!!.getGiven_name())
            contact_json.put(FMAILY_NAME, this.name!!.getFamily_name())
            contact_json.put(PREFIX_NAME, this.name!!.getPrefix())
            contact_json.put(MIDDLE_NAME, this.name!!.getMiddle_name())
            contact_json.put(SUFFIX_NAME, this.name!!.getSuffix())
            for (phoneBean in phoneList!!) {
                phone_list_json.put(phoneBean.toJson())
            }
            contact_json.put(PHONE_NUMBERS, phone_list_json)
            contact_json.put(DATA, data_json.toString())


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return contact_json
    }

    companion object {


        val ID = "id" //server id
        val GIVEN_NAME = "given_name"
        val FMAILY_NAME = "fmaily_name"
        val PREFIX_NAME = "prefix_name"
        val MIDDLE_NAME = "middle_name"
        val SUFFIX_NAME = "suffix_name"
        val PHONE_NUMBERS = "phone_numbers"

        val DATA = "data"
        val DATA_ID = "_id"
        val DATA_RAW_CONTACT_ID = "raw_contact_id"
        val DATA_TIMES_CONTACTED = "times_contacted"
        val DATA_LAST_TIME_CONTACTED = "last_time_contacted"
        val DATA_STARRED = "starred"
        val DATA_HAS_PHONE_NUMBER = "has_phone_number"
        val DATA_CONTACT_LAST_UPDATED_TIMESTAMP = "contact_last_updated_timestamp"
        val DATA_NAME = "name"
        val DATA_NICKNAME = "nickname"
        val DATA_SIPADDRESS = "sipAddress"
        val DATA_EMAIL_LIST = "emailList"
        val DATA_IM_LIST = "imList"
        val DATA_ORGANIZETION_LIST = "organizetionList"
        val DATA_PHONE_LIST = "phoneList"
        val DATA_POSTALADDRESS_LIST = "postalAddressList"
        val DATA_WEBSITE_LIST = "websiteList"
    }
}
