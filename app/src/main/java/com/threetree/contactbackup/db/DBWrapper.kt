package com.threetree.contactbackup.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteTransactionListener



class DBWrapper internal constructor(private val mContext: Context, private val mDatabase: SQLiteDatabase) {

    fun query(table: String, columns: Array<String>, selection: String,
              selectionArgs: Array<String>, groupBy: String, having: String,
              orderBy: String): Cursor {
        return mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
    }

    fun insert(table: String, nullColumnHack: String, values: ContentValues): Long {
        return mDatabase.insert(table, nullColumnHack, values)
    }

    fun insertWithOnConflict(table: String, nullColumnHack: String, values: ContentValues, conflictAlgorithm: Int): Long {
        return mDatabase.insertWithOnConflict(table, nullColumnHack, values, conflictAlgorithm)
    }

    fun delete(table: String, whereClause: String, whereArgs: Array<String>): Long {
        return mDatabase.delete(table, whereClause, whereArgs).toLong()
    }

    fun update(table: String, values: ContentValues, whereClause: String, whereArgs: Array<String>): Long {
        return mDatabase.update(table, values, whereClause, whereArgs).toLong()
    }

    fun beginTransaction() {
        mDatabase.beginTransaction()
    }

    fun beginTransactionWithListener(sqLiteTransactionListener: SQLiteTransactionListener) {
        mDatabase.beginTransactionWithListener(sqLiteTransactionListener)
    }

    fun endTransaction() {
        mDatabase.setTransactionSuccessful()
        mDatabase.endTransaction()
    }
}
