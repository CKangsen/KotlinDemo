package com.threetree.contactbackup.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.threetree.contactbackup.ApplicationPrefsManager.OPEN_ID



class DBHelper(private val mApplicationContext: Context) : SQLiteOpenHelper(mApplicationContext, DATABASE_NAME, null, DATABASE_VERSION) {
    private val mDatabaseWrapperLock = Any()
    private var mDBWrapper: DBWrapper? = null


    val database: DBWrapper
        get() = synchronized(mDatabaseWrapperLock) {
            if (mDBWrapper == null) {
                mDBWrapper = DBWrapper(mApplicationContext, writableDatabase)
            }
            return mDBWrapper
        }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DATABASE_IDMAPPINGDB_CREATE)
        db.execSQL(DATABASE_SERVERDELETEDB_CREATE)
        db.execSQL(DATABASE_SMS_IDAPPINGDB_CREATE)
        db.execSQL(DATABASE_CALLLOG_IDAPPINGDB_CREATE)
        db.execSQL(DATABASE_LOCAL_UPDATE_CONFLICT_DB_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("ALTER TABLE " + IDMappingDB.CONTACT_MAPPING_TABLE_NAME + " ADD " + IDMappingDB.SERVER_LATEST_VERSION + " integer not null default 0;")
        }

    }

    companion object {

        /**
         *
         */
        private val DATABASE_NAME = "waka_icloud.db"
        /**
         *
         * Version 2 : IDMappingDB add columns IDMappingDB.SERVER_LATEST_VERSION
         */
        private val DATABASE_VERSION = 2


        private val DATABASE_IDMAPPINGDB_CREATE = ("CREATE TABLE " + IDMappingDB.CONTACT_MAPPING_TABLE_NAME + " ("
                + IDMappingDB._ID + " integer primary key autoincrement,"
                + IDMappingDB.CONTACT_ID + " integer not null default 0,"
                + IDMappingDB.LOCAL_VERSION + " integer not null default 0,"
                + IDMappingDB.SERVER_ID + " string UNIQUE not null,"
                + IDMappingDB.SERVER_VERSION + " integer not null default 0,"
                + IDMappingDB.MD5 + " string ,"
                + OPEN_ID + " string not null ,"
                + IDMappingDB.SERVER_LATEST_VERSION + " integer not null default 0)")

        private val DATABASE_SERVERDELETEDB_CREATE = ("CREATE TABLE " + ServerDeleteDB.SERVER_DELETE_TABLE_NAME + " ("
                + ServerDeleteDB._ID + " integer primary key autoincrement,"
                + ServerDeleteDB.SEVER_ID + " string UNIQUE not null,"
                + ServerDeleteDB.MD5 + " string not null,"
                + ServerDeleteDB.CONTACT_DATA + " string ,"
                + OPEN_ID + " string not null)")


        private val DATABASE_SMS_IDAPPINGDB_CREATE = ("CREATE TABLE " + SMSidMappingDB.SMS_IDMAPPING_TABLE_NAME + " ("
                + SMSidMappingDB._ID + " integer primary key autoincrement,"
                + SMSidMappingDB.MD5 + " string UNIQUE not null,"
                + OPEN_ID + " string not null)")

        private val DATABASE_CALLLOG_IDAPPINGDB_CREATE = ("CREATE TABLE " + CallLogIdMappingDB.CALLLOG_IDMAPPING_TABLE_NAME + " ("
                + CallLogIdMappingDB._ID + " integer primary key autoincrement,"
                + CallLogIdMappingDB.CALLLOG_ID + " integer not null default 0,"
                + CallLogIdMappingDB.MD5 + " string UNIQUE not null,"
                + OPEN_ID + " string not null)")

        private val DATABASE_LOCAL_UPDATE_CONFLICT_DB_CREATE = ("CREATE TABLE " + LocalUpdateConflictContactDB.LOCAL_UPDATE_CONFLICT_TABLE_NAME + " ("
                + LocalUpdateConflictContactDB._ID + " integer primary key autoincrement,"
                + LocalUpdateConflictContactDB.CONTACT_ID + " integer UNIQUE not null default 0,"
                + OPEN_ID + " string not null)")
    }
}
