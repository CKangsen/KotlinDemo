
package com.threetree.contactbackup.base

import android.os.Bundle


abstract class BasePrsenterFragment<V, T : BasePresenter<V>> : BaseFragment() {
    protected var presenter: T? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = createPresenter()
        if (null != presenter) {
            presenter!!.attachView(this as V)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != presenter) {
            presenter!!.detachView()
        }
    }

    protected abstract fun createPresenter(): T
}
