package com.threetree.contactbackup.ui

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Telephony
import android.support.annotation.RequiresApi
import android.app.AlertDialog
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView

import com.afmobi.statInterface.statsdk.core.TcStatInterface
import com.afmobi.tudcsdk.utils.ToastUtil
import com.afmobi.tudcsdk.utils.ValidateUtil
import com.threetree.contactbackup.CacheStatisticsManager
import com.threetree.contactbackup.Factory
import com.threetree.contactbackup.R
import com.threetree.contactbackup.base.BasePrsenterFragment
import com.threetree.contactbackup.constant.WakaIcloudConstant
import com.threetree.contactbackup.midcore.WKCInstance
import com.threetree.contactbackup.model.ReadPhoneContactModel
import com.threetree.contactbackup.presenter.MainPresenter
import com.threetree.contactbackup.presenter.view.IMainView
import com.threetree.contactbackup.util.DateUtils
import com.threetree.contactbackup.util.LogUtils
import com.threetree.contactbackup.util.NetWorkUtil
import com.threetree.contactbackup.util.PermissionUtils

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder

import com.threetree.contactbackup.constant.WakaIcloudConstant.REQ_CODE_SYNC_ERROR
import com.threetree.contactbackup.constant.WakaIcloudConstant.REQ_CODE_TOKEN_ERROR
import com.threetree.contactbackup.constant.WakaIcloudConstant.REQ_CODE_UNNETWORK
import com.threetree.contactbackup.constant.WakaIcloudConstant.REQ_SETDEFAULT_SMS_RESULT_CODE



class WakaIcloudMainFragment : BasePrsenterFragment<IMainView, MainPresenter>(), IMainView {

    @BindView(R.id.bg_layout) internal var mBgLayout: RelativeLayout? = null
    @BindView(R.id.title_layout) internal var mTitleLayout: RelativeLayout? = null
    @BindView(R.id.process_layout) internal var mProcessLayout: LinearLayout? = null
    @BindView(R.id.process) internal var mProcess: LinearLayout? = null

    @BindView(R.id.sync_success_layout) internal var mSyncSuccessLayout: LinearLayout? = null
    @BindView(R.id.final_sync_layout) internal var mFinalLastSyncLayout: LinearLayout? = null

    @BindView(R.id.info_load_error_layout) internal var mInfoLoadErrorLayout: LinearLayout? = null
    @BindView(R.id.refresh) internal var mRefreshBtn: TextView? = null


    @BindView(R.id.app_title) internal var mAppTitle: TextView? = null
    @BindView(R.id.last_sync) internal var mLastSyncTitle: TextView? = null
    @BindView(R.id.sync_time) internal var mLastSyncTime: TextView? = null

    @BindView(R.id.info_layout) internal var mInfoTextLayout: RelativeLayout? = null
    @BindView(R.id.info_text) internal var mInfoText: TextView? = null
    @BindView(R.id.sysc_info_text) internal var mSyscInfoText: TextView? = null

    @BindView(R.id.info_text_local) internal var mInfoText_local: TextView? = null
    @BindView(R.id.info_text_server) internal var mInfoText_server: TextView? = null

    @BindView(R.id.sync_status_text) internal var mSyscStatusText: TextView? = null
    @BindView(R.id.sync_success_time) internal var mSyscStatusTime: TextView? = null
    @BindView(R.id.sync_status_icon) internal var mSyscStatusIcon: ImageView? = null

    @BindView(R.id.menu) internal var mMenuBtn: ImageView? = null
    @BindView(R.id.sync_btn) internal var mSyncBtn: ImageView? = null

    @BindView(R.id.local_current_count) internal var mLocalCurrentCount: TextView? = null
    @BindView(R.id.icloud_current_count) internal var mIcloudCurrentCount: TextView? = null
    @BindView(R.id.local_change_count) internal var mLocalChangeCount: TextView? = null
    @BindView(R.id.icloud_change_count) internal var mIcloudChangeCount: TextView? = null
    @BindView(R.id.iv_newversion) internal var mIv_newVersion: ImageView? = null

    internal var initTime: Long = 0
    internal var doneTime: Long = 0

    private var mappingcount: Int = 0

    private var device_width: Int = 0
    /*是否已加载服务器数据完毕*/
    private var is_load_done = false
    private var is_has_sync_success_done = false
    private var is_has_sync_fail_done = false

    /*开关&&权限 状态*/
    private var is_sync_contact = false
    private var is_sync_sms = false
    private var is_sync_phone = false

    /*权限状态*/
    private var is_auth_contact = false
    private var is_auth_sms = false
    private var is_auth_phone = false

    /*开关状态*/
    private var is_sync_contact_switch = false
    private var is_sync_sms_switch = false
    private var is_sync_phone_switch = false

    internal var mCacheStatisticsManager: CacheStatisticsManager

    private var unbinder: Unbinder? = null

    val isSync: Boolean
        get() = mProcessLayout!!.visibility == View.VISIBLE

    internal var defaultSmsApp: String

