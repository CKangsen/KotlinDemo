package com.threetree.contactbackup.base

import android.os.Bundle
import android.support.v4.app.Fragment


abstract class BaseFragment : Fragment() {
    protected lateinit var mBackHandledInterface: BackHandledInterface

    /**
     * 在baseFragement中实现返回按键
     *
     * @return
     */
    abstract fun onBackPressed(): Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity !is BackHandledInterface) {
            throw ClassCastException("Hosting Activity must implement BackHandledInterface")
        } else {
            this.mBackHandledInterface = activity as BackHandledInterface
        }
    }

    override fun onResume() {
        super.onResume()
        //告诉FragmentActivity，当前Fragment在栈顶
        mBackHandledInterface.setSelectedFragment(this)
    }
}
