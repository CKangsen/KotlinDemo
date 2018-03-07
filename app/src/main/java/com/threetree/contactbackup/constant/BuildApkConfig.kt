
package com.threetree.contactbackup.constant


object BuildApkConfig {
    val SDK_VERSION = "1.0"
    /*是否打日志*/
    /**
     * is debug model
     * @return
     */
    var isDebugModel = true
        private set
    val channel_Id: String
        get() = "101"

    /**
     * set debug model
     * @param isDebugModel
     */
    fun setDebugMode(isDebugModel: Boolean) {
        this.isDebugModel = isDebugModel
    }
}
