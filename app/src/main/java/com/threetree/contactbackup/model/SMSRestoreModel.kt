package com.threetree.contackbackup.model

import com.threetree.contackbackup.Factory
import com.threetree.contackbackup.bean.SmsInfo
import com.threetree.contackbackup.db.DBManager
import com.threetree.contackbackup.db.GreenDaoDBManager
import com.threetree.contackbackup.model.listener.SMSRestoreFinishListener


import org.reactivestreams.Subscriber

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class SMSRestoreModel {
    internal var mRestoreList: List<SmsInfo>? = null
    private var mDBManager: GreenDaoDBManager? = null

    constructor(restoreList: List<SmsInfo>) {
        mRestoreList = restoreList
        mDBManager = Factory.Companion.get().getDBManager()
    }

    constructor() {
        mDBManager = Factory.Companion.get().getDBManager()
    }

    fun setrestoreList(restoreList: List<SmsInfo>) {
        mRestoreList = restoreList
    }

    fun doRestore() {
        mDBManager!!.insertSMS(mRestoreList)
    }

    fun doRestore(smsRestoreFinishListener: SMSRestoreFinishListener) {
        Thread(Runnable {
            if (mRestoreList == null || mRestoreList!!.size <= 0) {
                smsRestoreFinishListener.finish()
                return@Runnable
            }
            mDBManager!!.insertSMS(mRestoreList)
            smsRestoreFinishListener.finish()
        }).start()

    }

    fun doRestore(obserer: Observer<*>) {
        Observable.create<List<SmsInfo>>(ObservableOnSubscribe<List<Any>> { e ->
            if (mRestoreList == null || mRestoreList!!.size <= 0) {
                e.onComplete()
                return@ObservableOnSubscribe
            }
            e.onNext(mRestoreList)
            e.onComplete()
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<List<SmsInfo>> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(o: List<SmsInfo>) {
                        mDBManager!!.insertSMS(o)
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {
                        obserer.onComplete()
                    }

                })


    }
}
