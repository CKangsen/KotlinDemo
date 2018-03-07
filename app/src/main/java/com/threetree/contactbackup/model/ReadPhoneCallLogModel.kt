package com.threetree.contactbackup.model

import android.content.ContentResolver
import android.database.Cursor
import android.provider.CallLog

import com.threetree.contactbackup.bean.CallLogBean
import com.threetree.contactbackup.util.GetMD5Utils

import java.util.ArrayList


class ReadPhoneCallLogModel(private val mResolver: ContentResolver) {
    private var mCallLogBeanList: MutableList<CallLogBean>? = null

    val callLogBeanList: List<CallLogBean>?
        get() {
            getPhoneCallLogData()
            return mCallLogBeanList
        }

    /**
     * 根据提供的idList获取通话记录
     *
     * 返回List<CallLogBean>
    </CallLogBean> */
    fun getCallLogInfoByIdList(idList: List<Int>): List<CallLogBean> {
        if (mCallLogBeanList == null || mCallLogBeanList!!.size <= 0) {
            getPhoneCallLogData()
        }

        val newCallLogBeanList = ArrayList<CallLogBean>()

        for (callLogBean in mCallLogBeanList!!) {
            if (idList.contains(callLogBean.get_id())) {
                newCallLogBeanList.add(callLogBean)
            }
        }
        return newCallLogBeanList
    }


    private fun getPhoneCallLogData() {

        mCallLogBeanList = ArrayList<CallLogBean>()
        var callLogCursor: Cursor? = null
        //CallLog集合
        try {
            callLogCursor = mResolver.query(CallLog.Calls.CONTENT_URI, selectCallLogColumms, null, null, null)

            while (callLogCursor!!.moveToNext()) {
                val callLogBean = CallLogBean()
                callLogBean.set_id(callLogCursor.getInt(CALLLOG_ID_INDEX))
                callLogBean.setNumber(callLogCursor.getString(NUMBER_INDEX))
                callLogBean.setDate(callLogCursor.getString(DATE_INDEX))
                callLogBean.setType(callLogCursor.getInt(TYPE_INDEX))
                callLogBean.setPresentation(callLogCursor.getInt(NUMBER_PRESENTATION_INDEX))
                callLogBean.setDuration(callLogCursor.getString(DURATION_INDEX))
                //callLogBean.setData_usage(callLogCursor.getString(DATA_USAGE_INDEX));
                //callLogBean.setFeatures(callLogCursor.getInt(FEATURES_INDEX));
                callLogBean.setCachename(callLogCursor.getString(CACHED_NAME_INDEX))
                callLogBean.setCachenumbertype(callLogCursor.getString(CACHED_NUMBER_TYPE_INDEX))
                callLogBean.setCachenumberlabel(callLogCursor.getString(CACHED_NUMBER_LABEL_INDEX))
                callLogBean.setCountryiso(callLogCursor.getString(COUNTRY_ISO_INDEX))
                callLogBean.setIs_read(callLogCursor.getInt(IS_READ_INDEX))
                callLogBean.setGeocoded_location(callLogCursor.getString(GEOCODED_LOCATION_INDEX))

                //由number以及Timestamp 取md5 生成的唯一索引
                callLogBean.setServerid(GetMD5Utils.getMD5(callLogBean.getNumber() + callLogBean.getDate()))

                mCallLogBeanList!!.add(callLogBean)

            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } finally {
            if (callLogCursor != null) {
                callLogCursor.close()
            }
        }


    }

    companion object {

        val TAG = ReadPhoneCallLogModel::class.java.simpleName

        val selectCallLogColumms = arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE, CallLog.Calls.NUMBER_PRESENTATION, CallLog.Calls.DURATION,
                //CallLog.Calls.DATA_USAGE,
                //CallLog.Calls.FEATURES,
                CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_TYPE, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.COUNTRY_ISO, CallLog.Calls.IS_READ, CallLog.Calls.GEOCODED_LOCATION)


        val CALLLOG_ID_INDEX = 0
        val NUMBER_INDEX = 1
        val DATE_INDEX = 2
        val TYPE_INDEX = 3
        val NUMBER_PRESENTATION_INDEX = 4
        val DURATION_INDEX = 5
        //public static final int  DATA_USAGE_INDEX = 6;
        //public static final int  FEATURES_INDEX = DURATION_INDEX+1;
        val CACHED_NAME_INDEX = DURATION_INDEX + 1
        val CACHED_NUMBER_TYPE_INDEX = CACHED_NAME_INDEX + 1
        val CACHED_NUMBER_LABEL_INDEX = CACHED_NUMBER_TYPE_INDEX + 1
        val COUNTRY_ISO_INDEX = CACHED_NUMBER_LABEL_INDEX + 1
        val IS_READ_INDEX = COUNTRY_ISO_INDEX + 1
        val GEOCODED_LOCATION_INDEX = IS_READ_INDEX + 1
    }

    /*
    type PhoneRecord struct {
	PK        string `dynamo:"pk,hash,omitempty"` //由number以及Timestamp 取md5 生成的唯一索引
	Number    string `json:"number,omitempty" dynamo:"number,range,omitempty"` //手机号
	Timestamp int64  `json:"timestamp,omitempty" index:"gsi_ts,hash"  dynamo:"timestamp,omitempty"` //时间戳,精确到毫秒
	Data      string `json:"data,omitempty" dynamo:"data,omitempty"`   //用户自定义数据
    }
    */
}
