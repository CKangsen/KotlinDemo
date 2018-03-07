package com.threetree.contactbackup.util

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

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.UnsupportedEncodingException
import java.util.ArrayList

import com.threetree.contactbackup.bean.ContactBaseBean.LABEL
import com.threetree.contactbackup.bean.ContactBaseBean.TYPE
import com.threetree.contactbackup.bean.ContactBaseBeanIncludeData.DATA
import com.threetree.contactbackup.bean.ContactBean.DATA_CONTACT_LAST_UPDATED_TIMESTAMP
import com.threetree.contactbackup.bean.ContactBean.DATA_HAS_PHONE_NUMBER
import com.threetree.contactbackup.bean.ContactBean.DATA_LAST_TIME_CONTACTED
import com.threetree.contactbackup.bean.ContactBean.DATA_RAW_CONTACT_ID
import com.threetree.contactbackup.bean.ContactBean.DATA_STARRED
import com.threetree.contactbackup.bean.ContactBean.DATA_TIMES_CONTACTED
import com.threetree.contactbackup.bean.ContactEmailBean.ADDRESS
import com.threetree.contactbackup.bean.ContactEmailBean.DISPALY_NAME
import com.threetree.contactbackup.bean.ContactImBean.CUSTOM_PROTOCOL
import com.threetree.contactbackup.bean.ContactImBean.PROTOCOL
import com.threetree.contactbackup.bean.ContactNameBean.DISPLAY_NAME
import com.threetree.contactbackup.bean.ContactNameBean.FAMILY_NAME
import com.threetree.contactbackup.bean.ContactNameBean.FULL_NAME_STYLE
import com.threetree.contactbackup.bean.ContactNameBean.GIVEN_NAME
import com.threetree.contactbackup.bean.ContactNameBean.MIDDLE_NAME
import com.threetree.contactbackup.bean.ContactNameBean.PHONETIC_FAMILY_NAME
import com.threetree.contactbackup.bean.ContactNameBean.PHONETIC_GIVEN_NAME
import com.threetree.contactbackup.bean.ContactNameBean.PHONETIC_MIDDLE_NAME
import com.threetree.contactbackup.bean.ContactNameBean.PHONETIC_NAME_STYLE
import com.threetree.contactbackup.bean.ContactNameBean.PREFIX_NAME
import com.threetree.contactbackup.bean.ContactNameBean.SUFFIX_NAME
import com.threetree.contactbackup.bean.ContactNickNameBean.NAME
import com.threetree.contactbackup.bean.ContactOrganizationBean.COMPANY
import com.threetree.contactbackup.bean.ContactOrganizationBean.DEPARTMENT
import com.threetree.contactbackup.bean.ContactOrganizationBean.JOB_DESCRIPTION
import com.threetree.contactbackup.bean.ContactOrganizationBean.OFFICE_LOCATION
import com.threetree.contactbackup.bean.ContactOrganizationBean.PHONETIC_NAME
import com.threetree.contactbackup.bean.ContactOrganizationBean.SYMBOL
import com.threetree.contactbackup.bean.ContactOrganizationBean.TITLE
import com.threetree.contactbackup.bean.ContactPhoneBean.CUSTOM
import com.threetree.contactbackup.bean.ContactPhoneBean.NORMALIZED_NUMBER
import com.threetree.contactbackup.bean.ContactPhoneBean.NUMBER
import com.threetree.contactbackup.bean.ContactPhoneBean.NUMBER_TYPE
import com.threetree.contactbackup.bean.ContactPostalAddressBean.CITY
import com.threetree.contactbackup.bean.ContactPostalAddressBean.COUNTRY
import com.threetree.contactbackup.bean.ContactPostalAddressBean.FORMATTED_ADDRESS
import com.threetree.contactbackup.bean.ContactPostalAddressBean.NEIGHBORHOOD
import com.threetree.contactbackup.bean.ContactPostalAddressBean.POBOX
import com.threetree.contactbackup.bean.ContactPostalAddressBean.POSTCODE
import com.threetree.contactbackup.bean.ContactPostalAddressBean.REGION
import com.threetree.contactbackup.bean.ContactPostalAddressBean.STREET
import com.threetree.contactbackup.bean.ContactSipAddressBean.SIP_ADDRESS
import com.threetree.contactbackup.bean.ContactWebsiteBean.URL

