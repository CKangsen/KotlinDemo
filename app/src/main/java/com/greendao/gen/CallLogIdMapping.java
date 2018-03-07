package com.greendao.gen;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by cks on 17-12-13.
 */

@Entity
public class CallLogIdMapping {

    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private Integer calllog_id;
    @Unique
    @NotNull
    private String md5;
    @NotNull
    private String openid;

    @Keep
    public CallLogIdMapping(Long id, @NotNull Integer calllog_id,
            @NotNull String md5, @NotNull String openid) {
        this.id = id;
        this.calllog_id = calllog_id;
        this.md5 = md5;
        this.openid = openid;
    }

    public CallLogIdMapping() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCalllog_id() {
        return calllog_id;
    }

    public void setCalllog_id(Integer calllog_id) {
        this.calllog_id = calllog_id;
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
