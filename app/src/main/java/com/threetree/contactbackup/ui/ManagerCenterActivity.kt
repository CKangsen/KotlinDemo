package com.threetree.contactbackup.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.support.annotation.RequiresApi
import android.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton

import com.afmobi.tudcsdk.utils.ToastUtil
import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.R
import com.threetree.contactbackup.base.BasePresenterActivity
import com.threetree.contactbackup.constant.IntentKey
import com.threetree.contactbackup.constant.WakaIcloudConstant
import com.threetree.contactbackup.presenter.ManagerCenterPresenter
import com.threetree.contactbackup.presenter.view.IManagerCenterView
import com.threetree.contactbackup.ui.resetpassword.ResetPasswordActivity

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnCheckedChanged
import butterknife.OnClick

import com.threetree.contactbackup.constant.WakaIcloudConstant.REQ_SETDEFAULT_SMS_RESULT_CODE

class ManagerCenterActivity : BasePresenterActivity<IManagerCenterView, ManagerCenterPresenter>(), IManagerCenterView {
    @BindView(R.id.tv_title) internal var tv_title: TextView? = null
    @BindView(R.id.tv_version) internal var tv_version: TextView? = null
    @BindView(R.id.btn_logout) internal var btn_logout: Button? = null
    @BindView(R.id.iv_newversion) internal var iv_newversion: ImageView? = null
    @BindView(R.id.iv_back) internal var iv_back: ImageView? = null
    @BindView(R.id.ll_password) internal var ll_password: LinearLayout? = null
    @BindView(R.id.ll_checkupdate) internal var ll_checkupdate: RelativeLayout? = null
    @BindView(R.id.tb_contacts) internal var tb_contacts: ToggleButton? = null
    @BindView(R.id.tb_messages) internal var tb_messages: ToggleButton? = null
    @BindView(R.id.tb_phone) internal var tb_phone: ToggleButton? = null
    private var mCacheStatisticsManager: CacheStatisticsManager? = null

