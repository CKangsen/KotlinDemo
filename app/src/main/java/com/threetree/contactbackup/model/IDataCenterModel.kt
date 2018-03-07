package com.threetree.contactbackup.model


import com.threetree.contactbackup.model.listener.DataCenterListener



interface IDataCenterModel {
    fun lognin(st: String, onlogninResponListener: DataCenterListener.OnlogninResponListener)
    fun logout(onlogOutResponListener: DataCenterListener.OnlogOutResponListener)
    fun getContact(version: Int, ongetContactResponListener: DataCenterListener.OngetContactResponListener)
    fun uploadContact(contactJson: String, onuploadContactResponListener: DataCenterListener.OnuploadContactResponListener)
    fun getCallVersion(ongetCallVersionResponListener: DataCenterListener.OngetCallVersionResponListener)
    fun getCallRecords(startTs: Long, endTs: Long, ongetCallRecordResponListener: DataCenterListener.OngetCallRecordResponListener)
    fun uploadCalls(callJson: String, onuploadCallResponListener: DataCenterListener.OnuploadCallResponListener)
    fun getSMSVersion(ongetSMSVersionResponListener: DataCenterListener.OngetSMSVersionResponListener)
    fun getSMSRecords(startTs: Long, endTs: Long, ongetSMSRecordResponListener: DataCenterListener.OngetSMSRecordResponListener)
    fun uploadSMS(smsJson: String, onuploadSMSResponListener: DataCenterListener.OnuploadSMSResponListener)

}
