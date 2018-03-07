package com.threetree.contactbackup.model

import com.afmobi.tudcsdk.internal.bean.LoginProfile
import com.afmobi.tudcsdk.login.TUDCSdkInnerManager



class ManagerCenterModel : IManagerCenterModel {
    val loginProfile: LoginProfile?
        get() = if (TUDCSdkInnerManager.getManager().isExistTgt(false)) {
            TUDCSdkInnerManager.getManager().getLocalProfile()
        } else null

    fun gotoPassword() {

    }
}
