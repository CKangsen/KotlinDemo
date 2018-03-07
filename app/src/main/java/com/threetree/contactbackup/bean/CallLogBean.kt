package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 通话记录 信息类
 */

class CallLogBean {


    /**
     * 服务器记录的id (即 MD5：number+ts)
     */
    var serverid: String? = null
    /**
     * 通话记录id
     */
    var _id: Int = 0
    /**
     * 通话类型
     */
    var type: Int = 0
    /**
     * 电话号码
     */
    var number: String? = null
    /**
     * 显示规则
     */
    var presentation: Int = 0
    /**
     * 通话日期
     */
    var date: String? = null
    /**
     * 通话时长
     */
    var duration: String? = null
    /**
     * 数据用量
     */
    //private String data_usage;
    /**
     * 呼叫功能
     */
    //private int features;
    /**
     * 本机号码
     */
    //private String phone_account_address;
    /**
     * 缓存名字
     */
    //    public String getData_usage() {
    //        return data_usage;
    //    }

    //    public int getFeatures() {
    //        return features;
    //    }

    //    public String getPhone_account_address() {
    //        return phone_account_address;
    //    }

    //    public void setData_usage(String data_usage) {
    //        this.data_usage = data_usage;
    //    }

    //    public void setFeatures(int features) {
    //        this.features = features;
    //    }

    //    public void setPhone_account_address(String phone_account_address) {
    //        this.phone_account_address = phone_account_address;
    //    }

    var cachename: String? = null
    /**
     * 缓存电话类型
     */
    var cachenumbertype: String? = null
    /**
     * 缓存电话标签
     */
    var cachenumberlabel: String? = null
    /**
     * 城市代码
     */
    var countryiso: String? = null
    /**
     * 是否已读
     */
    var is_read: Int = 0
    /**
     * 地理位置
     */
    var geocoded_location: String? = null

    fun toJsonString(): String {
        return toJson().toString()
    }

    fun toJson(): JSONObject {
        val callLog_json = JSONObject()
        val data_json = JSONObject()
        try {
            data_json.put(DATA_ID, this._id)
            data_json.put(DATA_TYPE, this.type)
            data_json.put(DATA_PRESENTATION, this.presentation)
            data_json.put(DATA_DUARATION, this.duration)
            //data_json.put(DATA_USAGE,this.data_usage);
            //data_json.put(DATA_FEATURES,this.features);
            //data_json.put(DATA_PHONE_ACCOUNT_ADDRESS,this.phone_account_address);
            data_json.put(DATA_CACHE_NAME, this.cachename)
            data_json.put(DATA_CACHE_NUMBER_TYPE, this.cachenumbertype)
            data_json.put(DATA_CACHE_NUMBER_LAYBEL, this.cachenumberlabel)
            data_json.put(DATA_COUNTRY_ISO, this.countryiso)
            data_json.put(DATA_IS_READ, this.is_read)
            data_json.put(DATA_GEOCODED_LOCATION, this.geocoded_location)

            callLog_json.put(ID, serverid)
            callLog_json.put(NUMBER, this.number)
            callLog_json.put(TIMESTAMP, java.lang.Long.valueOf(this.date)!!.toLong())
            callLog_json.put(DATA, data_json.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return callLog_json
    }

    companion object {

        val ID = "id"
        val NUMBER = "number"
        val TIMESTAMP = "timestamp"
        val DATA = "data"
        val DATA_ID = "_id"
        val DATA_TYPE = "type"
        val DATA_PRESENTATION = "presentation"
        val DATA_DUARATION = "duration"
        //public static final String DATA_USAGE = "data_usage";
        val DATA_FEATURES = "features"
        //public static final String DATA_PHONE_ACCOUNT_ADDRESS = "phone_account_address";
        val DATA_CACHE_NAME = "cachename"
        val DATA_CACHE_NUMBER_TYPE = "cachenumbertype"
        val DATA_CACHE_NUMBER_LAYBEL = "cachenumberlabel"
        val DATA_COUNTRY_ISO = "countryiso"
        val DATA_IS_READ = "is_read"
        val DATA_GEOCODED_LOCATION = "geocoded_location"
    }
}
