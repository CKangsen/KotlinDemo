package com.greendao.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SMSID_MAPPING".
*/
public class SMSidMappingDao extends AbstractDao<SMSidMapping, Long> {

    public static final String TABLENAME = "SMSID_MAPPING";

    /**
     * Properties of entity SMSidMapping.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Md5 = new Property(1, String.class, "md5", false, "MD5");
        public final static Property Openid = new Property(2, String.class, "openid", false, "OPENID");
    }


    public SMSidMappingDao(DaoConfig config) {
        super(config);
    }
    
    public SMSidMappingDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SMSID_MAPPING\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"MD5\" TEXT NOT NULL UNIQUE ," + // 1: md5
                "\"OPENID\" TEXT NOT NULL );"); // 2: openid
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SMSID_MAPPING\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SMSidMapping entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getMd5());
        stmt.bindString(3, entity.getOpenid());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SMSidMapping entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getMd5());
        stmt.bindString(3, entity.getOpenid());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public SMSidMapping readEntity(Cursor cursor, int offset) {
        SMSidMapping entity = new SMSidMapping( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // md5
            cursor.getString(offset + 2) // openid
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SMSidMapping entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMd5(cursor.getString(offset + 1));
        entity.setOpenid(cursor.getString(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(SMSidMapping entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(SMSidMapping entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(SMSidMapping entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
