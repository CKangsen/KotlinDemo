package com.threetree.contactbackup.bean


open class ContactBaseBeanIncludeData {

    private var data: String? = null
    private var type: Int = 0
    private var label: String? = null

    constructor() {}

    constructor(data: String, type: Int, label: String) {
        this.data = data
        this.type = type
        this.label = label
    }

    companion object {

        val DATA = "data"
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

    fun getData(): String? {
        return this.data
    }

    fun setData(data : String) {
        this.data = data
    }
}
