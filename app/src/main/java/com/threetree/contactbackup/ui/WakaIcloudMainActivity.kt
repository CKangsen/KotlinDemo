package com.threetree.contactbackup.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.app.AlertDialog
import android.text.TextUtils
import android.view.Window

import com.afmobi.statInterface.statsdk.core.TcStatInterface
import com.afmobi.tudcsdk.Tudcsdk
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.R
import com.threetree.contactbackup.WakaIcloudApplication
import com.threetree.contactbackup.base.BackHandledInterface
import com.threetree.contactbackup.base.BaseFragment
import com.threetree.contactbackup.util.LogUtils
import com.threetree.contactbackup.util.PermissionUtils

import java.util.ArrayList
import java.util.Arrays

import butterknife.ButterKnife


class WakaIcloudMainActivity : FragmentActivity(), BackHandledInterface {

    /**TAG */
    private val TAG = WakaIcloudMainActivity::class.java.simpleName
    private val singleFragment: BaseFragment? = null
    private var mFragmentManager: FragmentManager? = null


    internal var defaultSmsApp: String? = null

    val visibleFragment: BaseFragment?
        get() {
            val fragmentManager = supportFragmentManager
            val fragments = fragmentManager.fragments
            if (fragments != null) {
                for (fragment in fragments) {
                    if (fragment != null && fragment.isVisible)
                        return fragment as BaseFragment
                }
            }

            return null
        }

    val noGrantedPermissions: ArrayList<String>?
        get() {
            val permissions = ArrayList<String>()

            for (i in 0 until PermissionUtils.getRequestPermissions().size) {
                val requestPermission = PermissionUtils.getRequestPermissions()[i]

                var checkSelfPermission = -1
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkSelfPermission = ContextCompat.checkSelfPermission(this, requestPermission)
                    } else {
                        checkSelfPermission = PermissionChecker.checkSelfPermission(activity, requestPermission)
                    }

                } catch (e: RuntimeException) {
                    LogUtils.e(TAG, "RuntimeException:" + e.message)
                    return null
                }

