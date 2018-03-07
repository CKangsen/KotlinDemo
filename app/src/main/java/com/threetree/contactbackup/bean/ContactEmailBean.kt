package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人的邮箱信息类
 */

class ContactEmailBean : ContactBaseBean {

    var address: String? = null
    var dispaly_name: String? = null

    constructor(type: Int, label: String, address: String, dispaly_name: String) : super(type, label) {
        this.address = address
        this.dispaly_name = dispaly_name
    }

    constructor() {}

    fun toJsonString(): String {
        val json = JSONObject()
        try {

            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(ADDRESS, this.address)
            json.put(DISPALY_NAME, this.dispaly_name)
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
            json.put(ADDRESS, this.address)
            json.put(DISPALY_NAME, this.dispaly_name)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    companion object {

        val ADDRESS = "address"
        val DISPALY_NAME = "dispaly_name"
    }
}
