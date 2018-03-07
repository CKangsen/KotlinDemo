package com.threetree.contactbackup

import android.content.Context

import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager


abstract class Factory {

    abstract val applicationContext: Context
    abstract val dbManager: GreenDaoDBManager
    abstract val cacheStatisticsManager: CacheStatisticsManager
    abstract val applicationPrefsManager: ApplicationPrefsManager

    companion object {

        @Volatile private var sInstance: Factory? = null

        fun get(): Factory? {
            return sInstance
        }

        protected fun setInstance(factory: Factory) {
            sInstance = factory
        }
    }
}