                if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                    LogUtils.i(TAG, "getNoGrantedPermission PermissionChecker.checkSelfPermission != PackageManager.PERMISSION_GRANTED:" + requestPermission)
                    permissions.add(requestPermission)
                }
            }
            return permissions
        }

    private val activity: Activity
        get() = this

    private val mPermissionGrant = object : PermissionUtils.PermissionGrant {
        override fun onPermissionGranted(requestCode: Int) {

            showPermissionMessage(activity, noGrantedPermissions)
        }

        override fun onTUDCPermissionGranted(permission: Array<String>) {
            val tudcPermissions = Factory.get().getCacheStatisticsManager().getTUDCRequestPermissions()
            if (tudcPermissions == null || tudcPermissions!!.size <= 0) {
                return
            }
            var TUDCGrantedCount = 0
            val tudcPL = Arrays.asList<String>(*tudcPermissions!!)
            val cloudPL = Arrays.asList(*permission)
            for (pTUDC in tudcPL) {
                if (cloudPL.contains(pTUDC)) {
                    TUDCGrantedCount++
                }
            }

            if (TUDCGrantedCount == tudcPermissions!!.size) {
                Tudcsdk.sdkInitialize(Factory.get().getApplicationContext())
                WakaIcloudApplication.initWKCInstance(Factory.get().getApplicationContext())
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
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
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this.activity)

        val manager = supportFragmentManager
        mFragmentManager = manager
        replaceFragment(WakaIcloudMainFragment.newInstance())

        checkPermissions()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    override fun onResume() {
        super.onResume()
        TcStatInterface.onResume(Factory.get().getApplicationContext())       //统计时长

        //        final String myPackageName = getPackageName();
        //        defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this.getBaseContext());
        //        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)&& ApplicationPrefsManager.getInstance(this.getBaseContext()).isSyncMessages()) {
        //            Intent intent =
        //                    new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        //            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
        //                    myPackageName);
        //            startActivity(intent);
        //        }
    }

    public override fun onPause() {
        super.onPause()
        TcStatInterface.onPause()
    }

    override fun onBackPressed() {
        //        BaseFragment fragment = getVisibleFragment();
        //        if(fragment == null || !fragment.onBackPressed()){
        //            if(getSupportFragmentManager().getBackStackEntryCount() == 0){
        //                super.onBackPressed();
        //            }else{
        ////                getSupportFragmentManager().popBackStack();
        ////                super.onBackPressed();
        //                finish();
        //            }
        //        }
        val wakaIcloudMainFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as WakaIcloudMainFragment
        if (wakaIcloudMainFragment != null && !wakaIcloudMainFragment!!.getIsSync()) {
            Factory.get().getCacheStatisticsManager().clearCompareStatistics()
        }
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        this.startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (mFragmentManager != null) {
            val fragments = mFragmentManager!!.fragments
            for (fragment in fragments) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    fun replaceFragment(fragment: BaseFragment) {
        if (mFragmentManager != null) {
            fragment.setRetainInstance(true)
            fragment.setTargetFragment(fragment, 0)
            mFragmentManager!!.beginTransaction().add(R.id.fragment_container, fragment, FRAGMENT_TAG)
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(FRAGMENT_TAG)
                    .commit()
        }
    }

    fun checkPermissionAllGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false
            }
        }
        return true
    }

    fun checkPermissions() {
        var targetSdkVersion = 0
        try {
            val info = this.packageManager.getPackageInfo(
                    this.packageName, 0)
            targetSdkVersion = info.applicationInfo.targetSdkVersion
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        //如果TUDC部分未授权，则设置新的权限数组
        PermissionUtils.setPermissionsArray(Factory.get().getCacheStatisticsManager().getTUDCRequestPermissions())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                PermissionUtils.requestMultiPermissions(this, mPermissionGrant)
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                showPermissionMessage(activity, noGrantedPermissions)

            }
        } else {
            // targetSdkVersion < Android M, we have to use PermissionChecker
            showPermissionMessage(activity, noGrantedPermissions)
        }
    }

    private fun showPermissionMessage(context: Activity, permissions: ArrayList<String>?) {
        if (permissions == null || permissions.size <= 0) {
            return
        }

        //拼构权限提示信息
        var isNeedPhonePermission = false
        var isNeedSMSPermission = false
        var isNeedContactPermission = false
        for (temp in permissions) {
            if (temp.contains("SMS")) {
                isNeedSMSPermission = true
            }
            if (temp.contains("CONTACTS")) {
                isNeedContactPermission = true
            }
            if (temp.contains("CALL_LOG")) {
                isNeedPhonePermission = true
            }
            if (temp.contains("PHONE_STATE")) {
                isNeedPhonePermission = true
            }
        }

        if (!(isNeedContactPermission || isNeedPhonePermission || isNeedSMSPermission)) {
            return
        }

        val tempString = StringBuffer()
        if (isNeedPhonePermission) {
            tempString.append(getString(R.string.phone) + "、")
        }
        if (isNeedSMSPermission) {
            tempString.append(getString(R.string.sms_permission) + "、")
        }
        if (isNeedContactPermission) {
            tempString.append(getString(R.string.contacts) + "")
        }
        var requestPermissions = tempString.toString()
        val a = "、"[0]
        if (a == requestPermissions[requestPermissions.length - 1]) {
            requestPermissions = requestPermissions.substring(0, requestPermissions.length - 1)
        }
        val message = String.format(getString(R.string.request_permission_message_before), getString(R.string.app_name), requestPermissions) + getString(R.string.request_permission_message_after)

        //弹窗
        AlertDialog.Builder(context)
                .setTitle(R.string.request_permission_title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.open_permission), DialogInterface.OnClickListener { dialog, which -> gotoPermissionActivity(context) })
                .create()
                .show()

    }

    private fun gotoPermissionActivity(activity: Activity) {
        val intent = Intent(activity, PermissionActivity::class.java)
        activity.startActivity(intent)
    }

    private fun gotoSettingActivity(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
        this.finish()
    }

    fun checkPermissionWithSyncSwitch(): Boolean {
        val is_sync_contact = Factory.get().getApplicationPrefsManager().isSyncContacts()
        val is_sync_sms = Factory.get().getApplicationPrefsManager().isSyncMessages()
        val is_sync_phone = Factory.get().getApplicationPrefsManager().isSyncPhone()

        var status = false

        out@ try {
            if (is_sync_contact) {
                status = PermissionChecker.checkSelfPermission(this, PermissionUtils.PERMISSION_READ_CONTACTS) == PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(this, PermissionUtils.PERMISSION_WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
                if (status == false) {
                    break@out
                }
            }
            if (is_sync_sms) {
                status = PermissionChecker.checkSelfPermission(this, PermissionUtils.PERMISSION_READ_SMS) == PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(this, PermissionUtils.PERMISSION_WRITE_SMS) == PackageManager.PERMISSION_GRANTED
                if (status == false) {
                    break@out
                }
            }
            if (is_sync_phone) {
                status = PermissionChecker.checkSelfPermission(this, PermissionUtils.PERMISSION_READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(this, PermissionUtils.PERMISSION_WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                if (status == false) {
                    break@out
                }
            }
        } catch (e: Exception) {
            LogUtils.d(TAG, " checkPermissionWithSyncSwitch : " + e.message.toString())
        }

        return status
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant)
    }

    fun setSelectedFragment(selectedFragment: BaseFragment) {
        mCurrentFragment = selectedFragment
    }

    override fun onDestroy() {
        mCurrentFragment = null
        super.onDestroy()
    }

    companion object {

        private val FRAGMENT_TAG = WakaIcloudMainActivity::class.java.simpleName
        private var mCurrentFragment: BaseFragment? = null
    }
}
