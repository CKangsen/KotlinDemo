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
public class LocalUpdateConflictContact {

    @Id(autoincrement = true)
    private Long id;
    @NotNull
    @Unique
    private Integer contact_id;
    @NotNull
    private String openid;

    @Keep
    public LocalUpdateConflictContact(Long id, @NotNull Integer contact_id,
            @NotNull String openid) {
        this.id = id;
        this.contact_id = contact_id;
        this.openid = openid;
    }

    @Keep
    public LocalUpdateConflictContact() {
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

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
