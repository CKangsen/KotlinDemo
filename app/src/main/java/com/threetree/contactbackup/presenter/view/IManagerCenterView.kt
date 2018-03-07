package com.threetree.contactbackup.presenter.view


interface IManagerCenterView {
    fun updateName(name: String)
    fun updateNeedLogin(resId: Int)
    fun gotoResetPassword(phone: String, email: String)
    fun showToast(resId: Int)
    fun updateLogOutView()
    fun setUpgradeInfo(version: String)
    fun loginSuccess()
    fun loginFailed(msg: String)
    fun logOutFailed(code: Int, msg: String)
}
