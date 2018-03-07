package com.threetree.contactbackup.util

import java.io.Serializable


abstract class SearchFilter : Serializable {//

    abstract fun getAlpha(str: String): Char

    abstract fun getFullSpell(str: String): String

    abstract fun getInputString(str: String): String

    companion object {

        private const val serialVersionUID = 1L
    }


}