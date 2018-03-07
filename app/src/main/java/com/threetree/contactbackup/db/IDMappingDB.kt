package com.threetree.contactbackup.db




/**
 * CMT：联系人本地-服务器映射数据表， CONTACT_MAPPING_TABLE的首字母缩写
 * 表字段类
 *
 */
object IDMappingDB {


    /**
     * 联系人本地-服务器映射 数据表名
     */
    val CONTACT_MAPPING_TABLE_NAME = "Contact_Mapping_Table"

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
    /**
     * 本地数据版本
     * <P>Type: INTEGER (long)</P>
     */
    val LOCAL_VERSION = "local_version"
    /**
     * 服务器映射id
     * <P>Type: Text</P>
     */
    val SERVER_ID = "server_id"
    /**
     * 服务器数据版本
     * <P>Type: INTEGER (long)</P>
     */
    val SERVER_VERSION = "server_version"
    /**
     * md5（名字+手机 集合）
     * <P>Type: Text</P>
     */
    val MD5 = "md5"
    /**
     * 服务器数据最新版本
     * <P>Type: INTEGER (long)</P>
     */
    val SERVER_LATEST_VERSION = "server_latest_version"


    val ID_INDEX = 0
    val CONTACT_ID_INDEX = ID_INDEX + 1
    val LOCAL_VERSION_INDEX = CONTACT_ID_INDEX + 1
    val SERVER_INDEX = LOCAL_VERSION_INDEX + 1
    val SERVER_VERSION_INDEX = SERVER_INDEX + 1
    val MD5_INDEX = SERVER_VERSION_INDEX + 1
    val OPENID_INDEX = MD5_INDEX + 1
    val SERVER_LATEST_VERSION_INDEX = OPENID_INDEX + 1

}
