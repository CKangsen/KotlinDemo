package com.threetree.contactbackup.ui

import android.support.v4.app.Fragment

import com.threetree.contactbackup.base.BaseActivity

import java.util.Stack


class ActivityManager private constructor() {

    private val activityStack: Stack<BaseActivity>?
    private val fragmentStack: Stack<Fragment>? = null

    /**
     * 是否清空所有的导航，设置此变量主要是清空导航栈时，在baseActivity在从数据源删除数据，导航遍历关闭acitivity时出错
     */
    var isAllClear: Boolean = false
    val activityCount: Int
        get() = activityStack!!.size

    /**
     * get current activity
     *
     * @return
     */
    val currentActivity: BaseActivity?
        get() = if (activityStack != null && activityStack.size > 0) {
            activityStack[activityStack.size - 1]
        } else null

    init {
        activityStack = Stack<BaseActivity>()
    }

    fun popActivity(activity: BaseActivity?) {
        var activity = activity
        if (activity != null) {
            activity!!.finish()
            activityStack!!.remove(activity)
            activity = null
        }
    }

    fun popActivity(cls: Class<*>) {
        for (i in activityStack!!.indices.reversed()) {
            val activity = activityStack[i]
            if (activity != null) {
                if (activity!!.getClass().equals(cls)) {
                    popActivity(activity)
                    break
                }
            } else {
                break
            }
        }
    }

    fun popActivity() {
        if (activityStack!!.size > 0) {
            val activity = activityStack.pop()
            if (activity != null) {
                activity!!.finish()
            }
            println(activity!! + "")
        }
    }

    fun currentActivity(): BaseActivity? {
        var activity: BaseActivity? = null
        if (!activityStack!!.empty()) {
            activity = activityStack.lastElement()
        }
        return activity
    }

    fun currentFragment(): Fragment? {
        var fragment: Fragment? = null
        if (!fragmentStack!!.empty()) {
            fragment = fragmentStack.lastElement()
            println("MyActivityManager  fragment  " + fragment!!)
        }
        return fragment
    }

    fun pushActivity(activity: BaseActivity) {
        //        LogUtils.i(TAG, "pushActivity==" + activity);
        activityStack!!.add(activity)
    }

    fun isExistsActivity(cls: Class<*>): Boolean {
        var isExists = false
        for (i in activityStack!!.indices) {
            val activity = activityStack[i]
            if (cls == activity.getClass()) {
                isExists = true
                break
            }
        }
        return isExists
    }

    /**
     * 获取指定的activity
     *
     * @param cls
     * @return
     */
    fun getActivity(cls: Class<*>): BaseActivity? {
        for (i in activityStack!!.indices) {
            val activity = activityStack[i]
            if (cls == activity.getClass()) {
                return activity
            }
        }
        return null
    }

    fun IsEmpty(): Boolean {
        return null != activityStack && 0 == activityStack.size
    }

    fun popAllActivityExceptOne(cls: Class<*>) {
        var topActivity: BaseActivity? = null
        while (true) {
            val activity = currentActivity() ?: break
            if (activity.getClass().equals(cls)) {
                activityStack!!.remove(activity)
                topActivity = activity
                continue
            }
            popActivity(activity)
        }
        if (topActivity != null) {
            activityStack!!.add(0, topActivity)
        }
    }

    private fun popAllActivity() {
        if (null != activityStack) {
            for (i in activityStack.indices) {
                activityStack[i].finish()
            }
        }
    }

    companion object {
        private val TAG = ActivityManager::class.java.simpleName

        private var instance: ActivityManager? = null

        val activityManager: ActivityManager
            get() {
                if (instance == null) {
                    instance = ActivityManager()
                }
                return instance
            }

        fun getActivityStack(): Stack<BaseActivity>? {
            return activityManager.activityStack
        }
    }
}
