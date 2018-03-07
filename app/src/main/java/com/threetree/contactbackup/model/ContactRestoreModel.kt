package com.threetree.contactbackup.model

import android.text.TextUtils

import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.bean.CallLogBean
import com.threetree.contactbackup.bean.ContactBean
import com.threetree.contactbackup.db.DBManager
import com.threetree.contactbackup.db.GreenDaoDBManager
import com.threetree.contactbackup.model.listener.ContactRestoreFinishListener
import com.threetree.contactbackup.util.IdPatternUtils
import com.threetree.contactbackup.util.LogUtils

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class ContactRestoreModel {

    internal var mRestoreList: List<ContactBean>? = null
    internal var mUpdateList: List<ContactBean>? = null
    private var mDBManager: GreenDaoDBManager? = null
    private var mContactRestoreFinishListener: ContactRestoreFinishListener? = null

    constructor(restoreList: List<ContactBean>, updateList: List<ContactBean>, contactRestoreFinishListener: ContactRestoreFinishListener) {
        mRestoreList = restoreList
        mUpdateList = updateList
        mDBManager = Factory.get()!!.dbManager
        mContactRestoreFinishListener = contactRestoreFinishListener
    }

    constructor(restoreList: List<ContactBean>, updateList: List<ContactBean>) {
        mRestoreList = restoreList
        mUpdateList = updateList
        mDBManager = Factory.get()!!.dbManager
        //mContactRestoreFinishListener = contactRestoreFinishListener;
    }

    fun doRestore() {

        Thread(Runnable {
            if (mRestoreList != null) {
                val initTime = System.currentTimeMillis()
                mDBManager!!.batchInsertContactsIntoPhone(mRestoreList)
                val endtime = System.currentTimeMillis()
                LogUtils.d(TAG, "onCommit mRestoreList:" + (endtime - initTime))
            }

            if (mUpdateList != null) {
                val initTime = System.currentTimeMillis()
                mDBManager!!.batchUpdateContactsIntoPhone(mUpdateList)
                val endtime = System.currentTimeMillis()
                LogUtils.d(TAG, "onCommit mUpdateList:" + (endtime - initTime))
            }
            mContactRestoreFinishListener!!.finish()
        }).start()


    }

    fun doRestore(obserer: Observer<*>) {
        Observable.create(ObservableOnSubscribe<Any> { e ->
            e.onNext("no")
            e.onComplete()
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : Observer<Any> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(o: Any) {
                        if (mRestoreList != null) {
                            val initTime = System.currentTimeMillis()
                            mDBManager!!.batchInsertContactsIntoPhone(mRestoreList)
                            val endtime = System.currentTimeMillis()
                            LogUtils.d(TAG, "onCommit mRestoreList:" + (endtime - initTime))
                        }

                        if (mUpdateList != null) {
                            val initTime = System.currentTimeMillis()
                            mDBManager!!.batchUpdateContactsIntoPhone(mUpdateList)
                            val endtime = System.currentTimeMillis()
                            LogUtils.d(TAG, "onCommit mUpdateList:" + (endtime - initTime))
                        }
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {
                        obserer.onComplete()
                    }

                })
    }

    companion object {

        val TAG = ContactRestoreModel::class.java.simpleName
    }
}
