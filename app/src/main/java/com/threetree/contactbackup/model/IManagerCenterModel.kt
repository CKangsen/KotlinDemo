package com.threetree.contactbackup.model

import com.afmobi.tudcsdk.internal.bean.LoginProfile



interface IManagerCenterModel {
    val loginProfile: LoginProfile
    fun gotoPassword()
}
