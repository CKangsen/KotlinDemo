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
 * DAO for table "SERVER_DELETE".
*/
public class ServerDeleteDao extends AbstractDao<ServerDelete, Long> {

    public static final String TABLENAME = "SERVER_DELETE";

    /**
     * Properties of entity ServerDelete.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Server_id = new Property(1, String.class, "server_id", false, "SERVER_ID");
        public final static Property Md5 = new Property(2, String.class, "md5", false, "MD5");
        public final static Property Contact_data = new Property(3, String.class, "contact_data", false, "CONTACT_DATA");
        public final static Property Openid = new Property(4, String.class, "openid", false, "OPENID");
    }


    public ServerDeleteDao(DaoConfig config) {
        super(config);
    }
    
    public ServerDeleteDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SERVER_DELETE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"SERVER_ID\" TEXT NOT NULL UNIQUE ," + // 1: server_id
                "\"MD5\" TEXT NOT NULL ," + // 2: md5
                "\"CONTACT_DATA\" TEXT," + // 3: contact_data
                "\"OPENID\" TEXT NOT NULL );"); // 4: openid
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SERVER_DELETE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, ServerDelete entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getServer_id());
        stmt.bindString(3, entity.getMd5());
 
        String contact_data = entity.getContact_data();
        if (contact_data != null) {
            stmt.bindString(4, contact_data);
        }
        stmt.bindString(5, entity.getOpenid());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, ServerDelete entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getServer_id());
        stmt.bindString(3, entity.getMd5());
 
        String contact_data = entity.getContact_data();
        if (contact_data != null) {
            stmt.bindString(4, contact_data);
        }
        stmt.bindString(5, entity.getOpenid());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public ServerDelete readEntity(Cursor cursor, int offset) {
        ServerDelete entity = new ServerDelete( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // server_id
            cursor.getString(offset + 2), // md5
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // contact_data
            cursor.getString(offset + 4) // openid
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, ServerDelete entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setServer_id(cursor.getString(offset + 1));
        entity.setMd5(cursor.getString(offset + 2));
        entity.setContact_data(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setOpenid(cursor.getString(offset + 4));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(ServerDelete entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(ServerDelete entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(ServerDelete entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
