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
public class IDMapping {

    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private Integer contact_id;
    @NotNull
    private Integer local_version;
    @Unique
    @NotNull
    private String server_id;
    @NotNull
    private Integer server_version;
    private String md5;
    @NotNull
    private String openid;
    @NotNull
    private Integer server_latest_version;

    @Keep
    public IDMapping(Long id, @NotNull Integer contact_id,
                     @NotNull Integer local_version, @NotNull String server_id,
                     @NotNull Integer server_version, String md5, @NotNull String openid,
                     @NotNull Integer server_latest_version) {
        this.id = id;
        this.contact_id = contact_id;
        this.local_version = local_version;
        this.server_id = server_id;
        this.server_version = server_version;
        this.md5 = md5;
        this.openid = openid;
        this.server_latest_version = server_latest_version;
    }

    public IDMapping() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getContact_id() {
        return contact_id;
    }

    public void setContact_id(Integer contact_id) {
        this.contact_id = contact_id;
    }

    public Integer getLocal_version() {
        return local_version;
    }

    public void setLocal_version(Integer local_version) {
        this.local_version = local_version;
    }

    public String getServer_id() {
        return server_id;
    }

    public void setServer_id(String server_id) {
        this.server_id = server_id;
    }

    public Integer getServer_version() {
        return server_version;
    }

    public void setServer_version(Integer server_version) {
        this.server_version = server_version;
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

    public Integer getServer_latest_version() {
        return server_latest_version;
    }

    public void setServer_latest_version(Integer server_latest_version) {
        this.server_latest_version = server_latest_version;
    }
}
