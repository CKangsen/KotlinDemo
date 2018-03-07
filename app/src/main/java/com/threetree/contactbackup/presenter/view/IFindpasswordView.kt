package com.threetree.contactbackup.presenter.view

import com.threetree.contactbackup.ui.region.model.Country

import java.util.HashMap

/**
 * Created by hp on 2017/6/21.
 */

interface IFindpasswordView {
    fun onGetTudcSMSCodeCompleled()
    fun onGetTudcEmailCodeCompleled()
    fun onCheckEmailIsRegistedCompleted(isRegisted: Boolean)
    fun onCheckVerifyEmailCodeCompleled()
    fun onCheckVerifyCodeCompleled()
    fun onSetupPasswordCompleled()
    fun onPasswordVerifyCompleled()
    fun onPasswordModifyCompleled()
    fun onCheckPhoneNumberIsRegistedCompleled()
    fun onlogoutCompleled()
    fun onError(code: Int, errMsg: String)
    fun onGetCountryCodeCompleted(countryHashMap: HashMap<String, Country>)
}
