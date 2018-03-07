package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人 昵称 信息类
 */

class ContactNickNameBean : ContactBaseBean {

    var name: String? = null

    constructor() {}

    constructor(type: Int, label: String, name: String) : super(type, label) {
        this.name = name
    }

    fun toJsonString(): String {
        val json = JSONObject()
        try {

            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(NAME, this.name)

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
            json.put(NAME, this.name)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    companion object {

        val NAME = "name"
    }
}
