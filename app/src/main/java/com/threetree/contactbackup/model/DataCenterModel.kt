package com.threetree.contactbackup.model

import android.text.TextUtils

import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.CallLogBean
import com.threetree.contactbackup.bean.ContactBean
import com.threetree.contactbackup.bean.SmsInfo
import com.threetree.contactbackup.midcore.Consts
import com.threetree.contactbackup.midcore.WKCInstance
import com.threetree.contactbackup.midcore.listener.HttpResultListener
import com.threetree.contactbackup.midcore.param.LoginParam
import com.threetree.contactbackup.model.listener.DataCenterListener
import com.threetree.contactbackup.util.JsonUtils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.HashMap
import java.util.Hashtable
import java.util.UUID


class DataCenterModel : IDataCenterModel, HttpResultListener {

    internal var mTudcCenter = WKCInstance.getDefaultInstance()

    private val mListenerMap: HashMap<String, Any>

    init {
        mListenerMap = HashMap()
    }


    fun lognin(st: String, onlogninResponListener: DataCenterListener.OnlogninResponListener) {
        val param = LoginParam()
        param.st = st
        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, onlogninResponListener)
        mTudcCenter.AfLogin(param, uuid, uuid, this)
    }

    fun logout(onlogOutResponListener: DataCenterListener.OnlogOutResponListener) {
        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, onlogOutResponListener)
        mTudcCenter.AfLogout(uuid, this)
        onlogOutResponListener.logOutSuccess()
    }

    fun getContact(version: Int, ongetContactResponListener: DataCenterListener.OngetContactResponListener) {
        val v = JSONObject()
        try {
            v.put("v", version)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, ongetContactResponListener)
        mTudcCenter.AfRequestData(Consts.REQ_CONTACT_RECOVERY, uuid, null, v.toString(), uuid, this)
    }

    fun uploadContact(contactJson: String, onuploadContactResponListener: DataCenterListener.OnuploadContactResponListener) {

        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, onuploadContactResponListener)
        mTudcCenter.AfRequestData(Consts.REQ_CONTACT_MODIFY, uuid, contactJson, null, uuid, this)

    }

    fun getCallVersion(ongetCallVersionResponListener: DataCenterListener.OngetCallVersionResponListener) {
        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, ongetCallVersionResponListener)
        mTudcCenter.AfRequestData(Consts.REQ_GET_CALL_RECORD_DETAIL, uuid, null, null, uuid, this)
    }

    fun getCallRecords(startTs: Long, endTs: Long, ongetCallRecordResponListener: DataCenterListener.OngetCallRecordResponListener) {
        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, ongetCallRecordResponListener)
        val start = startTs.toString()
        val end = endTs.toString()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("start", start)
            jsonObject.put("end", end)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        mTudcCenter.AfRequestData(Consts.REQ_GET_CALL_RECORDS, uuid, null, jsonObject.toString(), uuid, this)
    }

    fun uploadCalls(callJson: String, onuploadCallResponListener: DataCenterListener.OnuploadCallResponListener) {
        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, onuploadCallResponListener)
        mTudcCenter.AfRequestData(Consts.REQ_CALL_RECORD_BACKUP, uuid, callJson, null, uuid, this)
    }

    fun getSMSVersion(ongetSMSVersionResponListener: DataCenterListener.OngetSMSVersionResponListener) {
        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, ongetSMSVersionResponListener)
        mTudcCenter.AfRequestData(Consts.REQ_SMS_RECORDS_DETAIL, uuid, null, null, uuid, this)
    }

    fun getSMSRecords(startTs: Long, endTs: Long, ongetSMSRecordResponListener: DataCenterListener.OngetSMSRecordResponListener) {
        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, ongetSMSRecordResponListener)
        val start = startTs.toString()
        val end = endTs.toString()
        val jsonObject = JSONObject()
        try {
            jsonObject.put("start", start)
            jsonObject.put("end", end)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        mTudcCenter.AfRequestData(Consts.REQ_SMS_RECORDS_RECORDS, uuid, null, jsonObject.toString(), uuid, this)
    }

    fun uploadSMS(smsJson: String, onuploadSMSResponListener: DataCenterListener.OnuploadSMSResponListener) {
        val uuid = UUID.randomUUID().toString()
        mListenerMap.put(uuid, onuploadSMSResponListener)
        mTudcCenter.AfRequestData(Consts.REQ_SMS_RECORDS_BACKUP, uuid, smsJson, null, uuid, this)
    }

    override fun OnResult(httpHandle: Int, flag: Int, code: Int, msg: String, http_code: Int, result: Any, user_data: Any?) {
        LogUtils.i(TAG, "DataCenterModel: TUDCOnResult httpHandle =" + httpHandle + " flag=" + flag + " code=" + code + " msg=" + msg
                + " result:" + result + " user_data:" + user_data)
        // TODO Auto-generated method stubl
        var uuId: String? = null
        if (user_data != null && user_data is String) {
            uuId = user_data
        }
        if (code == Consts.REQ_CODE_SUCCESS) {
            when (flag) {
                Consts.REQ_LONGIN -> if (!TextUtils.isEmpty(uuId)) {
                    val str = result as String
                    if (str != null) {
                        println("ok gp: OnResult httpHandle =$httpHandle flag=$flag code=$code")
                        try {
                            val resoj = JSONObject(result)
                            val openid = resoj.getLong("openid")
                            ApplicationPrefsManager.getInstance(Factory.get().getApplicationContext()).setOpenId(openid)
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        (mListenerMap[uuId] as DataCenterListener.OnlogninResponListener).logninSuccess()
                        mListenerMap.remove(uuId)

                    }

                }
            /*case Consts.REQ_LOGOUT:
                    if (!TextUtils.isEmpty(uuId)) {
                            ((DataCenterListener.OnlogOutResponListener) mListenerMap.get(uuId)).logOutSuccess();
                            mListenerMap.remove(uuId);
                    }
                    break;*/
                Consts.REQ_CONTACT_RECOVERY -> if (!TextUtils.isEmpty(uuId)) {
                    val str = result as String
                    //解析返回的联系人数据JSON
                    val serverList = ArrayList<ContactBean>()
                    val server_deleteList = ArrayList<ContactBean>()
                    val serverIdHashtable = Hashtable()
                    val serverAllDataHashtable = Hashtable()
                    var version = -1
                    if (str != null) {
                        val resoj: JSONObject
                        val Contact_list: JSONArray
                        try {
                            resoj = JSONObject(str.replace("\n", ""))
                            version = resoj.getInt("v")
                            Contact_list = resoj.getJSONArray("contacts")
                            for (i in 0 until Contact_list.length()) {
                                try {
                                    val mContactBean = JsonUtils.JsonToContactBean(Contact_list.getString(i))
                                    val contact = JSONObject(Contact_list.getString(i))
                                    if (contact.getBoolean("mark_deleted")) {
                                        //记录在云端删除表
                                        server_deleteList.add(mContactBean)
                                        //                                                Factory.get().getDBManager().insertInServerdelete(
                                        //                                                        IdPatternUtils.formatServerId(mContactBean.getServerId(),Integer.parseInt(mContactBean.getServer_version())),
                                        //                                                        mContactBean.getMd5(),Contact_list.getString(i));
                                    } else {
                                        serverList.add(mContactBean)
                                        serverIdHashtable.put(mContactBean.getServerId(), Integer.parseInt(mContactBean.getServer_version()))
                                        serverAllDataHashtable.put(mContactBean.getServerId(), mContactBean)
                                    }
                                } catch (ex: JSONException) {
                                    ex.printStackTrace()
                                    LogUtils.d(TAG, ex.message)
                                }

                            }
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                            LogUtils.d(TAG, ex.message)
                        }

                    }
                    (mListenerMap[uuId] as DataCenterListener.OngetContactResponListener).getContactSuccess(version, serverList, serverIdHashtable, serverAllDataHashtable, server_deleteList)
                    mListenerMap.remove(uuId)
                }
                Consts.REQ_CONTACT_MODIFY -> {
                    if (!TextUtils.isEmpty(uuId)) {
                        val str = result as String
                        if (str != null) {
                            val addIds = ArrayList<String>()
                            try {
                                val resoj = JSONObject(str.replace("\n", ""))
                                val addIds_json = resoj.getJSONArray("add_ids")
                                for (i in 0 until addIds_json.length()) {
                                    addIds.add(addIds_json.get(i) as String)
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                LogUtils.d(TAG, e.message)
                            }

                            (mListenerMap[uuId] as DataCenterListener.OnuploadContactResponListener).uploadContactSuccess(addIds)
                            mListenerMap.remove(uuId)
                        }
                    }
                    if (!TextUtils.isEmpty(uuId)) {
                        val str = result as String
                        var backts: Long = 0
                        var callts: Long = 0
                        if (str != null) {
                            println("ok gp: OnResult httpHandle =$httpHandle flag=$flag code=$code")
                            try {
                                val resoj = JSONObject(result)
                                backts = resoj.getLong("latestbackupts")
                                callts = resoj.getLong("latestcallts")
                            } catch (ex: JSONException) {
                                ex.printStackTrace()
                            }

                            (mListenerMap[uuId] as DataCenterListener.OngetSMSVersionResponListener).getSMSVersionSuccess(backts, callts)
                            mListenerMap.remove(uuId)
                        }
                    }
                }
                Consts.REQ_SMS_RECORDS_DETAIL -> if (!TextUtils.isEmpty(uuId)) {
                    val str = result as String
                    var backts: Long = 0
                    var callts: Long = 0
                    if (str != null) {
                        println("ok gp: OnResult httpHandle =$httpHandle flag=$flag code=$code")
                        try {
                            val resoj = JSONObject(result)
                            backts = resoj.getLong("latestbackupts")
                            callts = resoj.getLong("latestcallts")
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        (mListenerMap[uuId] as DataCenterListener.OngetSMSVersionResponListener).getSMSVersionSuccess(backts, callts)
                        mListenerMap.remove(uuId)
                    }
                }
                Consts.REQ_SMS_RECORDS_BACKUP -> if (!TextUtils.isEmpty(uuId)) {
                    val str = result as String
                    if (str != null) {
                        println("ok gp: OnResult httpHandle =$httpHandle flag=$flag code=$code")
                        var backts: Long = 0
                        try {
                            val resoj = JSONObject(result)
                            backts = resoj.getLong("latestbackupts")
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        (mListenerMap[uuId] as DataCenterListener.OnuploadSMSResponListener).uploadSMSSuccess(backts)
                        mListenerMap.remove(uuId)
                    }

                }
                Consts.REQ_SMS_RECORDS_RECORDS -> if (!TextUtils.isEmpty(uuId)) {
                    val str = result as String
                    var jsonArray: JSONArray? = null
                    val serverList = ArrayList<SmsInfo>()
                    if (str != null) {
                        println("ok gp: OnResult httpHandle =$httpHandle flag=$flag code=$code")
                        try {
                            val resoj = JSONObject(str)
                            jsonArray = resoj.getJSONArray("records")
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                            LogUtils.d(TAG, ex.message)
                        }

                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                try {
                                    val mSmsInfo = JsonUtils.JsonToSmsInfo(jsonArray.getString(i))
                                    serverList.add(mSmsInfo)
                                } catch (ex: JSONException) {
                                    ex.printStackTrace()
                                    LogUtils.d(TAG, ex.message)
                                }

                            }
                        }
                        (mListenerMap[uuId] as DataCenterListener.OngetSMSRecordResponListener).getSMSRecordSuccess(serverList)
                        mListenerMap.remove(uuId)
                    }
                }
                Consts.REQ_GET_CALL_RECORD_DETAIL -> if (!TextUtils.isEmpty(uuId)) {
                    val str = result as String
                    var backts: Long = 0
                    var callts: Long = 0
                    if (str != null) {
                        println("ok gp: OnResult httpHandle =$httpHandle flag=$flag code=$code")
                        try {
                            val resoj = JSONObject(result)
                            backts = resoj.getLong("latestbackupts")
                            callts = resoj.getLong("latestcallts")
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                            LogUtils.d(TAG, ex.message)
                        }

                        (mListenerMap[uuId] as DataCenterListener.OngetCallVersionResponListener).getCallVersionSuccess(backts, callts)
                        mListenerMap.remove(uuId)
                    }
                }
                Consts.REQ_GET_CALL_RECORDS -> if (!TextUtils.isEmpty(uuId)) {
                    val str = result as String
                    var jsonArray: JSONArray? = null
                    val serverList = ArrayList<CallLogBean>()
                    if (str != null) {
                        println("ok gp: OnResult httpHandle =$httpHandle flag=$flag code=$code")
                        try {
                            val resoj = JSONObject(str)
                            jsonArray = resoj.getJSONArray("records")
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                            LogUtils.d(TAG, ex.message)
                        }

                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (i in 0 until jsonArray.length()) {
                                try {
                                    val mCallLogBean = JsonUtils.JsonToCallLogBean(jsonArray.getString(i))
                                    serverList.add(mCallLogBean)
                                } catch (ex: JSONException) {
                                    ex.printStackTrace()
                                    LogUtils.d(TAG, ex.message)
                                }

                            }
                        }
                        (mListenerMap[uuId] as DataCenterListener.OngetCallRecordResponListener).getCallRecordSuccess(serverList)
                        mListenerMap.remove(uuId)
                    }
                }
                Consts.REQ_CALL_RECORD_BACKUP -> if (!TextUtils.isEmpty(uuId)) {
                    val str = result as String
                    if (str != null) {
                        println("ok gp: OnResult httpHandle =$httpHandle flag=$flag code=$code")
                        var backts: Long = 0
                        try {
                            val resoj = JSONObject(result)
                            backts = resoj.getLong("latestbackupts")
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                        }

                        (mListenerMap[uuId] as DataCenterListener.OnuploadCallResponListener).uploadCallsSuccess(backts)
                        mListenerMap.remove(uuId)
                    }

                }
            }
        } else {
            when (flag) {
                Consts.REQ_LONGIN -> if (!TextUtils.isEmpty(uuId)) {
                    (mListenerMap[uuId] as DataCenterListener.OnlogninResponListener).logninFailed(code, msg)
                    mListenerMap.remove(uuId)
                }
                Consts.REQ_LOGOUT -> if (!TextUtils.isEmpty(uuId)) {
                    (mListenerMap[uuId] as DataCenterListener.OnlogOutResponListener).logOutFailed(code, msg)
                    mListenerMap.remove(uuId)
                }
                Consts.REQ_CONTACT_RECOVERY -> if (!TextUtils.isEmpty(uuId)) {
                    val str = result as String
                    //解析返回的联系人数据JSON
                    val serverList = ArrayList<ContactBean>()
                    val server_deleteList = ArrayList<ContactBean>()
                    val serverIdHashtable = Hashtable()
                    val serverAllDataHashtable = Hashtable()
                    var version = -1
                    if (str != null) {
                        val resoj: JSONObject
                        val Contact_list: JSONArray
                        try {
                            resoj = JSONObject(str.replace("\n", ""))
                            version = resoj.getInt("v")
                            Contact_list = resoj.getJSONArray("contacts")
                            for (i in 0 until Contact_list.length()) {
                                try {
                                    val mContactBean = JsonUtils.JsonToContactBean(Contact_list.getString(i))
                                    val contact = JSONObject(Contact_list.getString(i))
                                    if (contact.getBoolean("mark_deleted")) {
                                        //记录在云端删除表
                                        server_deleteList.add(mContactBean)
                                        //                                                Factory.get().getDBManager().insertInServerdelete(
                                        //                                                        IdPatternUtils.formatServerId(mContactBean.getServerId(),Integer.parseInt(mContactBean.getServer_version())),
                                        //                                                        mContactBean.getMd5(),Contact_list.getString(i));
                                    } else {
                                        serverList.add(mContactBean)
                                        serverIdHashtable.put(mContactBean.getServerId(), Integer.parseInt(mContactBean.getServer_version()))
                                        serverAllDataHashtable.put(mContactBean.getServerId(), mContactBean)
                                    }
                                } catch (ex: JSONException) {
                                    ex.printStackTrace()
                                    LogUtils.d(TAG, ex.message)
                                }

                            }
                        } catch (ex: JSONException) {
                            ex.printStackTrace()
                            LogUtils.d(TAG, ex.message)
                        }

                    }
                    (mListenerMap[uuId] as DataCenterListener.OngetContactResponListener).getContactFailed(code, msg, version, serverList, serverIdHashtable, serverAllDataHashtable, server_deleteList)
                    mListenerMap.remove(uuId)

                }
                Consts.REQ_CONTACT_MODIFY -> if (!TextUtils.isEmpty(uuId)) {
                    (mListenerMap[uuId] as DataCenterListener.OnuploadContactResponListener).uploadContactFailed(code, msg)
                    mListenerMap.remove(uuId)
                }
                Consts.REQ_SMS_RECORDS_DETAIL -> if (!TextUtils.isEmpty(uuId)) {
                    (mListenerMap[uuId] as DataCenterListener.OngetSMSVersionResponListener).getSMSVersionFailed(code, msg)
                    mListenerMap.remove(uuId)
                }
                Consts.REQ_SMS_RECORDS_BACKUP -> if (!TextUtils.isEmpty(uuId)) {
                    (mListenerMap[uuId] as DataCenterListener.OnuploadSMSResponListener).uploadSMSFailed(code, msg)
                    mListenerMap.remove(uuId)
                }
                Consts.REQ_SMS_RECORDS_RECORDS -> if (!TextUtils.isEmpty(uuId)) {
                    (mListenerMap[uuId] as DataCenterListener.OngetSMSRecordResponListener).getSMSRecordFailed(code, msg)
                    mListenerMap.remove(uuId)
                }
                Consts.REQ_GET_CALL_RECORD_DETAIL -> if (!TextUtils.isEmpty(uuId)) {
                    (mListenerMap[uuId] as DataCenterListener.OngetCallVersionResponListener).getCallVersionFailed(code, msg)
                    mListenerMap.remove(uuId)
                }
                Consts.REQ_GET_CALL_RECORDS -> if (!TextUtils.isEmpty(uuId)) {
                    (mListenerMap[uuId] as DataCenterListener.OngetCallRecordResponListener).getCallRecordFailed(code, msg)
                    mListenerMap.remove(uuId)
                }
                Consts.REQ_CALL_RECORD_BACKUP -> if (!TextUtils.isEmpty(uuId)) {
                    (mListenerMap[uuId] as DataCenterListener.OnuploadCallResponListener).uploadCallsFailed(code, msg)
                    mListenerMap.remove(uuId)
                }
            }
        }

    }

    companion object {

        val TAG = "DataCenterModel"
    }
}
