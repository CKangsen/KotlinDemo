package com.threetree.contactbackup.db


/**
 * 服务器删除表 累加
 * 表字段类
 *
 */

object ServerDeleteDB {
    /**
     * 服务器删除表 数据表名
     */
    val SERVER_DELETE_TABLE_NAME = "Server_Delete_Table"

    /**
     * _ID：表主键id
     * <P>Type: INTEGER (long)</P>
     */
    val _ID = "_id"


    /**
     * 服务器id (id+v)
     * <P>Type: TEXT</P>
     */
    val SEVER_ID = "server_id"
    /**
     * MD5：表名字和电话号码生成的MD5值
     * <P>Type: TEXT (long)</P>
     */
    val MD5 = "md5"

    /**
     * CONTACT_DATA：服务器下发单条数据的JSON TEXT 可以解析成contactbean
     * <P>Type: TEXT (long)</P>
     */
    val CONTACT_DATA = "contact_data"
}
