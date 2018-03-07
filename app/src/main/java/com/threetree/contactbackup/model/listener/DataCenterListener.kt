package com.threetree.contactbackup.model.listener

import com.threetree.contactbackup.bean.CallLogBean
import com.threetree.contactbackup.bean.ContactBean
import com.threetree.contactbackup.bean.SmsInfo

import java.util.Hashtable


class DataCenterListener {
    //业务逻辑处理完成后回调接口
    interface OnlogninResponListener {
        fun logninSuccess()
        fun logninFailed(Code: Int, msg: String)
    }

    interface OnlogOutResponListener {
        fun logOutSuccess()
        fun logOutFailed(Code: Int, msg: String)
    }

    interface OngetContactResponListener {
        fun getContactSuccess(server_version: Int, serverContactList: List<ContactBean>, serverContactIDHashtable: Hashtable<String, Int>, serverContactDataHashtable: Hashtable<String, ContactBean>, serverDeleteContactList: List<ContactBean>)
        fun getContactFailed(Code: Int, msg: String, server_version: Int, serverContactList: List<ContactBean>, serverContactIDHashtable: Hashtable<String, Int>, serverContactDataHashtable: Hashtable<String, ContactBean>, serverDeleteContactList: List<ContactBean>)
    }

    interface OnuploadContactResponListener {
        fun uploadContactSuccess(addIds: List<String>)
        fun uploadContactFailed(Code: Int, msg: String)
    }

    interface OngetCallVersionResponListener {
        fun getCallVersionSuccess(latestbackupts: Long, latestcallts: Long)

        fun getCallVersionFailed(Code: Int, msg: String)
    }

    interface OngetCallRecordResponListener {
        fun getCallRecordSuccess(serverList: List<CallLogBean>)

        fun getCallRecordFailed(Code: Int, msg: String)
    }

    interface OnuploadCallResponListener {
        fun uploadCallsSuccess(latestbackupts: Long)

        fun uploadCallsFailed(Code: Int, msg: String)
    }

    interface OngetSMSVersionResponListener {
        fun getSMSVersionSuccess(latestbackupts: Long, latestcallts: Long)

        fun getSMSVersionFailed(Code: Int, msg: String)
    }

    interface OngetSMSRecordResponListener {
        fun getSMSRecordSuccess(serverList: List<SmsInfo>)

        fun getSMSRecordFailed(Code: Int, msg: String)
    }

    interface OnuploadSMSResponListener {
        fun uploadSMSSuccess(latestbackupts: Long)
        fun uploadSMSFailed(Code: Int, msg: String)

    }

    interface OnTudcLogninResponListener {
        fun logninSuccess(st: String)
        fun logninCancel()
        fun logninFailed(msg: String)
    }
}
