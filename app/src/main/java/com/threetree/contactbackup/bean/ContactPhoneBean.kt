package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人 电话 信息类
 */

class ContactPhoneBean : ContactBaseBean {

    var number: String? = null
    var normalized_number: String? = null
    var custom: String? = null
        set(custom) {
            this.setLabel(custom)
            field = custom
        }//服务器定义字段，取值来源于本地Label字段

    constructor(type: Int, label: String, number: String, normalized_number: String) : super(type, label) {
        this.number = number
        this.normalized_number = normalized_number
    }

    constructor() {}

    fun toJsonString(): String {
        val json = JSONObject()
        try {

            json.put(NUMBER_TYPE, this.getType())
            json.put(CUSTOM, this.getLabel())
            json.put(NUMBER, this.number)
            //json.put(NORMALIZED_NUMBER,this.normalized_number);

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json.toString()
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        try {

            json.put(NUMBER_TYPE, this.getType())
            json.put(CUSTOM, this.getLabel())
            json.put(NUMBER, this.number)
            //json.put(NORMALIZED_NUMBER,this.normalized_number);

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    companion object {

        val NUMBER = "number"
        val NORMALIZED_NUMBER = "normalized_number"
        val CUSTOM = "custom"
        val NUMBER_TYPE = "number_type"
    }
}
