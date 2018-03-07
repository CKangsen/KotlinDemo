package com.threetree.contackbackup.model

import android.app.Activity
import android.content.Intent

import com.afmobi.tudcsdk.CallbackManager
import com.threetree.contackbackup.constant.WakaIcloudConstant
import com.threetree.contackbackup.model.listener.DataCenterListener
import com.threetree.contackbackup.ui.login.LoginActivity


class LoginModel {
    private var mCallbackManager: CallbackManager? = null


    fun login(activity: Activity, onTudcLogninResponListener: DataCenterListener.OnTudcLogninResponListener) {
        if (mCallbackManager == null) {
            mCallbackManager = CallbackManager.Factory.create()
            //            TUDCLoginManager.getInstance().registerCallback(mCallbackManager, new TudcCallback() {
            //                @Override
            //                public void onSuccess(String authentication) {
            //                    if (!TextUtils.isEmpty(authentication)) {
            //                        onTudcLogninResponListener.logninSuccess(authentication);
            //                    }
            //                }
            //
            //                @Override
            //                public void onCancel(int cancelCode) {
            //                    if (cancelCode == TUDCLoginManager.SWITCHACCOUNT_CANCELCODE) {
            //                        TUDCLoginManager.getInstance().logIn(activity, LoginBehavior.SDK_SLIENT_AUTHORIZE, TUDCLoginManager.LOGIN_TYPE_SHOW);
            //                    }
            //                    onTudcLogninResponListener.logninCancel();
            //                }
            //
            //                @Override
            //                public void onError(TudcsdkException error) {
            //                    onTudcLogninResponListener.logninFailed( error.toString());
            //                }
            //            });
        }
        //        TUDCLoginManager.getInstance().logIn(activity, LoginBehavior.SDK_PHONE_IDENTIFYING_CODE_AUTHORIZE, TUDCLoginManager.LOGIN_TYPE_SHOW);
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        activity.startActivityForResult(intent, WakaIcloudConstant.INSTANCE.getICLOUD_LOGIN_RESULT_CODE())
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (mCallbackManager != null) {
            mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }
}