    internal val LoginUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                UPDATE_STATISTICS -> updateInfo()
                SYNC_DATA -> getPresenter().synncdata()
                UPDATE_PROCESS -> sendMessageToUpdateProcess(msg.arg2, UPDATE_PROCESS)
                else -> {
                }
            }
            super.handleMessage(msg)
        }
    }

    @Volatile
    var target_process_percentage = 0

    private var mAlertDialog: AlertDialog? = null
    private var countLocal: Int = 0
    private var countAddLocal: Int = 0
    private var countDeleteLocal: Int = 0
    private var countUpdateLocal: Int = 0
    private var countAddServer: Int = 0
    private var countDeleteSever: Int = 0
    private var countUpdateServer: Int = 0

    // 枚举进程
    val isRunningForeground: Boolean
        get() {
            val activityManager = Factory.get().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val appProcessInfos = activityManager.runningAppProcesses
            if (appProcessInfos != null && appProcessInfos.size > 0) {
                for (appProcessInfo in appProcessInfos) {
                    if (appProcessInfo.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        if (appProcessInfo.processName == Factory.get().getApplicationContext().getApplicationInfo().processName) {
                            LogUtils.d(TAG, "EntryActivity isRunningForeGround")
                            return true
                        }
                    }
                }
            }
            LogUtils.d(TAG, "EntryActivity isRunningBackGround")
            return false
        }

    fun initSyncSwitch() {
        is_sync_contact_switch = Factory.get().getApplicationPrefsManager().isSyncContacts()
        is_sync_sms_switch = Factory.get().getApplicationPrefsManager().isSyncMessages()
        is_sync_phone_switch = Factory.get().getApplicationPrefsManager().isSyncPhone()

        is_auth_contact = PermissionUtils.isAuthContactPermission()
        is_auth_sms = PermissionUtils.isAuthSMSPermission()
        is_auth_phone = PermissionUtils.isAuthPhonePermission()

        is_sync_contact = is_sync_contact_switch && is_auth_contact
        is_sync_sms = is_sync_sms_switch && is_auth_sms
        is_sync_phone = is_sync_phone_switch && is_auth_phone
    }

    fun onBackPressed(): Boolean {
        //        getFragmentManager().popBackStack();
        return false
    }

    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dm = getResources().getDisplayMetrics()
        device_width = dm.widthPixels
        mCacheStatisticsManager = Factory.get().getCacheStatisticsManager()
        initSyncSwitch()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View {

        this.getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )

        val resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = getResources().getDimensionPixelSize(resourceId)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val title_layout = view.findViewById(R.id.title_layout) as RelativeLayout
        val layoutParams = title_layout.layoutParams
        layoutParams.height = layoutParams.height + statusBarHeight
        title_layout.layoutParams = layoutParams
        title_layout.setPadding(0, statusBarHeight, 0, 0)

        checkdefaultSMS()

        unbinder = ButterKnife.bind(this, view)
        return view
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnClick(R.id.sync_btn)
    internal fun click_sync_btn() {
        initTime = System.currentTimeMillis()
        sync()
    }

    @OnClick(R.id.menu)
    internal fun click_menu() {
        mCacheStatisticsManager.clearCompareStatistics()
        val intent = Intent(getContext(), ManagerCenterActivity::class.java)
        startActivity(intent)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @OnClick(R.id.refresh)
    internal fun click_refresh() {
        if (!NetWorkUtil.isAvailable(Factory.get().getApplicationContext())) {
            ToastUtil.showToast(Factory.get().getApplicationContext(), getString(R.string.network_error))
            return
        }
        if (!WKCInstance.getDefaultInstance().Checklogin()) {
            is_has_sync_success_done = false
            is_has_sync_fail_done = false
            getPresenter().tudcLogin(getActivity())
        } else {
            doLoading()
        }
    }


    private fun checkCurrentContactCount() {
        val countContact = ReadPhoneContactModel.Companion.getPhoneContactCounts()
        Factory.get().getApplicationPrefsManager().setLocalCurrentContactCount(countContact)

        mLocalCurrentCount!!.setText(countContact + "")
        if (WKCInstance.getDefaultInstance().Checklogin() && ValidateUtil.isExistTgt(false)) {
            mIcloudCurrentCount!!.setText(Factory.get().getApplicationPrefsManager().getCloudCurrentContactCount() + "")
        } else {
            mIcloudCurrentCount!!.text = "0"
        }
    }

    private fun checkLastSyncStatus() {
        if (Factory.get().getApplicationPrefsManager().getLastSyncSuccessTimestamp() !== 0) {
            mFinalLastSyncLayout!!.visibility = View.VISIBLE
            mLastSyncTitle!!.visibility = View.VISIBLE
            mLastSyncTime!!.visibility = View.VISIBLE
            mLastSyncTitle!!.setText(R.string.last_sync)
            mLastSyncTime!!.setText(DateUtils.getDateTime(Factory.get().getApplicationPrefsManager().getLastSyncSuccessTimestamp()))

            mSyncSuccessLayout!!.visibility = View.GONE
        } else {
            mFinalLastSyncLayout!!.visibility = View.VISIBLE
            mLastSyncTitle!!.visibility = View.VISIBLE
            mLastSyncTime!!.visibility = View.GONE
            mLastSyncTitle!!.setText(getString(R.string.not_sync))

            mSyncSuccessLayout!!.visibility = View.GONE
        }
    }

    private fun checkSyncBtnStatus(isCallFromOnResume: Boolean) {

        //        boolean isOpenPhone = Factory.get().getApplicationPrefsManager().isSyncPhone() ;
        //        boolean isOpenSMS = Factory.get().getApplicationPrefsManager().isSyncMessages() ;
        //        boolean isOpenContact = Factory.get().getApplicationPrefsManager().isSyncContacts() ;
        val isOpenPhone = is_sync_phone
        val isOpenSMS = is_sync_sms
        val isOpenContact = is_sync_contact

        if (!WKCInstance.getDefaultInstance().Checklogin() || !ValidateUtil.isExistTgt(false)) {
            mSyncBtn!!.setImageResource(R.drawable.sync_btn_selector)
            mSyncBtn!!.isClickable = true
            mInfoText!!.setText(getString(R.string.click_sync_to_login))
            if (mSyscInfoText!!.visibility == View.VISIBLE) {
                mSyscInfoText!!.visibility = View.GONE
            }
            showInfoLayout()
            return
        }
        if (!isOpenPhone && !isOpenSMS && !isOpenContact) {
            mSyncBtn!!.setImageResource(R.drawable.cloud_btn_backup_disable)
            mSyncBtn!!.isClickable = false
            mInfoText!!.setText(getString(R.string.please_open_all_sync_switch))
            if (mSyscInfoText!!.visibility == View.VISIBLE) {
                mSyscInfoText!!.visibility = View.GONE
            }
            if (!is_auth_phone && !is_auth_sms && !is_auth_contact) {
                mInfoText!!.text = ""
                mSyncBtn!!.setImageResource(R.drawable.sync_btn_selector)
                mSyncBtn!!.isClickable = true
            }
        } else {
            if (getString(R.string.please_open_all_sync_switch).equals(mInfoText!!.text.toString())) {
                showLoadFailLayout()
            }
            mSyncBtn!!.setImageResource(R.drawable.sync_btn_selector)
            mSyncBtn!!.isClickable = true
        }


        //拼构同步选项提示信息

        val tempString = StringBuffer()
        if (isOpenContact) {
            tempString.append(getString(R.string.contacts) + "、")
        }
        if (isOpenSMS) {
            tempString.append(getString(R.string.messages) + "、")
        }
        if (isOpenPhone) {
            tempString.append(getString(R.string.phone) + "")
        }

        if (isOpenContact || isOpenSMS || isOpenPhone) {
            var syncInfo = tempString.toString()
            val a = "、"[0]
            if (a == syncInfo[syncInfo.length - 1]) {
                syncInfo = syncInfo.substring(0, syncInfo.length - 1)
            }

            if (!isCallFromOnResume) {
                val finalSyncInfo = String.format(getString(R.string.sync), syncInfo)
                mInfoText!!.setText(finalSyncInfo)
                if (mSyscInfoText!!.visibility == View.VISIBLE) {
                    mSyscInfoText!!.visibility = View.GONE
                }
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun setDefaultSmsApp() {
        val myPackageName = getActivity().getPackageName()
        defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getActivity().getBaseContext())
        if (Telephony.Sms.getDefaultSmsPackage(getActivity()) != myPackageName && ApplicationPrefsManager.getInstance(getActivity()).isSyncMessages()) {
            ApplicationPrefsManager.getInstance(getActivity()).setSyncMessages(false)
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun onResume() {
        super.onResume()
        setDefaultSmsApp()
        TcStatInterface.recordPageStart(this)  //统计页面
        initSyncSwitch()

        /*获取升级信息*/
        getPresenter().getUpgradeInfo()

        if (mProcessLayout!!.visibility == View.VISIBLE) {

        } else {
            mCacheStatisticsManager.clearCompareStatistics()
            mLocalChangeCount!!.visibility = View.GONE
            mIcloudChangeCount!!.visibility = View.GONE
            mInfoText_local!!.visibility = View.GONE
            mInfoText_server!!.visibility = View.GONE
            if (WKCInstance.getDefaultInstance().Checklogin() && ValidateUtil.isExistTgt(false)) {
                checkSyncBtnStatus(true)
                doLoading()
            } else {
                checkSyncBtnStatus(false)
            }
            checkCurrentContactCount()
            checkLastSyncStatus()
        }
    }

    fun onPause() {
        super.onPause()
        TcStatInterface.recordPageEnd()
        dismissDeleteAlert()
    }

    fun onStop() {
        super.onStop()
        shutdownDeleteAlert()
    }

    fun onDestroyView() {
        super.onDestroyView()
        shutdownDeleteAlert()
        unbinder!!.unbind()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun checkdefaultSMS() {
        val myPackageName = getActivity().getPackageName()
        defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getActivity().getBaseContext())
        if (Telephony.Sms.getDefaultSmsPackage(getActivity()) != myPackageName && ApplicationPrefsManager.getInstance(getActivity().getBaseContext()).isSyncMessages()) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                    myPackageName)
            //            startActivity(intent);
            startActivityForResult(intent, INSTANCE.getREQ_SETDEFAULT_SMS_RESULT_CODE())
        }
    }


    protected fun createPresenter(): MainPresenter {
        return MainPresenter()
    }

    fun sendMessageToUpdateProcess(process_length: Int, messageType: Int) {

        if (mProcessLayout!!.visibility != View.VISIBLE) {
            mSyncBtn!!.isClickable = true
            return
        } else {
            mSyncBtn!!.isClickable = false
        }

        var length = process_length
        val percentage = (length.toFloat() / device_width.toFloat() * 100).toInt()

        if (percentage < this.target_process_percentage) {
            length = length + INCREMENTAL_PROCESS
            setProcessValue(length)
            val msg = Message()
            msg.what = messageType
            msg.arg2 = length
            LoginUiHandler.sendMessageDelayed(msg, 5)

        }

        if (percentage >= this.target_process_percentage) {
            length = length + INCREMENTAL_PROCESS
            setProcessValue(length)
            val next_msg = Message()
            next_msg.what = messageType
            next_msg.arg2 = length
            if (percentage > 95 && this.target_process_percentage < 90) {
                next_msg.arg2 = process_length
                LoginUiHandler.sendMessageDelayed(next_msg, 2000)
                LogUtils.d(TAG, "sendMessageToUpdateProcess run here")
            } else {
                LoginUiHandler.sendMessageDelayed(next_msg, 2000)
            }

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun doLoading() {
        if (!is_sync_contact && !is_sync_phone && !is_sync_sms) {
            is_load_done = true
            return
        }
        clearCacheCount()
        LogUtils.d(TAG, "doLoading")
        checkLastSyncStatus()
        is_load_done = false
        is_has_sync_success_done = false
        is_has_sync_fail_done = false
        mInfoText!!.setText(getString(R.string.loaing_data))
        mBgLayout!!.setBackgroundResource(R.drawable.cloud_bg_backup_green)
        mBgLayout!!.invalidate()
        showInfoLayout()
        getPresenter().checkSyncWithCloud()
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun sync() {
        LogUtils.d(TAG, "call sync()")
        if (!NetWorkUtil.isAvailable(Factory.get().getApplicationContext())) {
            ToastUtil.showToast(Factory.get().getApplicationContext(), getString(R.string.network_error))
            return
        }
        if (!WKCInstance.getDefaultInstance().Checklogin() || !ValidateUtil.isExistTgt(false)) {
            is_has_sync_success_done = false
            is_has_sync_fail_done = false
            getPresenter().tudcLogin(getActivity())
        } else {
            //if (((WakaIcloudMainActivity) this.getActivity()).checkPermissionWithSyncSwitch()) {
            if (is_load_done) {
                if (countDeleteLocal >= WakaIcloudConstant.INSTANCE.getDELETE_COUNT_LIMIT()) {
                    showDeleteAlert()
                } else {
                    //                        if (is_has_sync_success_done){
                    //                            ToastUtil.showToast(Factory.get().getApplicationContext(),getString(R.string.synced));
                    //                            return;
                    //                        }
                    if (is_has_sync_fail_done) {
                        doLoading()
                        is_has_sync_fail_done = false
                        return
                    }
                    //三项均未授权,弹窗检测权限
                    if (!is_auth_contact && !is_auth_phone && !is_auth_sms) {
                        (this.getActivity() as WakaIcloudMainActivity).checkPermissions()
                        return
                    }
                    //                        syncData();
                    getPresenter().CheckCanSync()

                }

            } else {
                ToastUtil.showToast(Factory.get().getApplicationContext(), getString(R.string.loading_data))
            }
            //} else {
            //    ((WakaIcloudMainActivity) this.getActivity()).checkPermissions();
            //}
        }

    }

    fun updateStatistics(countAddLocal: Int, countDeleteLocal: Int, countUpdateLocal: Int, countAddServer: Int, countDeleteSever: Int, countUpdateServer: Int, countLocal: Int) {
        this.countLocal = countLocal
        this.countAddLocal = countAddLocal
        this.countDeleteLocal = countDeleteLocal
        this.countUpdateLocal = countUpdateLocal
        this.countAddServer = countAddServer
        this.countDeleteSever = countDeleteSever
        this.countUpdateServer = countUpdateServer
        LoginUiHandler.sendEmptyMessage(UPDATE_STATISTICS)
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun logninSuccess(isExistTgt: Boolean) {
        if (isExistTgt) {
            //存在tgt，无需调用TUDC登录界面
            ToastUtil.showToast(Factory.get().getApplicationContext(), R.string.login_success)
            doLoading()
        } else {
            //需调用TUDC登录界面,从TUDC返回
        }

    }

    fun getSMSVersionSuccess(latestbackupts: String, latestcallts: String) {

    }

    fun uploadContactsSuccess(progress: Int) {
        LogUtils.d(TAG, "uploadContactsSuccess")
        this.target_process_percentage = progress
    }

    fun uploadSMSSuccess(progress: Int) {
        LogUtils.d(TAG, "uploadSMSSuccess")
        this.target_process_percentage = progress
    }

    fun uploadCallSuccess(progress: Int) {
        LogUtils.d(TAG, "uploadCallSuccess")
        this.target_process_percentage = progress
    }


    fun oprateFaild(code: Int, errMsg: String) {
        if (code == INSTANCE.getREQ_CODE_UNNETWORK()) {
            ToastUtil.showToast(Factory.get().getApplicationContext(), getString(R.string.network_error))
        } else if (code == INSTANCE.getREQ_CODE_TOKEN_ERROR()) {
            if (mProcessLayout!!.visibility == View.VISIBLE) {
                showSyncFailedLayout()
            }
            getPresenter().logOut()
            getPresenter().tudcLogin(getActivity())
        } else {
            ToastUtil.showToast(Factory.get().getApplicationContext(), errMsg)
        }

    }

    fun loadDataFailed(code: Int, errMsg: String) {
        LogUtils.d(TAG, "loadDataFailed code:$code errMsg:$errMsg")
        if (code == INSTANCE.getREQ_CODE_UNNETWORK()) {
            ToastUtil.showToast(Factory.get().getApplicationContext(), getString(R.string.network_error))
            showLoadFailLayout()
        } else if (code == INSTANCE.getREQ_CODE_TOKEN_ERROR()) {
            getPresenter().logOut()
            getPresenter().tudcLogin(getActivity())
        } else {
            ToastUtil.showToast(Factory.get().getApplicationContext(), errMsg)
            showLoadFailLayout()
        }

    }

    fun uploadSyncFailed(code: Int, errMsg: String) {
        LogUtils.d(TAG, "uploadSyncFailed code:$code errMsg:$errMsg")
        mInfoText_server!!.visibility = View.GONE
        mInfoText_local!!.visibility = View.GONE
        if (code == INSTANCE.getREQ_CODE_UNNETWORK()) {
            ToastUtil.showToast(Factory.get().getApplicationContext(), getString(R.string.network_error))
            showSyncFailedLayout()
        } else if (code == INSTANCE.getREQ_CODE_TOKEN_ERROR()) {
            getPresenter().logOut()
            getPresenter().tudcLogin(getActivity())
        } else if (code == INSTANCE.getREQ_CODE_SYNC_ERROR()) {
            ToastUtil.showToast(Factory.get().getApplicationContext(), getString(R.string.sync_error))
            showSyncFailedLayout()
        } else {
            ToastUtil.showToast(Factory.get().getApplicationContext(), errMsg)
            showSyncFailedLayout()
        }
    }

    fun oprateFaild(errMsg: String) {
        ToastUtil.showToast(getActivity(), errMsg)
    }

    fun checkSyncWithCloudFinish() {
        LogUtils.d(TAG, "checkSyncWithCloudFinish")
        if (!NetWorkUtil.isAvailable(Factory.get().getApplicationContext())) {
            ToastUtil.showToast(Factory.get().getApplicationContext(), getString(R.string.network_error))
            showLoadFailLayout()
            return
        } else {
            is_load_done = true
            if (mappingcount == 0) {
                mappingcount = Factory.get().getDBManager().getMappingcount()
            }
            getPresenter().updateUI()
            showInfoLayout()
        }
    }

    fun syncdataFinish() {
        LogUtils.d(TAG, "syncdataFinish")
        checkCurrentContactCount()
        clearBubbleCount()
        Factory.get().getApplicationPrefsManager().setLastSyncSuccessTimestamp(System.currentTimeMillis())

        //同步成功,进度条走完
        this.target_process_percentage = PERCENTAGE_SYNC_DONE
    }

    fun logOutFailed(code: Int, msg: String) {
        ToastUtil.showToast(getActivity(), msg)
    }

    fun updateLogOutView() {
        mCacheStatisticsManager.clearCompareStatistics()
        Factory.get().getApplicationPrefsManager().setCloudCurrentContactCount(0)
        Factory.get().getApplicationPrefsManager().setServer_big_version(0)
    }

    fun setUpgradeInfo() {
        /*有升级提示  需要显示红点*/
        mIv_newVersion!!.visibility = View.VISIBLE
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun checkCanSyncSuccess(isCanSync: Boolean) {
        mInfoText_server!!.visibility = View.GONE
        mInfoText_local!!.visibility = View.GONE
        if (isCanSync) {
            syncData()
        } else {
            doLoading()
        }
    }

    private fun clearBubbleCount() {
        mLocalChangeCount!!.visibility = View.GONE
        mIcloudChangeCount!!.visibility = View.GONE
    }

    private fun showSyncSuccessLayout() {
        LogUtils.d(TAG, "showSyncSuccessLayout")
        checkCurrentContactCount()
        mSyncSuccessLayout!!.visibility = View.VISIBLE
        mFinalLastSyncLayout!!.visibility = View.GONE
        mSyscStatusIcon!!.setImageResource(R.drawable.cloud_ic_backup_success)
        mBgLayout!!.setBackgroundResource(R.drawable.cloud_bg_backup_green)
        mBgLayout!!.invalidate()
        mSyscStatusText!!.setText(R.string.sync_success)
        mSyscStatusTime!!.setText(DateUtils.getDateTime(Factory.get().getApplicationPrefsManager().getLastSyncSuccessTimestamp()))

        mSyncBtn!!.isClickable = true
        hideProcess()
        is_has_sync_success_done = true
        //同步成功清除匹配统计
        clearCacheCount()
        mCacheStatisticsManager.clearCompareStatistics()
    }

    private fun showSyncFailedLayout() {
        LogUtils.d(TAG, "showSyncFailedLayout")
        hideProcess()
        mSyncSuccessLayout!!.visibility = View.VISIBLE
        mFinalLastSyncLayout!!.visibility = View.GONE
        mSyscStatusIcon!!.setImageResource(R.drawable.cloud_ic_backup_wrong)
        mBgLayout!!.setBackgroundResource(R.drawable.cloud_bg_backup_red)
        mSyncBtn!!.setImageResource(R.drawable.sync_btn_selector)
        mSyncBtn!!.invalidate()
        mBgLayout!!.invalidate()
        mSyscStatusText!!.setText(R.string.sync_fail)
        mSyscStatusTime!!.text = ""
        mInfoTextLayout!!.visibility = View.VISIBLE
        mInfoText!!.text = ""
        mInfoTextLayout!!.invalidate()
        mInfoText!!.invalidate()

        is_has_sync_fail_done = true
    }

    private fun showLoadFailLayout() {
        LogUtils.d(TAG, "showLoadFailLayout")
        hideProcess()
        mInfoLoadErrorLayout!!.visibility = View.VISIBLE
        mInfoTextLayout!!.visibility = View.GONE
        mSyncBtn!!.isClickable = false
    }

    private fun hideLoadFailLayout() {
        LogUtils.d(TAG, "hideLoadFailLayout")
        mInfoLoadErrorLayout!!.visibility = View.GONE
    }

    private fun showInfoLayout() {
        LogUtils.d(TAG, "showInfoLayout")
        mInfoLoadErrorLayout!!.visibility = View.GONE
        mInfoTextLayout!!.visibility = View.VISIBLE
        mSyncBtn!!.isClickable = true
    }

    fun updateInfo() {

        LogUtils.d(TAG, "updateInfo Count :" + "本地新增 " + countAddLocal + " ,删除 " + countDeleteLocal + " ,修改 " + countUpdateLocal + "  "
                + "云端新增 " + countAddServer + " ,删除 " + countDeleteSever + " ,修改 " + countUpdateServer)

        //更新本地通讯录数目统计
        val localCount = countAddServer + countUpdateServer
        if (localCount > WakaIcloudConstant.INSTANCE.getBUBBLE_MAX_COUNT()) {
            mLocalChangeCount!!.setText(WakaIcloudConstant.INSTANCE.getBUBBLE_MAX_COUNT() + "+")
        } else {
            mLocalChangeCount!!.text = localCount.toString() + ""
        }
        if (countAddServer + countUpdateServer > 0) {
            mLocalChangeCount!!.visibility = View.VISIBLE
        } else {
            mLocalChangeCount!!.visibility = View.GONE
        }
        mLocalCurrentCount!!.setText(Factory.get().getApplicationPrefsManager().getLocalCurrentContactCount() + "")

        //更新云端通讯录数目统计
        val cloudCount = countAddLocal + countUpdateLocal + countDeleteLocal
        if (cloudCount > WakaIcloudConstant.INSTANCE.getBUBBLE_MAX_COUNT()) {
            mIcloudChangeCount!!.setText(WakaIcloudConstant.INSTANCE.getBUBBLE_MAX_COUNT() + "+")
        } else {

            mIcloudChangeCount!!.text = cloudCount.toString() + ""
        }
        if (countAddLocal + countUpdateLocal + countDeleteLocal > 0) {
            mIcloudChangeCount!!.visibility = View.VISIBLE
        } else {
            mIcloudChangeCount!!.visibility = View.GONE
        }
        mIcloudCurrentCount!!.setText(Factory.get().getApplicationPrefsManager().getCloudCurrentContactCount() + "")

        if (isRunningForeground) {
            showDeleteAlert()
        }

        if (is_sync_contact) {


            //            mInfoText.setText(getUpdateInfo());
            showUpdateInfo()
            if (mSyscInfoText!!.visibility == View.VISIBLE) {
                mSyscInfoText!!.setText(getString(R.string.click_to_restore))
            }
        } else {
            checkSyncBtnStatus(false)
        }


    }

    private fun showDeleteAlert() {
        //本地有删除，弹窗选择处理
        val message = String.format(getString(R.string.local_delete_alert_text), countDeleteLocal)
        if (countDeleteLocal >= WakaIcloudConstant.INSTANCE.getDELETE_COUNT_LIMIT()) {
            val builder = AlertDialog.Builder(this.getActivity())
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.confirm), DialogInterface.OnClickListener { dialog, which -> positiveToDeleteAlert() })
                    .setNegativeButton(getString(R.string.cancle), DialogInterface.OnClickListener { dialog, which -> negativeToDeleteAlert() })
            //builder.create();
            mAlertDialog = builder.show()
        }
    }

    private fun dismissDeleteAlert() {
        if (mAlertDialog != null) {
            mAlertDialog!!.dismiss()
            mAlertDialog = null
        }
    }

    private fun shutdownDeleteAlert() {
        if (mAlertDialog != null) {
            mAlertDialog!!.cancel()
            mAlertDialog = null
        }
    }

    private fun positiveToDeleteAlert() {
        countDeleteLocal = 0//点击处理数据时，清空界面的标记数量，避免重复弹窗
        //        syncData();
        getPresenter().CheckCanSync()
    }


    private fun negativeToDeleteAlert() {
        Factory.get().getCacheStatisticsManager().setDeleteLocalList(null)
        countDeleteLocal = 0//点击不处理数据时，清空界面的标记数量
        getPresenter().CheckCanSync()
        //        syncData();
    }

    private fun syncData() {
        initProcess()
        LoginUiHandler.sendEmptyMessage(SYNC_DATA)
    }


    private fun showUpdateInfo() {
        LogUtils.d(TAG, "getUpdateInfo")
        val localmsg = SpannableStringBuilder("")
        val servermsg = SpannableStringBuilder("")
        var localspanString = SpannableString("")
        var serverspanString = SpannableString("")
        mSyscInfoText!!.visibility = View.GONE
        if (countAddLocal > 0 || countDeleteLocal > 0 || countUpdateLocal > 0) {
            mInfoText!!.setText(R.string.sync_after)
            mInfoText_local!!.visibility = View.VISIBLE
            localspanString = SpannableString(getString(R.string.cloud_Contacts))
            //再构造一个改变字体颜色的Span
            val span = ForegroundColorSpan(getResources().getColor(R.color.icloud_b0d8c8))
            localspanString.setSpan(span, 0, localspanString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        } else {
            mInfoText_local!!.visibility = View.GONE
        }
        if (countAddServer > 0 || countUpdateServer > 0) {
            mInfoText!!.setText(R.string.sync_after)
            mInfoText_server!!.visibility = View.VISIBLE
            serverspanString = SpannableString(getString(R.string.contacts))
            //再构造一个改变字体颜色的Span
            val span = ForegroundColorSpan(getResources().getColor(R.color.icloud_b0d8c8))
            serverspanString.setSpan(span, 0, serverspanString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        } else {
            mInfoText_server!!.visibility = View.GONE
        }


        if (countAddLocal > 0 && countDeleteLocal == 0 && countUpdateLocal == 0) {
            localmsg.append(localspanString)
            localmsg.append("\n" + getString(R.string.info_add) + ":")
            localmsg.append(getNumberSpan(countAddLocal))
            mInfoText_local!!.text = localmsg
        } else if (countAddLocal == 0 && countDeleteLocal > 0 && countUpdateLocal == 0) {
            localmsg.append(localspanString)
            localmsg.append("\n" + getString(R.string.info_remove) + ":")
            localmsg.append(getNumberSpan(countDeleteLocal))
            mInfoText_local!!.text = localmsg
        } else if (countAddLocal == 0 && countDeleteLocal == 0 && countUpdateLocal > 0) {
            localmsg.append(localspanString)
            localmsg.append("\n" + getString(R.string.info_modify) + ":")
            localmsg.append(getNumberSpan(countUpdateLocal))
            mInfoText_local!!.text = localmsg
        } else if (countAddLocal == 0 && countDeleteLocal > 0 && countUpdateLocal > 0) {
            localmsg.append(localspanString)
            localmsg.append("\n" + getString(R.string.info_remove) + ":")
            localmsg.append(getNumberSpan(countDeleteLocal))
            localmsg.append("   " + getString(R.string.info_modify) + ":")
            localmsg.append(getNumberSpan(countUpdateLocal))
            mInfoText_local!!.text = localmsg
        } else if (countAddLocal > 0 && countDeleteLocal == 0 && countUpdateLocal > 0) {
            localmsg.append(localspanString)
            localmsg.append("\n" + getString(R.string.info_add) + ":")
            localmsg.append(getNumberSpan(countAddLocal))
            localmsg.append("   " + getString(R.string.info_modify) + ":")
            localmsg.append(getNumberSpan(countUpdateLocal))
            mInfoText_local!!.text = localmsg
        } else if (countAddLocal > 0 && countDeleteLocal > 0 && countUpdateLocal == 0) {
            localmsg.append(localspanString)
            localmsg.append("\n" + getString(R.string.info_add) + ":")
            localmsg.append(getNumberSpan(countAddLocal))
            localmsg.append("   " + getString(R.string.info_remove) + ":")
            localmsg.append(getNumberSpan(countDeleteLocal))
            mInfoText_local!!.text = localmsg
        } else if (countAddLocal > 0 && countDeleteLocal > 0 && countUpdateLocal > 0) {
            localmsg.append(localspanString)
            localmsg.append("\n" + getString(R.string.info_add) + ":")
            localmsg.append(getNumberSpan(countAddLocal))
            localmsg.append("   " + getString(R.string.info_remove) + ":")
            localmsg.append(getNumberSpan(countDeleteLocal))
            localmsg.append("   " + getString(R.string.info_modify) + ":")
            localmsg.append(getNumberSpan(countUpdateLocal))
            mInfoText_local!!.text = localmsg
        }

        if (countAddServer > 0 && countDeleteSever == 0 && countUpdateServer == 0) {
            servermsg.append(serverspanString)
            servermsg.append("\n" + getString(R.string.info_add) + ":")
            servermsg.append(getNumberSpan(countAddServer))
            mInfoText_server!!.text = servermsg

        } else if (countAddServer == 0 && countDeleteSever == 0 && countUpdateServer > 0) {
            servermsg.append(serverspanString)
            servermsg.append("\n" + getString(R.string.info_modify) + ":")
            servermsg.append(getNumberSpan(countUpdateServer))
            mInfoText_server!!.text = servermsg
        } else if (countAddServer == 0 && countDeleteSever > 0 && countUpdateServer > 0) {
            servermsg.append(serverspanString)
            servermsg.append("\n" + getString(R.string.info_modify) + ":")
            servermsg.append(getNumberSpan(countUpdateServer))
            mInfoText_server!!.text = servermsg
        } else if (countAddServer > 0 && countDeleteSever == 0 && countUpdateServer > 0) {
            servermsg.append(serverspanString)
            servermsg.append("\n" + getString(R.string.info_add) + ":")
            servermsg.append(getNumberSpan(countAddServer))
            servermsg.append("   " + getString(R.string.info_modify) + ":")
            servermsg.append(getNumberSpan(countUpdateServer))
            mInfoText_server!!.text = servermsg
        } else if (countAddServer > 0 && countDeleteSever > 0 && countUpdateServer == 0) {
            servermsg.append(serverspanString)
            servermsg.append("\n" + getString(R.string.info_add) + ":")
            servermsg.append(getNumberSpan(countAddServer))
            mInfoText_server!!.text = servermsg
        } else if (countAddServer > 0 && countDeleteSever > 0 && countUpdateServer > 0) {
            servermsg.append(serverspanString)
            servermsg.append("\n" + getString(R.string.info_add) + ":")
            servermsg.append(getNumberSpan(countAddServer))
            servermsg.append("   " + getString(R.string.info_modify) + ":")
            servermsg.append(getNumberSpan(countUpdateServer))
            mInfoText_server!!.text = servermsg
        }//        else if ( (countAddServer==0 && countDeleteSever>0 && countUpdateServer==0)  )
        //        {
        //            mInfoText_server.setVisibility(View.GONE);
        //            mInfoText_local.setVisibility(View.GONE);
        //            mInfoText.setText(getString(R.string.sync_no_change));
        //        }
        if (countLocal == 0 && Factory.get().getApplicationPrefsManager().getServer_big_version() < 1 && Factory.get().getApplicationPrefsManager().getCloudCurrentContactCount() > 0 &&
                mappingcount < 1) {
            mInfoText_local!!.visibility = View.GONE
            mInfoText_server!!.visibility = View.GONE
            mInfoText!!.setText(getString(R.string.local_empty))
            mSyscInfoText!!.visibility = View.VISIBLE
        } else if (mInfoText_local!!.visibility == View.GONE && mInfoText_server!!.visibility == View.GONE) {
            mInfoText!!.setText(getString(R.string.sync_no_change))
        }
        if (countLocal > 0) {
            mSyscInfoText!!.visibility = View.GONE
        }
    }


    private fun initProcess() {
        LogUtils.d(TAG, "initProcess")
        if (mSyscInfoText!!.visibility == View.VISIBLE) {
            mSyscInfoText!!.visibility = View.GONE
        }
        mInfoText!!.setText(getString(R.string.sync_start_ing) + "\n")
        hideLoadFailLayout()
        mProcessLayout!!.visibility = View.VISIBLE
        val params = mProcess!!.layoutParams as LinearLayout.LayoutParams
        params.width = 0//设置当前控件布局的高度,进度条清零
        mProcess!!.layoutParams = params//将设置好的布局参数应用到控件中
        mSyncBtn!!.isClickable = false
        mMenuBtn!!.isEnabled = false
        mProcessLayout!!.invalidate()
        setProcessValue(36)
        this.target_process_percentage = PERCENTAGE_START_SYNC
        doProcess(START_SYNC)
    }

    private fun hideProcess() {
        LoginUiHandler.removeMessages(START_SYNC)
        LoginUiHandler.removeMessages(UPDATE_PROCESS)
        this.target_process_percentage = PERCENTAGE_ZERO
        val params = mProcess!!.layoutParams as LinearLayout.LayoutParams
        params.width = 0//设置当前控件布局的高度,进度条清零
        mProcess!!.layoutParams = params//将设置好的布局参数应用到控件中
        mProcessLayout!!.visibility = View.INVISIBLE
        mSyncBtn!!.isClickable = true
        mProcessLayout!!.invalidate()
        mMenuBtn!!.isEnabled = true
        LogUtils.d(TAG, "hideProcess")

    }


    private fun doProcess(type: Int) {
        val params = mProcess!!.layoutParams as LinearLayout.LayoutParams
        val msg = Message()
        when (type) {
            START_SYNC -> {
                msg.what = UPDATE_PROCESS
                msg.arg2 = params.width
                LoginUiHandler.sendMessage(msg)
                LogUtils.d(TAG, "doProcess START_SYNC")
            }
        }

    }

    private fun setProcessValue(process_length: Int) {

        val params = mProcess!!.layoutParams as LinearLayout.LayoutParams
        //获取当前控件的布局对象
        if (params.width > process_length) {
            return
        } else {
            if (process_length - params.width <= INCREMENTAL_PROCESS) {
                params.width = process_length//设置当前控件布局的高度
                mProcess!!.layoutParams = params//将设置好的布局参数应用到控件中
                setProcessInfoUI(process_length)
                if (process_length == device_width) {
                    showSyncSuccessLayout()
                }
            }

        }

    }

    private fun setProcessInfoUI(process_length: Int) {
        var process_percentage = (process_length.toFloat() / device_width.toFloat() * 100).toInt()
        if (process_length == device_width) {
            process_percentage = 100
        }
        if (process_percentage >= PERCENTAGE_ZERO && process_percentage < PERCENTAGE_START_SYNC) {
            mInfoText!!.setText(getString(R.string.sync_start_ing) + "\n" + process_percentage + " %")
            return
        }
        if (process_percentage >= PERCENTAGE_START_SYNC && process_percentage < PERCENTAGE_SYNC_CONTACT_DONE) {
            if (is_sync_contact) {
                mInfoText!!.setText(getString(R.string.sync_contact_ing) + "\n" + process_percentage + " %")
                mSyncBtn!!.setImageResource(R.drawable.cloud_btn_contact_normal)
            } else {
                mInfoText!!.setText(getString(R.string.sync_process_ing) + "\n" + process_percentage + " %")
                mSyncBtn!!.setImageResource(R.drawable.sync_btn_selector)
            }
            return
        }
        if (process_percentage >= PERCENTAGE_SYNC_CONTACT_DONE && process_percentage < PERCENTAGE_SYNC_SMS_DONE) {
            if (is_sync_sms) {
                mInfoText!!.setText(getString(R.string.sync_sms_ing) + "\n" + process_percentage + " %")
                mSyncBtn!!.setImageResource(R.drawable.cloud_btn_messages_normal)
            } else {
                mInfoText!!.setText(getString(R.string.sync_process_ing) + "\n" + process_percentage + " %")
                mSyncBtn!!.setImageResource(R.drawable.sync_btn_selector)
            }
            return
        }
        if (process_percentage >= PERCENTAGE_SYNC_SMS_DONE && process_percentage < PERCENTAGE_SYNC_CALLLOG_DONE) {
            if (is_sync_phone) {
                mInfoText!!.setText(getString(R.string.sync_callLog_ing) + "\n" + process_percentage + " %")
                mSyncBtn!!.setImageResource(R.drawable.cloud_btn_call_normal)
            } else {
                mInfoText!!.setText(getString(R.string.sync_process_ing) + "\n" + process_percentage + " %")
                mSyncBtn!!.setImageResource(R.drawable.sync_btn_selector)
            }
            return
        }
        if (process_percentage >= PERCENTAGE_SYNC_CALLLOG_DONE && process_percentage < 99) {
            mInfoText!!.setText(getString(R.string.sync_process_ing) + "\n" + process_percentage + " %")
            mSyncBtn!!.setImageResource(R.drawable.sync_btn_selector)
            return
        }
        if (process_percentage >= 99) {
            mInfoText!!.text = ""
            mSyncBtn!!.setImageResource(R.drawable.sync_btn_selector)

            showSyncSuccessLayout()
            return
        }

    }

    private fun clearCacheCount() {
        countAddLocal = 0
        countDeleteLocal = 0
        countUpdateLocal = 0
        countAddServer = 0
        countDeleteSever = 0
        countUpdateServer = 0
    }

    private fun showAlertPopWindow(alertType: Int) {
        val contentView = LayoutInflater.from(getContext()).inflate(
                R.layout.alert_choose_pop_window, null)
        val popupWindow = PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true)
        val outLayout: View
        val alert_info: TextView
        val action1: TextView
        val action2: TextView
        val action3: TextView
        outLayout = contentView.findViewById(R.id.out)
        alert_info = contentView.findViewById(R.id.alert_info)
        action1 = contentView.findViewById(R.id.action1)
        action2 = contentView.findViewById(R.id.action2)
        action3 = contentView.findViewById(R.id.action3)

        when (alertType) {
            LOCAL_HAS_DELETE_ALERT -> {
                //TODO:
                alert_info.text = getAlertInfoText(getString(R.string.local_delete_info_text_before),
                        0,
                        getString(R.string.local_delete_info_text_after))
                action1.setText(getString(R.string.local_delete_action1_text))
                action2.setText(getString(R.string.local_delete_action2_text))
                action3.setText(getString(R.string.local_delete_action3_text))
            }
            ICLOUD_HAS_DELETE_ALERT -> {
                val tempBefore = String.format(getString(R.string.icloud_delete_info_text_before), "XXX")
                alert_info.text = getAlertInfoText(tempBefore,
                        0,
                        getString(R.string.icloud_delete_info_text_after))
                action1.setText(getString(R.string.icloud_delete_action1_text))
                action2.setText(getString(R.string.icloud_delete_action2_text))
                action3.setText(getString(R.string.icloud_delete_action3_text))
            }
            UPDATE_CONFLICT_ALERT -> {
                val tempBefore2 = String.format(getString(R.string.update_conflict_info_text_before), "XXX")
                alert_info.text = getAlertInfoText(tempBefore2,
                        0,
                        getString(R.string.update_conflict_info_text_after))
                action1.setText(getString(R.string.update_conflict_action1_text))
                action2.setText(getString(R.string.update_conflict_action2_text))
                action3.setText(getString(R.string.update_conflict_action3_text))
            }
            else -> {
            }
        }

        outLayout.setOnClickListener { popupWindow.dismiss() }

        action1.setOnClickListener {
            when (alertType) {
                LOCAL_HAS_DELETE_ALERT -> {
                }
                ICLOUD_HAS_DELETE_ALERT -> {
                }
                UPDATE_CONFLICT_ALERT -> {
                }
            }
            popupWindow.dismiss()
        }

        action2.setOnClickListener {
            when (alertType) {
                LOCAL_HAS_DELETE_ALERT -> {
                }
                ICLOUD_HAS_DELETE_ALERT -> {
                }
                UPDATE_CONFLICT_ALERT -> {
                }
            }
            popupWindow.dismiss()
        }

        action3.setOnClickListener {
            when (alertType) {
                LOCAL_HAS_DELETE_ALERT -> {
                }
                ICLOUD_HAS_DELETE_ALERT -> {
                }
                UPDATE_CONFLICT_ALERT -> {
                }
            }
            popupWindow.dismiss()
        }

        popupWindow.isTouchable = true

        popupWindow.isOutsideTouchable = true

        popupWindow.setOnDismissListener { }
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0)
    }

    fun getAlertInfoText(before: String, num: Int, after: String): SpannableStringBuilder {
        //改变字体颜色
        //先构造SpannableString
        val spanString = SpannableString(num.toString() + "")
        //再构造一个改变字体颜色的Span
        val span = ForegroundColorSpan(getResources().getColor(R.color.icloud_1aaa70))
        //将这个Span应用于指定范围的字体
        spanString.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        val result = SpannableStringBuilder()
        result.append(before + " ")
        result.append(spanString)
        result.append(" " + after)
        return result
    }


    fun getNumberSpan(number: Int): SpannableStringBuilder {
        var num = ""
        if (number > 999) {
            num = "999+"
        } else
            num = number.toString() + ""
        val spanString = SpannableString(num)
        val result = SpannableStringBuilder()
        val span = ForegroundColorSpan(getResources().getColor(R.color.icloud_a8ff01))
        spanString.setSpan(span, 0, spanString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        result.append(spanString)
        return result
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        getPresenter().onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == WakaIcloudConstant.INSTANCE.getICLOUD_LOGIN_RESULT_CODE()) {
                logninSuccess(false)
            } else if (requestCode == WakaIcloudConstant.INSTANCE.getREQ_SETDEFAULT_SMS_RESULT_CODE()) {
                ApplicationPrefsManager.getInstance(getActivity()).setSyncMessages(true)
            }
        } else {
            if (requestCode == WakaIcloudConstant.INSTANCE.getREQ_SETDEFAULT_SMS_RESULT_CODE()) {
                ApplicationPrefsManager.getInstance(getActivity()).setSyncMessages(false)
            }
        }
    }

    companion object {

        val TAG = WakaIcloudMainFragment::class.java.simpleName

        fun newInstance(): WakaIcloudMainFragment {
            val fragment = WakaIcloudMainFragment()
            val args = Bundle()
            fragment.setArguments(args)
            return fragment
        }

        val UPDATE_STATISTICS = 0
        val SYNC_DATA = 4
        val UPDATE_PROCESS = 5


        private val INCREMENTAL_PROCESS = 3

        private val START_SYNC = 1 //process_percentage = 10

        val PERCENTAGE_ZERO = 0 //process_percentage = 10
        val PERCENTAGE_START_SYNC = 10 //process_percentage = 10
        val PERCENTAGE_SYNC_CONTACT_DONE = 40//process_percentage = 40
        val PERCENTAGE_SYNC_SMS_DONE = 70//process_percentage = 70
        val PERCENTAGE_SYNC_CALLLOG_DONE = 90//process_percentage = 90
        val PERCENTAGE_SYNC_DONE = 100//process_percentage = 100

        val LOCAL_HAS_DELETE_ALERT = 1
        val ICLOUD_HAS_DELETE_ALERT = 2
        val UPDATE_CONFLICT_ALERT = 3
    }
}
