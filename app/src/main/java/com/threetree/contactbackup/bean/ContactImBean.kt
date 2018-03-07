package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人 通讯工具 信息类
 */

class ContactImBean : ContactBaseBeanIncludeData {

    var protocol: String? = null
    var custom_protocol: String? = null

    constructor(data: String, type: Int, label: String, protocol: String, custom_protocol: String) : super(data, type, label) {
        this.protocol = protocol
        this.custom_protocol = custom_protocol
    }

    constructor() {}

    fun toJsonString(): String {
        val json = JSONObject()
        try {

            json.put(DATA, this.getData())
            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(PROTOCOL, this.protocol)
            json.put(CUSTOM_PROTOCOL, this.custom_protocol)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json.toString()
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        try {

            json.put(DATA, this.getData())
            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(PROTOCOL, this.protocol)
            json.put(CUSTOM_PROTOCOL, this.custom_protocol)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    companion object {

        val PROTOCOL = "protocol"
        val CUSTOM_PROTOCOL = "custom_protocol"
    }
}
