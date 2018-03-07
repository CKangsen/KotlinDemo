package com.threetree.contactbackup.defaultsms

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.text.TextUtils

import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.SmsInfo
import com.threetree.contactbackup.constant.WakaIcloudConstant
import com.threetree.contactbackup.eventbus.ReceiveLoginCode
import com.threetree.contactbackup.eventbus.ReceiveRegisterCode
import com.threetree.contactbackup.model.SMSRestoreModel
import com.threetree.contactbackup.util.LogUtils
import com.threetree.contactbackup.util.NotificationUtil

import org.greenrobot.eventbus.EventBus

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by cks on 17-11-9.
 */

class HandleSmsService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        LogUtils.i(TAG, "----onBind------")
        return null
    }

    override fun onCreate() {
        LogUtils.i(TAG, "----onCreate------")
        super.onCreate()
    }

    override fun onStart(intent: Intent?, startId: Int) {
        LogUtils.i(TAG, "----onStart------")
        super.onStart(intent, startId)
        if (intent != null) {
            val sms_address = intent.getStringExtra(WakaIcloudConstant.INSTANCE.getSMS_ADDRESS())
            val sms_content = intent.getStringExtra(WakaIcloudConstant.INSTANCE.getSMS_CONTENT())

            val msg = SmsInfo()
            msg.setBody(sms_content)
            msg.setAddress(sms_address)
            val time = System.currentTimeMillis()
            msg.setDate(time.toString())
            msg.setType(1.toString())
            val list = ArrayList<SmsInfo>()
            list.add(msg)
            SMSRestoreModel(list).doRestore()

            //通知栏显示
            NotificationUtil.showOrdinaryNotification(Factory.get().getApplicationContext(), sms_address,
                    sms_content, sms_content, NotificationUtil.SmallIcon,
                    NotificationUtil.DEFAULT_CHANNEL)

            //TODO:验证码自动填充
            //            if (isWakaVerifyCodeSms(sms_content)){
            //                if (getRunningActivityName(Factory.get().getApplicationContext()).equals(WakaIcloudConstant.VERIFYCODE_LOGIN_ACTIVITY_NAME)){
            //                    EventBus.getDefault().post(new ReceiveLoginCode(getDynamicPassword(sms_content)));
            //                } else if (getRunningActivityName(Factory.get().getApplicationContext()).equals(WakaIcloudConstant.VERIFYCODE_REGISTER_ACTIVITY_NAME)){
            //                    EventBus.getDefault().post(new ReceiveRegisterCode(getDynamicPassword(sms_content)));
            //                }
            //            }
        }
    }

    override fun onDestroy() {
        LogUtils.i(TAG, "----onDestroy------")
        super.onDestroy()
    }

    fun isWakaVerifyCodeSms(msg: String): Boolean {
        return if (!TextUtils.isEmpty(msg)) {
            if (msg.contains(WakaIcloudConstant.INSTANCE.getVERIFYCODE_SMS_FLAG_1()) || msg.contains(WakaIcloudConstant.INSTANCE.getVERIFYCODE_SMS_FLAG_2())) {
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    fun getRunningActivityName(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        //完整类名
        val runningActivity = activityManager.getRunningTasks(1)[0].topActivity.className
        return runningActivity.substring(runningActivity.lastIndexOf(".") + 1)
    }

    fun getDynamicPassword(str: String): String {
        val continuousNumberPattern = Pattern.compile("[0-9\\.]+")
        val m = continuousNumberPattern.matcher(str)
        var dynamicPassword = ""
        while (m.find()) {
            if (m.group().length == 6) {
                dynamicPassword = m.group()
            }
        }

        return dynamicPassword
    }

    companion object {
        private val TAG = HandleSmsService::class.java.simpleName
    }
}
