package com.threetree.contactbackup.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.app.NotificationCompat


import com.threetree.contactbackup.R

import java.util.Random




class NotificationUtil {

    fun setIconId(iconId: Int) {
        this.iconId = iconId
    }

    companion object {

        val SmallIcon = R.mipmap.ic_launcher
        val NotificationNumber = 1
        val DEFAULT_CHANNEL = 101
        private var mManager: NotificationManager? = null
        private var mBuilder: NotificationCompat.Builder? = null
        private var mNotificationBuilder: Notification.Builder? = null
        private val RANDOM = Random()
        /**
         * 获取Builder
         */
        fun getBuilder(context: Context): NotificationCompat.Builder {
            mBuilder = NotificationCompat.Builder(context)
            mBuilder!!.setWhen(System.currentTimeMillis())
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
            return mBuilder
        }

        /**
         * 获取Builder
         */
        fun getNotificationBuilder(context: Context): Notification.Builder {
            mNotificationBuilder = Notification.Builder(context)
            mNotificationBuilder!!.setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
            return mNotificationBuilder
        }

        /**
         * 获取NotificationManager
         */
        fun getManager(context: Context): NotificationManager {

            if (mManager == null) {
                synchronized(NotificationUtil::class.java) {
                    if (mManager == null) {
                        mManager = context.getSystemService(context.NOTIFICATION_SERVICE) as NotificationManager
                    }
                }
            }
            return mManager
        }


        /**
         * 显示普通的通知
         */
        fun showOrdinaryNotification(context: Context, title: String, text: String, ticker: String,
                                     icon: Int, channel: Int) {

            val sdk = Build.VERSION.SDK_INT
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                mBuilder = getBuilder(context)
                mManager = getManager(context)
                mBuilder!!.setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(getDefalutIntent(context, Notification.FLAG_AUTO_CANCEL))
                        //.setNumber(NotificationNumber)//显示数量
                        .setTicker(ticker)//通知首次出现在通知栏，带上升动画效果的，可设置文字，图标
                        .setWhen(System.currentTimeMillis())//通知产生的时间
                        .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
                        .setAutoCancel(true)//设置让通知将自动取消
                        .setOngoing(false)//ture，设置他为一个正在进行的通知。如一个文件下载,网络连接。
                        .setDefaults(Notification.DEFAULT_SOUND)//向通知添加声音、闪灯和振动效果。最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 //DEFAULT_VIBRATE requires VIBRATE permission
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources, SmallIcon))
                        .setSmallIcon(SmallIcon)
                val mNotification = mBuilder!!.build()
                mNotification.icon = icon
                mManager!!.notify(dealWithId(channel), mNotification)
            } else {
                //多行通知显示
                mNotificationBuilder = getNotificationBuilder(context)
                mManager = getManager(context)

                mNotificationBuilder!!.setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(getDefalutIntent(context, Notification.FLAG_AUTO_CANCEL))
                        .setTicker(ticker)//通知首次出现在通知栏，带上升动画效果的，可设置文字，图标
                        .setWhen(System.currentTimeMillis())//通知产生的时间
                        .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
                        .setAutoCancel(true)//设置让通知将自动取消
                        .setOngoing(false)//ture，设置他为一个正在进行的通知。如一个文件下载,网络连接。
                        .setDefaults(Notification.DEFAULT_SOUND)//向通知添加声音、闪灯和振动效果。最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 //DEFAULT_VIBRATE requires VIBRATE permission
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources, SmallIcon))
                        .setSmallIcon(SmallIcon)
                val mNotification = Notification.BigTextStyle(mNotificationBuilder).bigText(text).build()
                mNotification.icon = icon
                mManager!!.notify(dealWithId(channel), mNotification)
            }


        }

        /**
         * 显示普通图片的通知
         */
        fun showPicNotification(context: Context, title: String, text: String, ticker: String,
                                icon: Int, channel: Int, pictureStyle: android.support.v7.app.NotificationCompat.BigPictureStyle) {
            mBuilder = getBuilder(context)

            mManager = getManager(context)
            mBuilder!!.setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(getDefalutIntent(context, Notification.FLAG_AUTO_CANCEL))
                    //.setNumber(NotificationNumber)//显示数量
                    .setTicker(ticker)//通知首次出现在通知栏，带上升动画效果的，可设置文字，图标
                    .setWhen(System.currentTimeMillis())//通知产生的时间
                    .setPriority(Notification.PRIORITY_DEFAULT)//设置该通知优先级
                    .setAutoCancel(true)//设置让通知将自动取消
                    .setOngoing(false)//ture，设置他为一个正在进行的通知。如一个文件下载,网络连接。
                    .setDefaults(Notification.DEFAULT_SOUND)//向通知添加声音、闪灯和振动效果。最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
                    //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 //DEFAULT_VIBRATE requires VIBRATE permission
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, SmallIcon))
                    .setSmallIcon(SmallIcon)
            mBuilder!!.setStyle(pictureStyle)
            val mNotification = mBuilder!!.build()
            mNotification.icon = icon
            mManager!!.notify(dealWithId(channel), mNotification)
        }

        /**
         * 带意图的通知栏：Intent 中可以包含很多参数、功能
         * 应用场景：页面启动、跳转、安装apk
         */
        fun showIntentNotification(context: Context, title: String, text: String, ticker: String,
                                   resultIntent: Intent, icon: Int, channel: Int, defaults: Int) {
            val RequestCode = -1
            mBuilder = getBuilder(context)
            mManager = getManager(context)
            val pendingIntent = PendingIntent.getActivity(context, RequestCode, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            mBuilder!!.setContentTitle(title)
                    .setContentText(text)
                    .setTicker(ticker)
                    .setSmallIcon(SmallIcon)
                    .setContentIntent(pendingIntent)
            if (defaults > 0) {
                mBuilder!!.setDefaults(Notification.DEFAULT_SOUND)
            }
            val mNotification = mBuilder!!.build()
            mNotification.icon = icon
            mManager!!.notify(dealWithId(channel), mNotification)

        }

        //获取默认的延期意图
        fun getDefalutIntent(context: Context, flags: Int): PendingIntent {
            val intent = Intent()
            return PendingIntent.getActivity(context, 1, intent, flags)
        }

        //通知channel ID，唯一标示一个通知
        fun dealWithId(channel: Int): Int {
            return if (channel >= 1 && channel <= 100) channel else RANDOM.nextInt(Integer.MAX_VALUE - 100) + 101
        }

        //获取系统SDK版本
        val systemVersion: Int
            get() = Build.VERSION.SDK_INT

        /**
         * 清除所有的通知
         * @param context
         */
        fun clearAllNotifification(context: Context) {
            mManager = getManager(context)
            mManager!!.cancelAll()
        }

        /**
         * 清除通知
         */
        fun clearNotifificationById(context: Context, channel: Int) {
            mManager = getManager(context)
            mManager!!.cancel(dealWithId(channel))
        }

        private var iconId = 0
    }


}
