package com.threetree.contackbackup.model

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils

import com.threetree.contackbackup.bean.SmsInfo
import com.threetree.contackbackup.util.LogUtils

import java.util.ArrayList
import java.util.Hashtable



class ReadPhoneSMSModel(private val mResolver: ContentResolver) {
    //所有短信列表
    private var mSmsInfoList: MutableList<SmsInfo>? = null

    /**
     * 获取手机上的所有短信
     * 返回List<SmsInfo>
    </SmsInfo> */
    //查找所有短信按时间排序
    val smsInfo: List<SmsInfo>
        get() {
            val initTime = System.currentTimeMillis()
            val uri = Uri.parse("content://sms/")
            val cursor = mResolver.query(uri, arrayOf("body", "date", "type", "address", "read", "_id"), "address!=?", arrayOf(""), "date DESC")
            mSmsInfoList = ArrayList<SmsInfo>()
            while (cursor!!.moveToNext()) {
                var body = cursor.getString(0)
                if (TextUtils.isEmpty(body)) {
                    body = ""
                }
                var date = cursor.getString(1)
                if (TextUtils.isEmpty(date)) {
                    date = ""
                }
                var type = cursor.getString(2)
                if (TextUtils.isEmpty(type)) {
                    type = ""
                }
                var address = cursor.getString(3)
                if (TextUtils.isEmpty(address)) {
                    address = ""
                }
                val id = cursor.getInt(4)
                val smsinfo = SmsInfo(body, date, type, address)
                smsinfo.setLocal_id(id)
                mSmsInfoList!!.add(smsinfo)
            }
            cursor.close()
            LogUtils.INSTANCE.d("ReadPhoneSMSModel", " getSmsInfo  List<SmsInfo> Time:" + (System.currentTimeMillis() - initTime))
            return mSmsInfoList
        }
}
