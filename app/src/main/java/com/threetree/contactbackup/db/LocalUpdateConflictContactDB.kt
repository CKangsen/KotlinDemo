package com.threetree.contactbackup.db

/**
 * 存储已检测到的修改冲突的本地联系人id
 *
 *
 */

object LocalUpdateConflictContactDB {

    /**
     * 联系人本地-服务器映射 数据表名
     */
    val LOCAL_UPDATE_CONFLICT_TABLE_NAME = "local_update_conflict_table"

    /**
     * _ID：表主键id
     * <P>Type: INTEGER (long)</P>
     */
    val _ID = "_id"
    /**
     * 联系人id
     * <P>Type: INTEGER (long)</P>
     */
    val CONTACT_ID = "contact_id"
}