    private val version: String?
        get() {
            var strVersion: String? = null
            val manager = this.getPackageManager()
            var info: PackageInfo? = null
            try {
                info = manager.getPackageInfo(this.getPackageName(), 0)
                strVersion = info!!.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return strVersion
        }

    internal var defaultSmsApp: String


    protected fun onBaseCreate(savedInstanceState: Bundle) {
        super.onBaseCreate(savedInstanceState)

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )

        setContentView(R.layout.activity_manager_center)
        ButterKnife.bind(this)
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()


        val resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = getResources().getDimensionPixelSize(resourceId)
        val title_layout = findViewById(R.id.title_layout) as RelativeLayout
        val layoutParams = title_layout.layoutParams
        layoutParams.height = layoutParams.height + statusBarHeight
        title_layout.layoutParams = layoutParams
        title_layout.setPadding(0, statusBarHeight, 0, 0)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected fun initView(savedInstanceState: Bundle) {
        tv_version!!.text = version
        setDefaultSmsApp()
        tb_contacts!!.isChecked = ApplicationPrefsManager.getInstance(this).isSyncContacts()
        tb_phone!!.isChecked = ApplicationPrefsManager.getInstance(this).isSyncPhone()

        getPresenter().getLoginProfile()
        getPresenter().getUpgradeInfo()

    }

    protected fun createPresenter(): ManagerCenterPresenter {
        return ManagerCenterPresenter()
    }

    @OnClick(R.id.iv_back)
    internal fun click_iv_back() {
        finish()
    }

    @OnClick(R.id.ll_password)
    internal fun click_ll_password() {
        if (btn_logout!!.isEnabled) {
            getPresenter().gotoResetPassword()
        }
    }

    @OnClick(R.id.tv_title)
    internal fun click_tv_title() {
        getPresenter().gotoProfile(this)
    }

    @OnClick(R.id.ll_checkupdate)
    internal fun click_ll_checkupdate() {
        getPresenter().checkUpdate()
    }

    @OnClick(R.id.btn_logout)
    internal fun click_btn_logout() {
        showLogOutDialog()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnCheckedChanged(R.id.tb_contacts)
    internal fun checkedChanged_tb_contacts(buttonView: CompoundButton, isChecked: Boolean) {
        var titleResId = 0
        val isAskToClose = !isChecked
        titleResId = R.string.sync_contacts_ask
        ApplicationPrefsManager.getInstance(this).setSyncContacts(isChecked)
        showDialog(isAskToClose, titleResId, tb_contacts)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnCheckedChanged(R.id.tb_messages)
    internal fun checkedChanged_tb_messages(buttonView: CompoundButton, isChecked: Boolean) {
        var titleResId = 0
        val isAskToClose = !isChecked
        titleResId = R.string.sync_message_ask
        ApplicationPrefsManager.getInstance(this).setSyncMessages(isChecked)
        checkdefaultSMS()
        showDialog(isAskToClose, titleResId, tb_messages)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnCheckedChanged(R.id.tb_phone)
    internal fun checkedChanged_tb_phone(buttonView: CompoundButton, isChecked: Boolean) {
        var titleResId = 0
        val isAskToClose = !isChecked
        titleResId = R.string.sync_phone_ask
        ApplicationPrefsManager.getInstance(this).setSyncPhone(isChecked)
        showDialog(isAskToClose, titleResId, tb_phone)
    }

    internal fun showDialog(isAskToClose: Boolean, titleResId: Int, btn: ToggleButton) {
        if (isAskToClose) {
            val dialog = AlertDialog.Builder(this).create()
            dialog.show()
            dialog.window!!.setContentView(R.layout.dialog_alert)
            dialog.setOnCancelListener { btn.isChecked = true }
            val tv_title = dialog.window!!.findViewById(R.id.tv_title) as TextView
            tv_title.setText(titleResId)
            val tv_dismiss = dialog.window!!.findViewById(R.id.tv_dismiss) as TextView
            tv_dismiss.setText(R.string.cancel)
            tv_dismiss.setOnClickListener {
                btn.isChecked = true
                dialog.dismiss()
            }
            val tv_ok = dialog.window!!.findViewById(R.id.tv_ok) as TextView
            tv_ok.setText(R.string.confirm)
            tv_ok.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }


    private fun showLogOutDialog() {
        val dialog = AlertDialog.Builder(this).create()
        dialog.show()
        dialog.window!!.setContentView(R.layout.dialog_alert)
        val tv_title = dialog.window!!.findViewById(R.id.tv_title) as TextView
        tv_title.setText(R.string.logout_ask)
        val tv_dismiss = dialog.window!!.findViewById(R.id.tv_dismiss) as TextView
        tv_dismiss.setText(R.string.cancel)
        tv_dismiss.setOnClickListener { dialog.dismiss() }
        val tv_ok = dialog.window!!.findViewById(R.id.tv_ok) as TextView
        tv_ok.setText(R.string.logout)
        tv_ok.setOnClickListener {
            getPresenter().logOut()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun updateName(name: String) {
        tv_title!!.text = name
        btn_logout!!.isEnabled = true
    }

    fun updateNeedLogin(resId: Int) {
        mCacheStatisticsManager!!.clearCompareStatistics()
        Factory.get().getApplicationPrefsManager().setLastSyncSuccessTimestamp(0)
        Factory.get().getApplicationPrefsManager().setCloudCurrentContactCount(0)
        Factory.get().getApplicationPrefsManager().setServer_big_version(0)
        tv_title!!.setText(resId)
        btn_logout!!.isEnabled = false
    }

    fun gotoResetPassword(phone: String, email: String) {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        if (TextUtils.isEmpty(phone) && TextUtils.isEmpty(email)) {
            intent.putExtra(IntentKey.INSTANCE.getPASSWORD_TYPE(), ResetPasswordActivity.Companion.getTYPE_CHANGER_PASSWORD_BY_PASSWORD())
        } else {
            intent.putExtra(IntentKey.INSTANCE.getPASSWORD_TYPE(), ResetPasswordActivity.Companion.getTYPE_CHANGER_PASSWORD())
        }

        startActivityForResult(intent, WakaIcloudConstant.INSTANCE.getREQUEST_CODE_CHANGEPASSWORD())
    }

    fun showToast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
    }

    fun updateLogOutView() {
        updateNeedLogin(R.string.click_to_login)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun setDefaultSmsApp() {
        val myPackageName = this.getPackageName()
        defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this.getBaseContext())
        if (Telephony.Sms.getDefaultSmsPackage(this) != myPackageName && ApplicationPrefsManager.getInstance(this.getBaseContext()).isSyncMessages()) {
            tb_messages!!.isChecked = false
            ApplicationPrefsManager.getInstance(this).setSyncMessages(false)
        } else {
            tb_messages!!.isChecked = ApplicationPrefsManager.getInstance(this).isSyncMessages()
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun onResume() {
        super.onResume()
        setDefaultSmsApp()
    }

    fun setUpgradeInfo(version: String) {
        tv_version!!.text = version
        iv_newversion!!.visibility = View.VISIBLE
    }

    fun loginSuccess() {
        getPresenter().getLoginProfile()
    }

    fun loginFailed(msg: String) {

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun checkdefaultSMS() {
        val myPackageName = this.getPackageName()
        defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this.getBaseContext())
        if (Telephony.Sms.getDefaultSmsPackage(this) != myPackageName && ApplicationPrefsManager.getInstance(this.getBaseContext()).isSyncMessages()) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                    myPackageName)
            //            startActivity(intent);
            startActivityForResult(intent, INSTANCE.getREQ_SETDEFAULT_SMS_RESULT_CODE())
        }
    }


    fun logOutFailed(Code: Int, msg: String) {
        ToastUtil.showToast(this, msg)
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        getPresenter().onActivityResult(requestCode, resultCode, data)

        if (requestCode == WakaIcloudConstant.INSTANCE.getREQUEST_CODE_CHANGEPASSWORD()) {
            if (resultCode == RESULT_OK) {
                getPresenter().logOut()
            }
        } else if (requestCode == WakaIcloudConstant.INSTANCE.getREQ_SETDEFAULT_SMS_RESULT_CODE()) {
            if (resultCode == RESULT_OK) {
                tb_messages!!.isChecked = ApplicationPrefsManager.getInstance(this).isSyncMessages()
            } else {
                tb_messages!!.isChecked = false
            }
        } else {
            if (requestCode == WakaIcloudConstant.INSTANCE.getICLOUD_LOGIN_RESULT_CODE()) {
                getPresenter().getLoginProfile()
            }
        }
    }
}
