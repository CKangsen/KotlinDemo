package com.threetree.contactbackup.bean

/**
 *
 *
 * 联系人共用属性 信息类
 */

open class ContactBaseBean {

    private var type: Int = 0
    private var label: String? = null

    constructor() {}

    constructor(type: Int, label: String) {
        this.type = type
        this.label = label
    }

    companion object {

        val TYPE = "type"
        val LABEL = "label"
    }

    fun getLabel(): String? {
        return this.label
    }

    fun getType(): Int? {
        return this.type
    }

    fun setLabel(label : String) {
        this.label = label
    }

    fun setType(type : Int) {
        this.type = type
    }


}
