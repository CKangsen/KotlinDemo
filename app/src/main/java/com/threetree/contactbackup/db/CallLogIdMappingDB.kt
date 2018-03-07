package com.threetree.contactbackup.db



object CallLogIdMappingDB {

    /**
     * 通话记录 本地id——服务器MD5 映射表
     */
    val CALLLOG_IDMAPPING_TABLE_NAME = "CallLog_Mapping_Table"
    /**
     * _ID：表主键id
     * <P>Type: INTEGER (long)</P>
     */
    val _ID = "_id"

    /**
     * 通话记录id
     * <P>Type: INTEGER (long)</P>
     */
    val CALLLOG_ID = "calllog_id"

    /**
     * MD5：number+ts
     * <P>Type: TEXT </P>
     */
    val MD5 = "md5"
}
