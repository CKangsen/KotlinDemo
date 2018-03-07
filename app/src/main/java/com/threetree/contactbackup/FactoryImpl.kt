package com.threetree.contactbackup

import android.content.Context

import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager


class FactoryImpl private constructor() : Factory() {

    var applicationContext: Context? = null
        private set
    var dbManager: GreenDaoDBManager? = null
        private set
    var cacheStatisticsManager: CacheStatisticsManager? = null
        private set
    var applicationPrefsManager: ApplicationPrefsManager? = null
        private set

    companion object {

        fun register(applicationContext: Context): Factory {

            val factory = FactoryImpl()
            Factory.setInstance(factory)
            factory.applicationContext = applicationContext
            factory.dbManager = GreenDaoDBManager.Companion.getInstance()
            factory.cacheStatisticsManager = CacheStatisticsManager.getInstance()
            factory.applicationPrefsManager = ApplicationPrefsManager.getInstance(applicationContext)

            return factory
        }
    }
}
