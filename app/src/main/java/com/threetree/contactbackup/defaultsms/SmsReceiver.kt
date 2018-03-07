package com.threetree.contactbackup.defaultsms

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage

import com.threetree.contactbackup.bean.SmsInfo
import com.threetree.contactbackup.constant.WakaIcloudConstant
import com.threetree.contactbackup.model.SMSRestoreModel
import com.threetree.contactbackup.util.NotificationUtil

import java.util.ArrayList



class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        //使用pdu秘钥来提取一个pdus数组
        val pdus = bundle!!.get("pdus") as Array<Any>

        val messages = arrayOfNulls<SmsMessage>(pdus.size)
        for (i in messages.indices) {
            messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
        }

        //获取发送方号码
        val address = messages[0].getOriginatingAddress()

        //获取短信内容
        var fullMessage = ""
        for (message in messages) {
            fullMessage += message.getMessageBody()
        }

        val i = Intent()
        i.component = ComponentName(context.packageName, "com.threetree.contactbackup.defaultsms.HandleSmsService")
        i.`package` = context.packageName
        i.action = WakaIcloudConstant.INSTANCE.getHANDLE_SMS_SERVICE_ACTION()
        i.flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
        i.putExtra(WakaIcloudConstant.INSTANCE.getSMS_ADDRESS(), address)
        i.putExtra(WakaIcloudConstant.INSTANCE.getSMS_CONTENT(), fullMessage)
        context.startService(i)
        //        在Service下执行以下操作
        //        SmsInfo  msg=new SmsInfo();
        //        msg.setBody(fullMessage);
        //        msg.setAddress(address);
        //        long time=System.currentTimeMillis();
        //        msg.setDate(String.valueOf(time));
        //        msg.setType(String.valueOf(1));
        //        List<SmsInfo> list=new ArrayList<SmsInfo>();
        //        list.add(msg);
        //        new SMSRestoreModel(list).doRestore();
        //
        //        //通知栏显示
        //        NotificationUtil.showOrdinaryNotification(context,address,fullMessage,fullMessage,NotificationUtil.SmallIcon,
        //                NotificationUtil.DEFAULT_CHANNEL);
    }
}
