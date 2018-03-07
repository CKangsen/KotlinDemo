package com.threetree.contactbackup.bean


class SmsInfo {
    //短信 内容
    var body: String? = null
    //短信 时间 整型
    var date: String? = null
    //短信 类型 发短信为2，收到短信为1
    var type: String? = null
    //短信号码
    var address: String? = null
    //服务器ID
    var server_id: String? = null
    //服务器版本
    var server_version: String? = null
    //本地ID
    var local_id: Int = 0

    constructor(body: String, date: String, type: String, address: String) : super() {
        this.body = body
        this.date = date
        this.type = type
        this.address = address
    }

    constructor() {}


}
