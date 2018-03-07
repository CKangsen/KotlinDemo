package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人 Sip地址 信息类
 */

class ContactSipAddressBean : ContactBaseBean {

    var sip_address: String? = null

    constructor() {}

    constructor(type: Int, label: String, sip_address: String) : super(type, label) {
        this.sip_address = sip_address
    }

    fun toJsonString(): String {
        val json = JSONObject()
        try {

            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(SIP_ADDRESS, this.sip_address)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json.toString()
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        try {

            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(SIP_ADDRESS, this.sip_address)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    companion object {

        val SIP_ADDRESS = "sip_address"
    }
}
