package com.threetree.contactbackup.util


import android.text.TextUtils

import com.afmobi.tudcsdk.utils.SearchFilter

/**
 * 筛选过滤
 */
class DefaultSearch : SearchFilter() {

    fun getAlpha(str: String): Char {
        if (!TextUtils.isEmpty(str)) {
            val ch = str[0]
            return if (ch >= 'A' && ch <= 'Z') {
                ch
            } else if (ch >= 'a' && ch <= 'z') {
                (ch.toInt() - 32).toChar()
            } else {
                '#'
            }
        }
        return '#'
    }

    fun getFullSpell(str: String): String {
        return if ('#' == getAlpha(str)) {
            "|"
        } else str.toUpperCase()
    }

    fun getInputString(str: String): String {
        return str.toUpperCase()
    }

    companion object {

        private val serialVersionUID = 1L
    }

}
