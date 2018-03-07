package com.afmobi.wakacloud.presenter

import android.content.Intent
import android.text.TextUtils

import com.afmobi.tudcsdk.internal.bean.LoginProfile
import com.afmobi.tudcsdk.login.TUDCSdkInnerManager
import com.afmobi.tudcsdk.login.model.listener.TudcInnerListener
import com.afmobi.tudcsdk.utils.ValidateUtil
import com.afmobi.wakacloud.R
import com.afmobi.wakacloud.base.BaseActivity
import com.afmobi.wakacloud.base.BasePresenter
import com.afmobi.wakacloud.midcore.WKCInstance
import com.afmobi.wakacloud.model.CheckUpdaeModel
import com.afmobi.wakacloud.model.DataCenterModel
import com.afmobi.wakacloud.model.ICheckUpdateModel
import com.afmobi.wakacloud.model.IManagerCenterModel
import com.afmobi.wakacloud.model.LoginModel
import com.afmobi.wakacloud.model.ManagerCenterModel
import com.afmobi.wakacloud.model.listener.DataCenterListener
import com.afmobi.wakacloud.presenter.view.IManagerCenterView
import com.tencent.bugly.beta.UpgradeInfo


class ManagerCenterPresenter : BasePresenter<IManagerCenterView>() {
    private val iManagerCenterModel: IManagerCenterModel
    private val iCheckUpdateModel: ICheckUpdateModel
    private var loginProfile: LoginProfile? = null
    private val loginModel: LoginModel?
    private val mDataCenterModel: DataCenterModel

    init {
        iManagerCenterModel = ManagerCenterModel()
        iCheckUpdateModel = CheckUpdaeModel()
        loginModel = LoginModel()
        mDataCenterModel = DataCenterModel()
    }

    /**
     * 获取用户Profile资料  如果未登录 名字位置显示登录提示
     */
    fun getLoginProfile() {
        if (!WKCInstance.getDefaultInstance().Checklogin()) {
            getView().updateNeedLogin(R.string.click_to_login)

        } else {
            loginProfile = iManagerCenterModel.getLoginProfile()
            if (isViewAttached()) {
                if (loginProfile != null) {
                    getView().updateName(loginProfile!!.getUserPhone())
                } else {
                    getView().updateNeedLogin(R.string.click_to_login)
                }
            }
        }
    }

    /**
     * 获取新版本升级信息
     */
    fun getUpgradeInfo() {
        val upgradeInfo = iCheckUpdateModel.getUpgradeInfo()
        if (upgradeInfo != null) {
            getView().setUpgradeInfo(upgradeInfo!!.versionName)
        }
    }

    fun checkUpdate() {
        iCheckUpdateModel.checkUpdate()
    }

    fun gotoResetPassword() {
        if (loginProfile != null) {
            getView().gotoResetPassword(loginProfile!!.getUserPhone(), loginProfile!!.getEmail())
        } else {
            // getView().showToast(R.string.click_to_login);
        }
    }

    fun logOut() {
        mDataCenterModel.logout(object : DataCenterListener.OnlogOutResponListener() {
            fun logOutSuccess() {
                tudcLogout()
            }

            fun logOutFailed(code: Int, msg: String) {
                if (isViewAttached()) {
                    getView().logOutFailed(code, msg)
                }
            }
        })
    }

    private fun tudcLogout() {
        TUDCSdkInnerManager.getManager().logOut(object : TudcInnerListener.OnLogoutTudcListener() {
            fun onLogoutSuccess() {
                if (isViewAttached()) {
                    getView().updateLogOutView()
                }
            }

            fun onLogoutFail(i: Int, s: String) {
                if (isViewAttached()) {
                    getView().updateLogOutView()
                }
            }
        })
        getView().updateLogOutView()
    }

    /**
     * 进入Profile页  如果未登录 进入登录页
     * @param activity
     */
    fun gotoProfile(activity: BaseActivity) {
        if (ValidateUtil.isExistTgt(false)) {
            TUDCSdkInnerManager.getManager().getStByTgt(object : TudcInnerListener.TudcGetUserStListener() {
                fun onTudcGetUserStSuccess(st: String, uname: String, avatar: String) {
                    if (!TextUtils.isEmpty(st)) {
                        /*TUDC登录成功后 调取App的login*/
                        login(st)
                    }
                }

                fun onTudcGetUserStError(i: Int, s: String) {
                    if (isViewAttached()) {
                        getView().loginFailed(s)
                    }
                }
            })
        } else {
            loginModel!!.login(activity, object : DataCenterListener.OnTudcLogninResponListener() {
                fun logninSuccess(st: String) {
                    login(st)
                }

                fun logninCancel() {

                }

                fun logninFailed(msg: String) {
                    if (isViewAttached()) {
                        getView().loginFailed(msg)
                    }
                }
            })
        }
    }

    private fun login(st: String) {
        mDataCenterModel.lognin(st, object : DataCenterListener.OnlogninResponListener() {
            fun logninSuccess() {
                if (isViewAttached()) {
                    getView().loginSuccess()
                }
            }

            fun logninFailed(Code: Int, msg: String) {
                if (isViewAttached()) {
                    getView().loginFailed(msg)
                }
            }
        })
    }

    /*Tudc Login的Activity返回*/
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (loginModel != null) {
            loginModel!!.onActivityResult(requestCode, resultCode, data)
        }
    }
}
