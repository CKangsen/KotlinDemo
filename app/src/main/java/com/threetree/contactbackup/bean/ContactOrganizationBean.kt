package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人 组织 信息类
 */

class ContactOrganizationBean : ContactBaseBean {

    var company: String? = null
    var title: String? = null
    var department: String? = null
    var job_description: String? = null
    var symbol: String? = null
    var phonetic_name: String? = null
    var office_location: String? = null
    var phonetic_name_style: String? = null

    constructor(type: Int, label: String, company: String, title: String, department: String, job_description: String, symbol: String, phonetic_name: String, office_location: String, phonetic_name_style: String) : super(type, label) {
        this.company = company
        this.title = title
        this.department = department
        this.job_description = job_description
        this.symbol = symbol
        this.phonetic_name = phonetic_name
        this.office_location = office_location
        this.phonetic_name_style = phonetic_name_style
    }

    constructor() {}

    fun toJsonString(): String {
        val json = JSONObject()
        try {

            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(COMPANY, this.company)
            json.put(TITLE, this.title)
            json.put(DEPARTMENT, this.department)
            json.put(JOB_DESCRIPTION, this.job_description)
            json.put(SYMBOL, this.symbol)
            json.put(PHONETIC_NAME, this.phonetic_name)
            json.put(OFFICE_LOCATION, this.office_location)
            json.put(PHONETIC_NAME_STYLE, this.phonetic_name_style)

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
            json.put(COMPANY, this.company)
            json.put(TITLE, this.title)
            json.put(DEPARTMENT, this.department)
            json.put(JOB_DESCRIPTION, this.job_description)
            json.put(SYMBOL, this.symbol)
            json.put(PHONETIC_NAME, this.phonetic_name)
            json.put(OFFICE_LOCATION, this.office_location)
            json.put(PHONETIC_NAME_STYLE, this.phonetic_name_style)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    companion object {

        val COMPANY = "company"
        val TITLE = "title"
        val DEPARTMENT = "department"
        val JOB_DESCRIPTION = "job_description"
        val SYMBOL = "symbol"
        val PHONETIC_NAME = "phonetic_name"
        val OFFICE_LOCATION = "office_location"
        val PHONETIC_NAME_STYLE = "phonetic_name_style"
    }
}
