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
public class ServerDelete {

    @Id(autoincrement = true)
    private Long id;
    @Unique
    @NotNull
    private String server_id;
    @NotNull
    private String md5;
    private String contact_data;
    @NotNull
    private String openid;


    @Generated(hash = 68039926)
    public ServerDelete(Long id, @NotNull String server_id, @NotNull String md5,
            String contact_data, @NotNull String openid) {
        this.id = id;
        this.server_id = server_id;
        this.md5 = md5;
        this.contact_data = contact_data;
        this.openid = openid;
    }

    @Generated(hash = 177222488)
    public ServerDelete() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServer_id() {
        return server_id;
    }

    public void setServer_id(String server_id) {
        this.server_id = server_id;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getContact_data() {
        return contact_data;
    }

    public void setContact_data(String contact_data) {
        this.contact_data = contact_data;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
