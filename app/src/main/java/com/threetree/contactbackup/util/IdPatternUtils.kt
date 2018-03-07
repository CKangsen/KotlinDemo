package com.threetree.contactbackup.util

/**
 *
 *
 * localid means contact_id + "-" + version ;
 * serverid means server_id + "-" + version ;
 */

object IdPatternUtils {

    val LINK_SYMBOL = "-"

    fun formatLocalId(contact_id: Int, version: Int): String {
        return contact_id.toString() + LINK_SYMBOL + version
    }

    fun formatServerId(server_id: String, version: Int): String {
        return server_id + LINK_SYMBOL + version
    }

    fun getIdByParseLocalId(localid: String): Int {
        return Integer.valueOf(localid.split(LINK_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0])!!
    }

    fun getVersionByParseLocalId(localid: String): Int {
        return Integer.valueOf(localid.split(LINK_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])!!
    }

    fun getIdByParseServerId(server_id: String): String {
        return server_id.split(LINK_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }

    fun getVersionByParseServerId(server_id: String): Int {
        return Integer.valueOf(server_id.split(LINK_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])!!
    }
}
