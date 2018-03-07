package com.threetree.contactbackup.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView

import com.threetree.contactbackup.R
import com.threetree.contactbackup.base.BaseActivity


class PermissionActivity : BaseActivity() {


    private var back_btn: ImageView? = null
    private var goto_setting_btn: Button? = null

    protected fun onBaseCreate(savedInstanceState: Bundle) {
        setContentView(R.layout.activity_permission)
    }

    protected fun initView(savedInstanceState: Bundle) {
        back_btn = findViewById(R.id.permission_back) as ImageView
        goto_setting_btn = findViewById(R.id.goto_setting_btn) as Button

        back_btn!!.setOnClickListener { finish() }

        goto_setting_btn!!.setOnClickListener { gotoSettingActivity() }
    }

    private fun gotoSettingActivity() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", this.getPackageName(), null)
        intent.data = uri
        this.startActivity(intent)
        this.finish()
    }
}
