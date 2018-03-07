package com.threetree.contactbackup.base

import java.lang.ref.Reference
import java.lang.ref.WeakReference

abstract class BasePresenter<T> {
    protected var mReference: Reference<T>? = null

    /**
     * 判断界面是否销毁
     */
    protected val isViewAttached: Boolean
        get() = mReference != null && mReference!!.get() != null


    /**
     * 获取界面的引用
     *
     * @return
     */

    protected val view: T?
        get() = if (mReference == null) null else mReference!!.get()

    fun attachView(view: T) {
        mReference = WeakReference(view)
    }

    fun detachView() {
        if (mReference != null) {
            mReference!!.clear()
            mReference = null
        }
    }


}
