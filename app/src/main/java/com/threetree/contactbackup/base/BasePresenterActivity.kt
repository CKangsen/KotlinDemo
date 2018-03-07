package com.threetree.contactbackup.base

import android.os.Bundle


abstract class BasePresenterActivity<V, T : BasePresenter<V>> : BaseActivity() {
    protected var presenter: T? = null


    protected open fun onBaseCreate(savedInstanceState: Bundle) {
        presenter = createPresenter()
        if (null != presenter) {
            presenter!!.attachView(this as V)
        }
    }

    protected open fun initView(savedInstanceState: Bundle) {

    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != presenter) {
            presenter!!.detachView()
        }
    }

    protected abstract fun createPresenter(): T
}
