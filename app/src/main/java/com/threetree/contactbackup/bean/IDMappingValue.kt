package com.threetree.contactbackup.bean

/**
 * 联系人映射表 Bean 类
 *
 *
 */

class IDMappingValue {

    var contactid: Int = 0
    var contactV: Int = 0
    var serverid: String? = null
    var serverV: Int = 0
    var md5: String? = null
    var serverLatestV: Int = 0

    constructor() {}

    constructor(contactid: Int, contactV: Int, serverid: String, serverV: Int, md5: String) {
        this.contactid = contactid
        this.contactV = contactV
        this.serverid = serverid
        this.serverV = serverV
        this.md5 = md5
    }

    constructor(contactid: Int, contactV: Int, serverid: String, serverV: Int, md5: String, serverLatestV: Int) {
        this.contactid = contactid
        this.contactV = contactV
        this.serverid = serverid
        this.serverV = serverV
        this.md5 = md5
        this.serverLatestV = serverLatestV
    }
}
