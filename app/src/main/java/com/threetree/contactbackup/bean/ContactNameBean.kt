package com.threetree.contactbackup.bean

import org.json.JSONException
import org.json.JSONObject

/**
 *
 *
 * 联系人 名称 信息类
 */

class ContactNameBean : ContactBaseBean {

    private var display_name: String? = null
    private var given_name: String? = null
    private var family_name: String? = null
    private var middle_name: String? = null
    private var prefix: String? = null
    private var suffix: String? = null
    private var phonetic_given_name: String? = null
    private var phonetic_middle_name: String? = null
    private var phonetic_family_name: String? = null
    private var full_name_style: String? = null
    private var phonetic_name_style: String? = null

    constructor() {

    }

    constructor(type: Int, label: String, display_name: String, given_name: String, family_name: String, middle_name: String, prefix: String, suffix: String, phonetic_given_name: String, phonetic_middle_name: String, phonetic_family_name: String, full_name_style: String, phonetic_name_style: String) : super(type, label) {
        this.display_name = display_name
        this.given_name = given_name
        this.family_name = family_name
        this.middle_name = middle_name
        this.prefix = prefix
        this.suffix = suffix
        this.phonetic_given_name = phonetic_given_name
        this.phonetic_middle_name = phonetic_middle_name
        this.phonetic_family_name = phonetic_family_name
        this.full_name_style = full_name_style
        this.phonetic_name_style = phonetic_name_style
    }


    fun toJsonString(): String {
        val json = JSONObject()
        try {

            json.put(TYPE, this.getType())
            json.put(LABEL, this.getLabel())
            json.put(DISPLAY_NAME, this.display_name)
            //json.put(GIVEN_NAME,this.given_name);
            //json.put(FAMILY_NAME,this.family_name);
            //json.put(MIDDLE_NAME,this.middle_name);
            //json.put(PREFIX,this.prefix);
            //json.put(SUFFIX,this.suffix);
            json.put(PHONETIC_GIVEN_NAME, this.phonetic_given_name)
            json.put(PHONETIC_MIDDLE_NAME, this.phonetic_middle_name)
            json.put(PHONETIC_FAMILY_NAME, this.phonetic_family_name)
            json.put(FULL_NAME_STYLE, this.full_name_style)
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
            json.put(DISPLAY_NAME, this.display_name)
            //json.put(GIVEN_NAME,this.given_name);
            //json.put(FAMILY_NAME,this.family_name);
            //json.put(MIDDLE_NAME,this.middle_name);
            //json.put(PREFIX,this.prefix);
            //json.put(SUFFIX,this.suffix);
            json.put(PHONETIC_GIVEN_NAME, this.phonetic_given_name)
            json.put(PHONETIC_MIDDLE_NAME, this.phonetic_middle_name)
            json.put(PHONETIC_FAMILY_NAME, this.phonetic_family_name)
            json.put(FULL_NAME_STYLE, this.full_name_style)
            json.put(PHONETIC_NAME_STYLE, this.phonetic_name_style)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    companion object {

        val DISPLAY_NAME = "display_name"
        val GIVEN_NAME = "given_name"
        val FAMILY_NAME = "fmaily_name"
        val MIDDLE_NAME = "middle_name"
        val PREFIX_NAME = "prefix_name"
        val SUFFIX_NAME = "suffix_name"
        val PHONETIC_GIVEN_NAME = "phonetic_given_name"
        val PHONETIC_MIDDLE_NAME = "phonetic_middle_name"
        val PHONETIC_FAMILY_NAME = "phonetic_family_name"
        val FULL_NAME_STYLE = "full_name_style"
        val PHONETIC_NAME_STYLE = "phonetic_name_style"
    }


    fun getGiven_name(): String? {
        return this.given_name
    }

    fun setGiven_name(label : String) {
        this.given_name = label
    }

    fun getFamily_name(): String? {
        return this.family_name
    }

    fun setFamily_name(label : String) {
        this.family_name = label
    }

    fun getPrefix(): String? {
        return this.prefix
    }

    fun setPrefix(label : String) {
        this.prefix = label
    }

    fun getMiddle_name(): String? {
        return this.middle_name
    }

    fun setMiddle_name(label : String) {
        this.middle_name = label
    }

    fun getSuffix(): String? {
        return this.suffix
    }

    fun setSuffix(label : String) {
        this.suffix = label
    }
}
