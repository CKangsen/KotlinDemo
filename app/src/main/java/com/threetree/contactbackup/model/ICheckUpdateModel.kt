package com.threetree.contactbackup.model

import com.tencent.bugly.beta.UpgradeInfo


interface ICheckUpdateModel {
    val upgradeInfo: UpgradeInfo
    fun checkUpdate()
}
