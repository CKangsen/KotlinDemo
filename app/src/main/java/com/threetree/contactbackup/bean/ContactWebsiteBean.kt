package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人 网址 信息类
 */

class ContactWebsiteBean : ContactBaseBean {

    var url: String? = null

    constructor(type: Int, label: String, url: String) : super(type, label) {
        this.url = url
    }

    constructor() {}

    fun toJsonString(): String {
        val json = JSONObject()
        try {

            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(URL, this.url)
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
            json.put(URL, this.url)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    companion object {


        val URL = "url"
        val TYPE = "type"
        val LABEL = "label"
    }
}