/**
 * Created by hp on 2017/7/20.
 */

object JsonUtils {

    fun JsonToContactBean(ContactBeanJson: String): ContactBean {
        val mContactBean = ContactBean()
        try {
            val resoj = JSONObject(ContactBeanJson)
            val mContactNameBean = ContactNameBean()
            val mContactNickNameBean = ContactNickNameBean()
            if (resoj.has("data")) {
                val dataString = resoj.getString("data")
                val data = JSONObject(dataString)
                val dataname = data.getJSONObject("name")
                val nikename = data.getJSONObject("nickname")
                if (dataname.has(Companion.getDISPLAY_NAME())) {
                    mContactNameBean.setDisplay_name(dataname.getString(Companion.getDISPLAY_NAME()))
                }
                if (dataname.has(Companion.getFULL_NAME_STYLE())) {
                    mContactNameBean.setFull_name_style(dataname.getString(Companion.getFULL_NAME_STYLE()))
                }
                if (dataname.has(Companion.getPHONETIC_FAMILY_NAME())) {
                    mContactNameBean.setPhonetic_family_name(dataname.getString(Companion.getPHONETIC_FAMILY_NAME()))
                }
                if (dataname.has(Companion.getPHONETIC_GIVEN_NAME())) {
                    mContactNameBean.setPhonetic_given_name(dataname.getString(Companion.getPHONETIC_GIVEN_NAME()))
                }
                if (dataname.has(Companion.getPHONETIC_MIDDLE_NAME())) {
                    mContactNameBean.setPhonetic_middle_name(dataname.getString(Companion.getPHONETIC_MIDDLE_NAME()))
                }
                if (dataname.has(Companion.getPHONETIC_NAME_STYLE())) {
                    mContactNameBean.setPhonetic_name_style(dataname.getString(Companion.getPHONETIC_NAME_STYLE()))
                }
                if (nikename.has(Companion.getNAME())) {
                    mContactNickNameBean.setName(nikename.getString(Companion.getNAME()))
                }
                val emaillist = data.getJSONArray("emailList")
                val emailList = ArrayList<ContactEmailBean>()
                if (emaillist.length() > 0) {
                    for (j in 0 until emaillist.length()) {
                        val email = emaillist.getJSONObject(j)
                        val mContactEmailBean = ContactEmailBean()
                        try {
                            if (email.has(Companion.getADDRESS())) {
                                mContactEmailBean.setAddress(email.getString(Companion.getADDRESS()))
                            }
                            if (email.has(Companion.getDISPALY_NAME())) {
                                mContactEmailBean.setDispaly_name(email.getString(Companion.getDISPALY_NAME()))
                            }
                            if (email.has(Companion.getLABEL())) {
                                mContactEmailBean.setLabel(email.getString(Companion.getLABEL()))
                            }
                            if (email.has(Companion.getTYPE())) {
                                mContactEmailBean.setType(email.getInt(Companion.getTYPE()))
                            }
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        emailList.add(mContactEmailBean)
                    }
                }

                val organizetionList = data.getJSONArray("organizetionList")
                val OrganizationBeanList = ArrayList<ContactOrganizationBean>()
                if (organizetionList.length() > 0) {
                    for (k in 0 until organizetionList.length()) {
                        val organizetion = organizetionList.getJSONObject(k)
                        val mContactOrganizationBean = ContactOrganizationBean()
                        try {
                            if (organizetion.has(Companion.getPHONETIC_NAME_STYLE())) {
                                mContactOrganizationBean.setPhonetic_name_style(organizetion.getString(Companion.getPHONETIC_NAME_STYLE()))
                            }
                            if (organizetion.has(Companion.getPHONETIC_NAME())) {
                                mContactOrganizationBean.setPhonetic_name(organizetion.getString(Companion.getPHONETIC_NAME()))
                            }
                            if (organizetion.has(Companion.getLABEL())) {
                                mContactOrganizationBean.setLabel(organizetion.getString(Companion.getLABEL()))
                            }
                            if (organizetion.has(Companion.getTYPE())) {
                                mContactOrganizationBean.setType(organizetion.getInt(Companion.getTYPE()))
                            }
                            if (organizetion.has(Companion.getCOMPANY())) {
                                mContactOrganizationBean.setCompany(organizetion.getString(Companion.getCOMPANY()))
                            }
                            if (organizetion.has(Companion.getDEPARTMENT())) {
                                mContactOrganizationBean.setDepartment(organizetion.getString(Companion.getDEPARTMENT()))
                            }
                            if (organizetion.has(Companion.getJOB_DESCRIPTION())) {
                                mContactOrganizationBean.setJob_description(organizetion.getString(Companion.getJOB_DESCRIPTION()))
                            }
                            if (organizetion.has(Companion.getOFFICE_LOCATION())) {
                                mContactOrganizationBean.setOffice_location(organizetion.getString(Companion.getOFFICE_LOCATION()))
                            }
                            if (organizetion.has(Companion.getSYMBOL())) {
                                mContactOrganizationBean.setSymbol(organizetion.getString(Companion.getSYMBOL()))
                            }
                            if (organizetion.has(Companion.getTITLE())) {
                                mContactOrganizationBean.setTitle(organizetion.getString(Companion.getTITLE()))
                            }
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        OrganizationBeanList.add(mContactOrganizationBean)
                    }
                }


                val imList = data.getJSONArray("imList")
                val Listim = ArrayList<ContactImBean>()
                if (imList.length() > 0) {
                    for (l in 0 until imList.length()) {
                        val im = imList.getJSONObject(l)
                        val mContactImBean = ContactImBean()
                        try {
                            if (im.has(Companion.getCUSTOM_PROTOCOL())) {
                                mContactImBean.setCustom_protocol(im.getString(Companion.getCUSTOM_PROTOCOL()))
                            }
                            if (im.has(Companion.getPROTOCOL())) {
                                mContactImBean.setProtocol(im.getString(Companion.getPROTOCOL()))
                            }
                            if (im.has(Companion.getLABEL())) {
                                mContactImBean.setLabel(im.getString(Companion.getLABEL()))
                            }
                            if (im.has(Companion.getTYPE())) {
                                mContactImBean.setType(im.getInt(Companion.getTYPE()))
                            }
                            if (im.has(Companion.getDATA())) {
                                mContactImBean.setData(im.getString(Companion.getDATA()))
                            }
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        Listim.add(mContactImBean)
                    }
                }

                val postalAddressList = data.getJSONArray("postalAddressList")
                val ListpostalAddress = ArrayList<ContactPostalAddressBean>()
                if (postalAddressList.length() > 0) {
                    for (n in 0 until postalAddressList.length()) {
                        val postalAddress = postalAddressList.getJSONObject(n)
                        val mContactPostalAddressBean = ContactPostalAddressBean()
                        try {
                            if (postalAddress.has(Companion.getCITY())) {
                                mContactPostalAddressBean.setCity(postalAddress.getString(Companion.getCITY()))
                            }
                            if (postalAddress.has(Companion.getCOUNTRY())) {
                                mContactPostalAddressBean.setCountry(postalAddress.getString(Companion.getCOUNTRY()))
                            }
                            if (postalAddress.has(Companion.getLABEL())) {
                                mContactPostalAddressBean.setLabel(postalAddress.getString(Companion.getLABEL()))
                            }
                            if (postalAddress.has(Companion.getTYPE())) {
                                mContactPostalAddressBean.setType(postalAddress.getInt(Companion.getTYPE()))
                            }
                            if (postalAddress.has(Companion.getFORMATTED_ADDRESS())) {
                                mContactPostalAddressBean.setFormatted_address(postalAddress.getString(Companion.getFORMATTED_ADDRESS()))
                            }
                            if (postalAddress.has(Companion.getNEIGHBORHOOD())) {
                                mContactPostalAddressBean.setNeighborhood(postalAddress.getString(Companion.getNEIGHBORHOOD()))
                            }
                            if (postalAddress.has(Companion.getPOBOX())) {
                                mContactPostalAddressBean.setPobox(postalAddress.getString(Companion.getPOBOX()))
                            }
                            if (postalAddress.has(Companion.getPOSTCODE())) {
                                mContactPostalAddressBean.setPostcode(postalAddress.getString(Companion.getPOSTCODE()))
                            }
                            if (postalAddress.has(Companion.getREGION())) {
                                mContactPostalAddressBean.setRegion(postalAddress.getString(Companion.getREGION()))
                            }
                            if (postalAddress.has(Companion.getSTREET())) {
                                mContactPostalAddressBean.setStreet(postalAddress.getString(Companion.getSTREET()))
                            }
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        ListpostalAddress.add(mContactPostalAddressBean)
                    }
                }

                val websiteList = data.getJSONArray("websiteList")
                val Listwebsite = ArrayList<ContactWebsiteBean>()
                if (websiteList.length() > 0) {
                    for (p in 0 until websiteList.length()) {
                        val website = websiteList.getJSONObject(p)
                        val mContactWebsiteBean = ContactWebsiteBean()
                        try {
                            if (website.has(Companion.getURL())) {
                                mContactWebsiteBean.setUrl(website.getString(Companion.getURL()))
                            }
                            if (website.has(Companion.getTYPE())) {
                                mContactWebsiteBean.setType(website.getInt(Companion.getTYPE()))
                            }
                            if (website.has(Companion.getLABEL())) {
                                mContactWebsiteBean.setLabel(website.getString(Companion.getLABEL()))
                            }
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        Listwebsite.add(mContactWebsiteBean)
                    }
                }
                val sipAddress = data.getJSONObject("sipAddress")
                val mContactSipAddressBean = ContactSipAddressBean()
                try {
                    if (sipAddress.has(Companion.getSIP_ADDRESS())) {
                        mContactSipAddressBean.setSip_address(sipAddress.getString(Companion.getSIP_ADDRESS()))
                    }
                    if (sipAddress.has(Companion.getLABEL())) {
                        mContactSipAddressBean.setLabel(sipAddress.getString(Companion.getLABEL()))
                    }
                    if (sipAddress.has(Companion.getTYPE())) {
                        mContactSipAddressBean.setType(sipAddress.getInt(Companion.getTYPE()))
                    }
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                }

                mContactBean.setContact_last_updated_timestamp(data.getInt(Companion.getDATA_CONTACT_LAST_UPDATED_TIMESTAMP()))

                mContactBean.setLast_time_contacted(data.getInt(Companion.getDATA_LAST_TIME_CONTACTED()))
                mContactBean.setRaw_contact_id(data.getInt(Companion.getDATA_RAW_CONTACT_ID()))
                mContactBean.setTimes_contacted(data.getInt(Companion.getDATA_TIMES_CONTACTED()))
                mContactBean.setHas_phone_number(data.getInt(Companion.getDATA_HAS_PHONE_NUMBER()))
                mContactBean.setStarred(data.getInt(Companion.getDATA_STARRED()))
                mContactBean.setImList(Listim)
                mContactBean.setEmailList(emailList)
                mContactBean.setSipAddress(mContactSipAddressBean)
                mContactBean.setOrganizetionList(OrganizationBeanList)
                mContactBean.setPostalAddressList(ListpostalAddress)
                mContactBean.setWebsiteList(Listwebsite)

            }


            val id_v = resoj.getString("id")
            val id = id_v.substring(0, id_v.indexOf("-"))
            val version = id_v.substring(id_v.indexOf("-") + 1)

            mContactBean.setServerId(id)
            mContactBean.setServer_version(version)
            if (resoj.has(Companion.getGIVEN_NAME())) {
                mContactNameBean.setGiven_name(resoj.getString(Companion.getGIVEN_NAME()))
            }
            if (resoj.has(Companion.getFAMILY_NAME())) {
                mContactNameBean.setFamily_name(resoj.getString(Companion.getFAMILY_NAME()))
            }
            if (resoj.has(Companion.getPREFIX_NAME())) {
                mContactNameBean.setPrefix(resoj.getString(Companion.getPREFIX_NAME()))
            }
            if (resoj.has(Companion.getMIDDLE_NAME())) {
                mContactNameBean.setMiddle_name(resoj.getString(Companion.getMIDDLE_NAME()))
            }
            if (resoj.has(Companion.getSUFFIX_NAME())) {
                mContactNameBean.setSuffix(resoj.getString(Companion.getSUFFIX_NAME()))
            }

            if (resoj.has("phone_numbers")) {
                val phoneList = resoj.getJSONArray("phone_numbers")
                val Listphone = ArrayList<ContactPhoneBean>()
                if (phoneList.length() > 0) {
                    for (m in 0 until phoneList.length()) {
                        val phone = phoneList.getJSONObject(m)
                        val mContactPhoneBean = ContactPhoneBean()
                        try {
                            if (phone.has(Companion.getNUMBER())) {
                                mContactPhoneBean.setNumber(phone.getString(Companion.getNUMBER()))
                            }
                            if (phone.has(Companion.getLABEL())) {
                                mContactPhoneBean.setLabel(phone.getString(Companion.getLABEL()))
                            }
                            if (phone.has(Companion.getNUMBER_TYPE())) {
                                mContactPhoneBean.setType(phone.getInt(Companion.getNUMBER_TYPE()))
                            }
                            /*CUSTOM 最后设置，因为设置CUSTOM同时会设置LABEL*/
                            if (phone.has(Companion.getCUSTOM())) {
                                mContactPhoneBean.setCustom(phone.getString(Companion.getCUSTOM()))
                            }
                            if (phone.has(Companion.getNORMALIZED_NUMBER())) {
                                mContactPhoneBean.setNormalized_number(phone.getString(Companion.getNORMALIZED_NUMBER()))
                            }
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        Listphone.add(mContactPhoneBean)
                    }
                }

                mContactBean.setPhoneList(Listphone)
            }
            mContactBean.setName(mContactNameBean)
            mContactBean.setNickname(mContactNickNameBean)


            //同步md5规则，与本地一致
            var phoneInfo = "-"
            if (mContactBean.getPhoneList() != null) {
                for (phoneBean in mContactBean.getPhoneList()) {
                    phoneInfo = phoneInfo + phoneBean.getNumber()
                }
            }
            var namePart = "-"
            if (mContactBean.getName() != null) {
                namePart = (namePart + mContactBean.getName().getGiven_name()
                        + mContactBean.getName().getFamily_name()
                        + mContactBean.getName().getPrefix()
                        + mContactBean.getName().getMiddle_name()
                        + mContactBean.getName().getSuffix())
            }
            val md5 = GetMD5Utils.getMD5(namePart + phoneInfo)
            mContactBean.setMd5(md5)
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }

        return mContactBean
    }

    fun JsonToSmsInfo(SmsInfoJson: String): SmsInfo {
        val mSmsInfo = SmsInfo()
        try {
            val resoj = JSONObject(SmsInfoJson)
            val dataString = resoj.getString("data")
            val data = JSONObject(dataString)
            mSmsInfo.setAddress(resoj.getString("number"))
            mSmsInfo.setServer_id(resoj.getString("id"))
            mSmsInfo.setDate(resoj.getString("timestamp"))
            mSmsInfo.setType(data.getString("type"))
            mSmsInfo.setBody(data.getString("body"))
        } catch (ex: JSONException) {
            ex.printStackTrace()
        }

        return mSmsInfo
    }

    fun JsonToCallLogBean(callLogJson: String): CallLogBean {
        val mCallLogBean = CallLogBean()
        try {
            val resoj = JSONObject(callLogJson)
            val dataString = resoj.getString("data")
            val data = JSONObject(dataString)
            mCallLogBean.setNumber(resoj.getString("number"))
            if (resoj.has("id")) {
                val id_v = resoj.getString("id")
                mCallLogBean.setServerid(id_v)
            }
            mCallLogBean.setDate(resoj.getString("timestamp"))

            if (data.has("type")) {
                mCallLogBean.setType(data.getInt("type"))
            }
            if (data.has("presentation")) {
                mCallLogBean.setPresentation(data.getInt("presentation"))
            }
            if (data.has("duration")) {
                mCallLogBean.setDuration(data.getString("duration"))
            }
            if (data.has("data_usage")) {
                //mCallLogBean.setData_usage(data.getString("data_usage"));
            }
            if (data.has("features")) {
                //mCallLogBean.setFeatures(data.getInt("features"));
            }
            if (data.has("phone_account_address")) {
                //mCallLogBean.setPhone_account_address(data.getString("phone_account_address"));
            }
            if (data.has("cachename")) {
                mCallLogBean.setCachename(data.getString("cachename"))
            }
            if (data.has("cachenumbertype")) {
                mCallLogBean.setCachenumbertype(data.getString("cachenumbertype"))
            }
            if (data.has("cachenumberlabel")) {
                mCallLogBean.setCachenumberlabel(data.getString("cachenumberlabel"))
            }
            if (data.has("countryiso")) {
                mCallLogBean.setCountryiso(data.getString("countryiso"))
            }
            if (data.has("is_read")) {
                mCallLogBean.setIs_read(data.getInt("is_read"))
            }
            if (data.has("geocoded_location")) {
                mCallLogBean.setGeocoded_location(data.getString("geocoded_location"))
            }

        } catch (ex: JSONException) {
            ex.printStackTrace()
        }

        return mCallLogBean
    }

    fun SMSCreateUpLoadJson(uploadSmsInfoList: List<SmsInfo>): String {
        val uploadJson = JSONObject()
        val sms_records = JSONArray()
        for (i in uploadSmsInfoList.indices) {
            val smsinfo = uploadSmsInfoList[i]
            val data_json = JSONObject()
            val sms_json = JSONObject()
            try {
                sms_json.put("id", GetMD5Utils.getMD5(smsinfo.getAddress()
                        + smsinfo.getDate() + smsinfo.getType()))
                sms_json.put("number", smsinfo.getAddress())
                sms_json.put("timestamp", java.lang.Long.valueOf(smsinfo.getDate()).toLong())

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            try {
                data_json.put("body", toUtf8(smsinfo.getBody()))
                data_json.put("type", smsinfo.getType())
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            try {
                sms_json.put("data", data_json.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            sms_records.put(sms_json)
        }
        try {
            uploadJson.put("openid", ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            uploadJson.put("records", sms_records)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return uploadJson.toString()
    }

    fun CallLogCreateUpLoadJson(uploadCallLogList: List<CallLogBean>): String {
        val uploadJson = JSONObject()
        val callLog_records = JSONArray()
        for (i in uploadCallLogList.indices) {
            val callLogBean = uploadCallLogList[i]
            val callLog_json = callLogBean.toJson()

            callLog_records.put(callLog_json)
        }
        try {
            uploadJson.put("openid", ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            uploadJson.put("records", callLog_records)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return uploadJson.toString()
    }

    fun ContactCreateUpLoadJson(addContactList: List<ContactBean>?, updateContactList: List<ContactBean>?,
                                deleteIDMap: List<IDMappingValue>?,
                                deleteIDMapNeedUpdateV: List<IDMappingValue>?,
                                rollbackIDList: List<String>?): String {
        val modify_json = JSONObject()

        val main_json = JSONObject()

        val add_array = JSONArray()
        val update_array = JSONArray()
        val delete_array = JSONArray()
        val rollback_array = JSONArray()

        val localMapValueForAddContact = ArrayList<IDMappingValue>()
        val localMapValueForUpdateContact = ArrayList<IDMappingValue>()
        val localMapValueForDeleteContact = ArrayList<IDMappingValue>()
        val localMapValueForDeleteContactNeedUpdateV = ArrayList<IDMappingValue>()

        if (addContactList != null && addContactList.size > 0) {
            for (contactBean in addContactList) {
                localMapValueForAddContact.add(IDMappingValue(contactBean.get_id(),
                        Integer.parseInt(contactBean.getLocal_version()), null,
                        0,
                        contactBean.getMd5()))
                add_array.put(contactBean.toJson())
            }
        }
        if (updateContactList != null && updateContactList.size > 0) {
            for (contactBean in updateContactList) {
                localMapValueForUpdateContact.add(IDMappingValue(contactBean.get_id(),
                        Integer.parseInt(contactBean.getLocal_version()),
                        contactBean.getServerId(),
                        Integer.parseInt(contactBean.getServer_version()),
                        contactBean.getMd5()))
                update_array.put(contactBean.toJsonWithServerId())
            }
        }
        if (deleteIDMap != null && deleteIDMap.size > 0) {
            for (idMappingValue in deleteIDMap) {
                localMapValueForDeleteContact.add(idMappingValue)
                val tempID = IdPatternUtils.formatServerId(idMappingValue.getServerid(), idMappingValue.getServerV())
                val temp = JSONObject()
                try {
                    temp.put("id", tempID)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                delete_array.put(temp)
            }
        }
        if (deleteIDMapNeedUpdateV != null && deleteIDMapNeedUpdateV.size > 0) {
            for (idMappingValue in deleteIDMapNeedUpdateV) {
                localMapValueForDeleteContactNeedUpdateV.add(idMappingValue)
                val tempID = IdPatternUtils.formatServerId(idMappingValue.getServerid(), idMappingValue.getServerLatestV())
                val temp = JSONObject()
                try {
                    temp.put("id", tempID)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                delete_array.put(temp)
            }
        }

        if (rollbackIDList != null && rollbackIDList.size > 0) {
            for (tempID in rollbackIDList) {
                val temp = JSONObject()
                try {
                    temp.put("id", tempID)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                rollback_array.put(temp)
            }
        }


        //设置本地新增上传到服务器的id(IDMappingValue) 集合
        Factory.get().getCacheStatisticsManager().setLocalMapValueForAddContact(localMapValueForAddContact)
        //设置本地修改上传到服务器的id(IDMappingValue) 集合
        Factory.get().getCacheStatisticsManager().setLocalMapValueForUpdateContact(localMapValueForUpdateContact)
        //设置本地删除上传到服务器的id(IDMappingValue) 集合
        Factory.get().getCacheStatisticsManager().setLocalMapValueForDeleteContact(localMapValueForDeleteContact)
        //设置本地删除上传到服务器的id(IDMappingValue) 集合
        Factory.get().getCacheStatisticsManager().setLocalMapValueForDeleteContactNeedUpdateV(localMapValueForDeleteContactNeedUpdateV)

        try {
            main_json.put("add", add_array)
            main_json.put("update", update_array)
            main_json.put("delete", delete_array)
            main_json.put("rollback", rollback_array)

            modify_json.put("openid", ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).getOpenid())
            modify_json.put("v", Factory.get().getCacheStatisticsManager().getServer_big_version())
            modify_json.put("modify", main_json)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        LogUtils.d("ContactCreateUpLoadJson", modify_json.toString())

        return if ((addContactList == null || addContactList.size == 0)
                && (updateContactList == null || updateContactList.size == 0)
                && (deleteIDMap == null || deleteIDMap.size == 0)
                && (deleteIDMapNeedUpdateV == null || deleteIDMapNeedUpdateV.size == 0)
                && (rollbackIDList == null || rollbackIDList.size == 0)) {
            ""
        } else modify_json.toString()

    }

    fun toUtf8(str: String): String? {
        var result: String? = null
        try {
            result = String(str.toByteArray(charset("UTF-8")), "UTF-8")
        } catch (e: UnsupportedEncodingException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        return result
    }

}
