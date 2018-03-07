package com.threetree.contactbackup.presenter

import android.content.Context

import com.threetree.contactbackup.base.BasePresenter
import com.threetree.contactbackup.model.CountryCodeModel
import com.threetree.contactbackup.model.ICountryCodeModel
import com.threetree.contactbackup.model.listener.GetCountryCodeListener
import com.threetree.contactbackup.presenter.view.IFindpasswordView
import com.afmobi.tudcsdk.login.TUDCSdkInnerManager
import com.afmobi.tudcsdk.login.model.listener.TudcInnerListener
import com.afmobi.tudcsdk.midcore.Consts
import com.threetree.contactbackup.ui.region.model.Country

import java.util.HashMap


class FindpasswordPresenter(context: Context) : BasePresenter<IFindpasswordView>() {
    //    IFindpasswordModel findPwModel;
    //    ICheckPhoneNumberIsRegisted checkPhoneNumberIsRegisted;
    //    ILogoutModel logoutModel;
    internal var iCountryCodeModel: ICountryCodeModel

    init {
        iCountryCodeModel = CountryCodeModel(context, object : GetCountryCodeListener() {
            fun onGetCountryCodeCompleted(countryHashMap: HashMap<String, Country>) {
                if (getView() != null) {
                    getView().onGetCountryCodeCompleted(countryHashMap)
                }
            }

            fun onInitCompleted() {
                iCountryCodeModel.getCountryCode()
            }
        })
    }

    fun getSMSCode(phone: String, cc: String) {
        TUDCSdkInnerManager.getManager().getSmscodeforForgotPassword(phone, cc, object : TudcInnerListener.GetTudcSMSCodeListener() {
            fun onGetTudcSMSCodeSuccess() {
                if (getView() != null) {
                    getView().onGetTudcSMSCodeCompleled()
                }
            }

            fun onGetTudcSMSCodeError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })
    }

