package com.threetree.contactbackup.model

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message


import com.threetree.contactbackup.model.listener.GetCountryCodeListener
import com.threetree.contactbackup.ui.region.model.Country
import com.threetree.contactbackup.ui.region.model.RegionData

import java.util.HashMap


class CountryCodeModel(private var mContext: Context?, getCountryCodeListener: GetCountryCodeListener) : ICountryCodeModel {
    private val GETCOUNTRYCODE = 1001
    private val INIT_FINISH = 1002
    private var mLooperThread: LooperThread? = null
    private var mGetCountryCodeListener: GetCountryCodeListener? = null
    private var isInit: Boolean = false

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                INIT_FINISH -> mGetCountryCodeListener!!.onInitCompleted()
                GETCOUNTRYCODE -> try {
                    mGetCountryCodeListener!!.onGetCountryCodeCompleted(msg.obj as HashMap<String, Country>)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                else -> {
                }
            }
        }
    }

    init {
        mLooperThread = LooperThread()
        mLooperThread!!.start()
        mGetCountryCodeListener = getCountryCodeListener
    }

    internal inner class LooperThread : Thread() {
        var handler: Handler? = null
        var looper: Looper? = null
        override fun run() {
            Looper.prepare()
            looper = Looper.myLooper()
            handler = object : Handler() {

                override fun handleMessage(msg: Message) {
                    if (!isInit) {
                        isInit = true
                    }
                    when (msg.what) {
                        GETCOUNTRYCODE -> doGetCountryCode()
                        else -> {
                        }
                    }
                }
            }
            if (!isInit) {
                mHandler.sendEmptyMessage(INIT_FINISH)
            }
            Looper.loop()

        }
    }


    private fun doGetCountryCode() {
        val msg = Message.obtain()
        msg.what = GETCOUNTRYCODE
        msg.obj = RegionData.getInstance().getCountryCodeHash(mContext)
        mHandler.sendMessage(msg)
    }


    fun getCountryCode() {
        if (mLooperThread != null && mLooperThread!!.handler != null) {
            val msg = Message.obtain()
            msg.what = GETCOUNTRYCODE
            mLooperThread!!.handler!!.sendMessage(msg)
        }
    }

    fun clear() {
        mGetCountryCodeListener = null
        this.mContext = null
        mLooperThread = null

    }
}
