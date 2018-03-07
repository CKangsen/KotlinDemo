package com.threetree.contactbackup.base

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.text.TextUtils
import android.view.Window

import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.ui.ActivityManager

import butterknife.ButterKnife


abstract class BaseActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (!this.isTaskRoot) { //判断该Activity是不是任务空间的源Activity，“非”也就是说是被系统重新实例化出来
            //如果你就放在launcher Activity中话，这里可以直接return了
            val mainIntent = intent
            if (mainIntent != null) {
                val action = mainIntent.action
                if (!TextUtils.isEmpty(action) && mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action == Intent.ACTION_MAIN) {
                    finish()
                    return //finish()之后该活动会继续执行后面的代码，你可以logCat验证，加return避免可能的exception
                }
            }
        }

        onBaseCreate(savedInstanceState)
        ActivityManager.getActivityManager().pushActivity(this)
        initView(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityManager.getActivityStack().remove(this)
    }

    /**
     * 设置一个ContentView及其他的一些初始化
     *
     * @param savedInstanceState
     */
    protected abstract fun onBaseCreate(savedInstanceState: Bundle?)

    /**
     * findviewbyid
     *
     * @param savedInstanceState
     */
    protected abstract fun initView(savedInstanceState: Bundle?)

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }
}
