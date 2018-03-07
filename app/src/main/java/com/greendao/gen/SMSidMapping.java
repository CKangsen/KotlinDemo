package com.greendao.gen;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by cks on 17-12-13.
 */

@Entity
public class SMSidMapping {

    @Id(autoincrement = true)
    private Long id;
    @Unique
    @NotNull
    private String md5;
    @NotNull
    private String openid;

    @Generated(hash = 1585550344)
    public SMSidMapping(Long id, @NotNull String md5, @NotNull String openid) {
        this.id = id;
        this.md5 = md5;
        this.openid = openid;
    }

    @Generated(hash = 1801898596)
    public SMSidMapping() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
