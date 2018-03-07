package com.threetree.contactbackup.model

import com.tencent.bugly.beta.Beta
import com.tencent.bugly.beta.UpgradeInfo



class CheckUpdaeModel : ICheckUpdateModel {

    val upgradeInfo: UpgradeInfo
        get() = Beta.getUpgradeInfo()

    fun checkUpdate() {
        Beta.checkUpgrade()
    }
}
