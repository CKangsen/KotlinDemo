package com.threetree.contactbackup.model

import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.CallLogBean

import com.threetree.contactbackup.db.GreenDaoDBManager
import com.threetree.contactbackup.model.listener.CallLogRestoreFinishListener

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class CallLogRestoreModel {

    internal var mRestoreList: List<CallLogBean>? = null
    private var mDBManager: GreenDaoDBManager? = null

    constructor(restoreList: List<CallLogBean>) {
        mRestoreList = restoreList
        mDBManager = Factory.get()!!.dbManager
    }

    constructor() {
        mDBManager = Factory.get()!!.dbManager
    }

    fun setRestoreList(restoreList: List<CallLogBean>) {
        mRestoreList = restoreList
    }

    fun doRestore() {
        if (mRestoreList == null || mRestoreList!!.size <= 0) {
            return
        }
        for (callLogBean in mRestoreList!!) {
            mDBManager!!.insertOneCallLogIntoPhone(callLogBean)
        }
    }

    fun doRestore(callLogRestoreFinishListener: CallLogRestoreFinishListener) {
        Thread(Runnable {
            if (mRestoreList == null || mRestoreList!!.size <= 0) {
                callLogRestoreFinishListener.finish()
                return@Runnable
            }
            for (callLogBean in mRestoreList!!) {
                mDBManager!!.insertOneCallLogIntoPhone(callLogBean)
            }
            callLogRestoreFinishListener.finish()
        }).start()

    }

    fun doRestore(obserer: Observer<*>) {
        Observable.create(ObservableOnSubscribe<List<CallLogBean>> { e ->
            if (mRestoreList == null || mRestoreList!!.size <= 0) {
                e.onComplete()
                return@ObservableOnSubscribe
            }
            e.onNext(mRestoreList!!)
            e.onComplete()
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<List<CallLogBean>> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(o: List<CallLogBean>) {
                        for (callLogBean in o) {
                            mDBManager!!.insertOneCallLogIntoPhone(callLogBean)
                        }
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {
                        obserer.onComplete()
                    }

                })
    }
}