    fun logout() {
        TUDCSdkInnerManager.getManager().logOut(object : TudcInnerListener.OnLogoutTudcListener() {
            fun onLogoutSuccess() {
                if (getView() != null) {
                    getView().onlogoutCompleled()
                }
            }

            fun onLogoutFail(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })
        if (getView() != null) {
            getView().onlogoutCompleled()
        }
    }

    //    public void getCountryCode(){
    //        countryCode.getCountryCode();
    //    }
    //
    fun checkEmailIsRegisted(email: String) {
        TUDCSdkInnerManager.getManager().checkEmailIsRegisted(email, object : TudcInnerListener.TudcCheckEmailIsRegistedListener() {
            fun onTudcCheckEmailIsRegistedCompleled(b: Boolean) {
                if (getView() != null) {
                    getView().onCheckEmailIsRegistedCompleted(b)
                }
            }

            fun onTudcCheckEmailIsRegistedError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })
    }

    fun getEmailCode(email: String) {
        TUDCSdkInnerManager.getManager().getEmailcodeForForgotPassword(email, object : TudcInnerListener.GetTudcEmaiCodeListener() {
            fun onGetTudcEmaiCodeCompleled() {
                if (getView() != null) {
                    getView().onGetTudcEmailCodeCompleled()
                }
            }

            fun onGetTudcEmaiCodeError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })
    }

    fun checkPhoneNumberIsRegisted(phone: String, cc: String) {
        TUDCSdkInnerManager.getManager().checkPhoneNumberIsRegisted(phone, cc, object : TudcInnerListener.TudcCheckPhoneNumberIsRegistedListener() {
            fun onTudcCheckPhoneNumberIsRegistedCompleled(b: Boolean) {
                if (b) {
                    if (getView() != null) {
                        getView().onCheckPhoneNumberIsRegistedCompleled()
                    }
                } else {
                    if (getView() != null) {
                        getView().onError(Consts.REQ_CODE_REG_PHONE_HAVE_REGISTERED, "")
                    }
                }
            }

            fun onTudcCheckPhoneNumberIsRegistedError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onCheckPhoneNumberIsRegistedCompleled()
                }

            }
        })
    }

    fun CheckVerifyCode(phone: String, cc: String, captcha: String) {
        TUDCSdkInnerManager.getManager().checkSmscodeForForgotPassword(phone, cc, captcha, object : TudcInnerListener.TudcCheckVerifyCodeListener() {
            fun onCheckVerifyCodeSuccess() {
                if (getView() != null) {
                    getView().onCheckVerifyCodeCompleled()
                }
            }

            fun onCheckVerifyCodeError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })

    }

    fun CheckVerifyEmailCode(eamil: String, captcha: String) {
        TUDCSdkInnerManager.getManager().checkEmailcodeForForgotPassword(eamil, captcha, object : TudcInnerListener.TudcCheckVerifyCodeListener() {
            fun onCheckVerifyCodeSuccess() {
                if (getView() != null) {
                    getView().onCheckVerifyEmailCodeCompleled()
                }
            }

            fun onCheckVerifyCodeError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })
    }


    fun passwordVerify(pwd: String) {
        TUDCSdkInnerManager.getManager().passwordVerifyForResetPassword(pwd, object : TudcInnerListener.TudcPasswordVerifyListener() {
            fun onPasswordVerifySuccess() {
                if (getView() != null) {
                    getView().onPasswordVerifyCompleled()
                }
            }

            fun onPasswordVerifyError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })
        //        findPwModel.onPasswordVerify( pwd, request_id, request_id, new TudcInnerListener.TudcPasswordVerifyListener() {
        //
        //            @Override
        //            public void onPasswordVerifySuccess() {
        //                if(getView() != null){
        //                    getView().onPasswordVerifyCompleled();
        //                }
        //            }
        //
        //            @Override
        //            public void onPasswordVerifyError(int result_code, String msg) {
        //                if(getView() != null){
        //                    getView().onError(result_code,msg);
        //                }
        //            }
        //        });
    }


    fun passwordModify(oldPwd: String, newPwd: String) {
        TUDCSdkInnerManager.getManager().modifyPassword(oldPwd, newPwd, object : TudcInnerListener.TudcPasswordModifyListener() {
            fun onPasswordModifySuccess() {
                if (getView() != null) {
                    getView().onPasswordModifyCompleled()
                }
            }

            fun onPasswordModifyError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })

        //        findPwModel.onPasswordModify(oldPwd,newPwd , request_id, request_id, new TudcInnerListener.TudcPasswordModifyListener() {
        //
        //
        //            @Override
        //            public void onPasswordModifySuccess() {
        //                if(getView() != null){
        //                    getView().onPasswordModifyCompleled();
        //                }
        //            }
        //
        //            @Override
        //            public void onPasswordModifyError(int result_code, String msg) {
        //                if(getView() != null){
        //                    getView().onError(result_code,msg);
        //                }
        //            }
        //        });

    }

    fun setupPasswordByEmail(account: String, cc: String, newPwd: String, captcha: String) {
        TUDCSdkInnerManager.getManager().resetPassWordByEmail(account, cc, newPwd, captcha, object : TudcInnerListener.TudcPasswordResetListener() {
            fun onPasswordResetCompleled() {
                if (getView() != null) {
                    getView().onSetupPasswordCompleled()
                }
            }

            fun onPasswordResetError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })
    }

    fun setupPasswordByPhone(account: String, cc: String, newPwd: String, captcha: String) {
        TUDCSdkInnerManager.getManager().resetPassWordByPhone(account, cc, newPwd, captcha, object : TudcInnerListener.TudcPasswordResetListener() {
            fun onPasswordResetCompleled() {
                if (getView() != null) {
                    getView().onSetupPasswordCompleled()
                }
            }

            fun onPasswordResetError(i: Int, s: String) {
                if (getView() != null) {
                    getView().onError(i, s)
                }
            }
        })
    }
}
