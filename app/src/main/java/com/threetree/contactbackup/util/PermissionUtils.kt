package com.threetree.contactbackup.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.app.AlertDialog
import android.util.Log

import com.threetree.contactbackup.Factory

import java.util.ArrayList
import java.util.HashMap

object PermissionUtils {

    private val TAG = PermissionUtils::class.java.simpleName

    val CODE_INTERNET = 0
    val CODE_READ_CALL_LOG = CODE_INTERNET + 1
    val CODE_WRITE_CALL_LOG = CODE_READ_CALL_LOG + 1
    val CODE_READ_CONTACTS = CODE_WRITE_CALL_LOG + 1
    val CODE_WRITE_CONTACTS = CODE_READ_CONTACTS + 1
    val CODE_READ_SMS = CODE_WRITE_CONTACTS + 1
    val CODE_WRITE_SMS = CODE_READ_SMS + 1

    val CODE_MULTI_PERMISSION = 100
    val CODE_HAS_PERMISSION_NOT_GRANTED = 1000

    val PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
    val PERMISSION_GET_ACCOUNTS = Manifest.permission.GET_ACCOUNTS
    val PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
    val PERMISSION_CALL_PHONE = Manifest.permission.CALL_PHONE
    val PERMISSION_CAMERA = Manifest.permission.CAMERA
    val PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    val PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    val PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    val PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val PERMISSION_INTERNET = Manifest.permission.INTERNET
    val PERMISSION_READ_CALL_LOG = Manifest.permission.READ_CALL_LOG
    val PERMISSION_WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG
    val PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS
    val PERMISSION_WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS
    val PERMISSION_READ_SMS = Manifest.permission.READ_SMS
    val PERMISSION_WRITE_SMS = "android.permission.WRITE_SMS"


    /**
     * iCloud 需要的权限
     * 后续可在此添加
     */
    var requestPermissions = arrayOf(PERMISSION_INTERNET, PERMISSION_READ_CALL_LOG, PERMISSION_WRITE_CALL_LOG, PERMISSION_READ_CONTACTS, PERMISSION_WRITE_CONTACTS, PERMISSION_READ_SMS, PERMISSION_WRITE_SMS)

    val isAuthContactPermission: Boolean
        get() {
            val context = Factory.get().getApplicationContext()
            return PermissionChecker.checkSelfPermission(context, PERMISSION_READ_CONTACTS) == PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(context, PERMISSION_WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
        }

    val isAuthSMSPermission: Boolean
        get() {
            val context = Factory.get().getApplicationContext()
            return PermissionChecker.checkSelfPermission(context, PERMISSION_READ_SMS) == PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(context, PERMISSION_WRITE_SMS) == PackageManager.PERMISSION_GRANTED
        }

    val isAuthPhonePermission: Boolean
        get() {
            val context = Factory.get().getApplicationContext()
            return PermissionChecker.checkSelfPermission(context, PERMISSION_READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED && PermissionChecker.checkSelfPermission(context, PERMISSION_WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        }

    @JvmStatic
    fun setPermissionsArray(add: Array<String>) {
        if (add == null) {
            return
        }
        requestPermissions = Union(requestPermissions, add)
    }

    //得到两个数组的并集
    private fun Union(a1: Array<String>, a2: Array<String>): Array<String> {
        val list1 = ArrayList<String>()
        val list2 = ArrayList<String>()
        for (i in a1)
            list1.add(i)
        for (i in a2)
            list2.add(i)
        list1.removeAll(list2)
        list2.addAll(list1)
        return list2.toTypedArray()
    }

    interface PermissionGrant {
        fun onPermissionGranted(requestCode: Int)
        fun onTUDCPermissionGranted(permission: Array<String>)
    }

    /**
     * 请求单个权限
     *
     * @param activity
     * @param requestCode request permission, e.g. if you need request CAMERA permission,parameters is PermissionUtils.CODE_CAMERA
     * @param permissionGrant
     */
    fun requestPermission(activity: Activity?, requestCode: Int, permissionGrant: PermissionGrant) {
        if (activity == null) {
            return
        }

        Log.i(TAG, "requestPermission requestCode:" + requestCode)
        if (requestCode < 0 || requestCode >= requestPermissions.size) {
            Log.w(TAG, "requestPermission illegal requestCode:" + requestCode)
            return
        }

        val requestPermission = requestPermissions[requestCode]

        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang.RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以在这个地方，低于23就什么都不做，
        // 个人建议try{}catch(){}单独处理，提示用户开启权限。
        //        if (Build.VERSION.SDK_INT < 23) {
        //            return;
        //        }

        val checkSelfPermission: Int
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission)
        } catch (e: RuntimeException) {
            /*Toast.makeText(activity, "please open this permission", Toast.LENGTH_SHORT)
                    .show();*/
            Log.e(TAG, "RuntimeException:" + e.message)
            return
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "ActivityCompat.checkSelfPermission != PackageManager.PERMISSION_GRANTED")


            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                Log.i(TAG, "requestPermission shouldShowRequestPermissionRationale")
                shouldShowRationale(activity, requestCode, requestPermission)

            } else {
                Log.d(TAG, "requestCameraPermission else")
                ActivityCompat.requestPermissions(activity, arrayOf(requestPermission), requestCode)
            }

        } else {
            Log.d(TAG, "ActivityCompat.checkSelfPermission ==== PackageManager.PERMISSION_GRANTED")
            //Toast.makeText(activity, "opened:" + requestPermissions[requestCode], Toast.LENGTH_SHORT).show();
            permissionGrant.onPermissionGranted(requestCode)
        }
    }

    private fun requestMultiResult(activity: Activity?, permissions: Array<String>, grantResults: IntArray, permissionGrant: PermissionGrant) {

        if (activity == null) {
            return
        }

        //TODO
        Log.d(TAG, "onRequestPermissionsResult permissions length:" + permissions.size)
        val perms = HashMap<String, Int>()

        val notGranted = ArrayList<String>()
        for (i in permissions.indices) {
            Log.d(TAG, "permissions: [i]:" + i + ", permissions[i]" + permissions[i] + ",grantResults[i]:" + grantResults[i])
            perms.put(permissions[i], grantResults[i])
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permissions[i])
            }
        }

        if (notGranted.size == 0) {
            /*Toast.makeText(activity, "all permission success" + notGranted, Toast.LENGTH_SHORT)
                    .show();*/
            permissionGrant.onPermissionGranted(CODE_MULTI_PERMISSION)
            permissionGrant.onTUDCPermissionGranted(permissions)
        } else {
            permissionGrant.onPermissionGranted(CODE_HAS_PERMISSION_NOT_GRANTED)
            //openSettingActivity(activity, "those permission need granted!");
        }

    }


    /**
     * 请求多个权限
     *
     * @param activity
     * @param grant
     */
    fun requestMultiPermissions(activity: Activity, grant: PermissionGrant) {

        val permissionsList = getNoGrantedPermission(activity, false)
        val shouldRationalePermissionsList = getNoGrantedPermission(activity, true)

        //TODO checkSelfPermission
        if (permissionsList == null || shouldRationalePermissionsList == null) {
            return
        }
        Log.d(TAG, "requestMultiPermissions permissionsList:" + permissionsList.size + ",shouldRationalePermissionsList:" + shouldRationalePermissionsList.size)

        if (permissionsList.size > 0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toTypedArray(),
                    CODE_MULTI_PERMISSION)
            Log.d(TAG, "showMessageOKCancel requestPermissions")

        } else if (shouldRationalePermissionsList.size > 0) {

            ActivityCompat.requestPermissions(activity, shouldRationalePermissionsList.toTypedArray(),
                    CODE_MULTI_PERMISSION)
            Log.d(TAG, "showMessageOKCancel requestPermissions")

        } else {
            grant.onPermissionGranted(CODE_MULTI_PERMISSION)
        }

    }


    private fun shouldShowRationale(activity: Activity, requestCode: Int, requestPermission: String) {
        //TODO
        /*String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
        showMessageOKCancel(activity, "Rationale: " + permissionsHint[requestCode], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{requestPermission},
                        requestCode);
                Log.d(TAG, "showMessageOKCancel requestPermissions:" + requestPermission);
            }
        });*/
    }

    private fun showMessageOKCancel(context: Activity, message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()

    }

    /**
     * @param activity
     * @param requestCode  Need consistent with requestPermission
     * @param permissions
     * @param grantResults
     */
    fun requestPermissionsResult(activity: Activity?, requestCode: Int, permissions: Array<String>,
                                 grantResults: IntArray, permissionGrant: PermissionGrant) {

        if (activity == null) {
            return
        }
        Log.d(TAG, "requestPermissionsResult requestCode:" + requestCode)

        if (requestCode == CODE_MULTI_PERMISSION) {
            requestMultiResult(activity, permissions, grantResults, permissionGrant)
            return
        }

        if (requestCode < 0 || requestCode >= requestPermissions.size) {
            Log.w(TAG, "requestPermissionsResult illegal requestCode:" + requestCode)
            //Toast.makeText(activity, "illegal requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
            return
        }

        Log.i(TAG, "onRequestPermissionsResult requestCode:" + requestCode + ",permissions:" + permissions.toString()
                + ",grantResults:" + grantResults.toString() + ",length:" + grantResults.size)

        if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onRequestPermissionsResult PERMISSION_GRANTED")
            //TODO success, do something, can use callback
            permissionGrant.onPermissionGranted(requestCode)

        } else {
            //TODO hint user this permission function
            Log.i(TAG, "onRequestPermissionsResult PERMISSION NOT GRANTED")
            //TODO
            //            String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
            //            openSettingActivity(activity,  "Result" + permissionsHint[requestCode]);
        }

    }

    private fun openSettingActivity(activity: Activity, message: String) {

        showMessageOKCancel(activity, message, DialogInterface.OnClickListener { dialog, which ->
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            Log.d(TAG, "getPackageName(): " + activity.packageName)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        })
    }


    /**
     * @param activity
     * @param isShouldRationale true: return no granted and shouldShowRequestPermissionRationale permissions, false:return no granted and !shouldShowRequestPermissionRationale
     * @return
     */
    fun getNoGrantedPermission(activity: Activity, isShouldRationale: Boolean): ArrayList<String>? {

        val permissions = ArrayList<String>()

        for (i in requestPermissions.indices) {
            val requestPermission = requestPermissions[i]


            //TODO checkSelfPermission
            var checkSelfPermission = -1
            try {
                checkSelfPermission = ContextCompat.checkSelfPermission(activity, requestPermission)
            } catch (e: RuntimeException) {
                /*Toast.makeText(activity, "please open those permission", Toast.LENGTH_SHORT)
                        .show();*/
                Log.e(TAG, "RuntimeException:" + e.message)
                return null
            }

            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "getNoGrantedPermission ActivityCompat.checkSelfPermission != PackageManager.PERMISSION_GRANTED:" + requestPermission)

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                    Log.d(TAG, "shouldShowRequestPermissionRationale if")
                    if (isShouldRationale) {
                        permissions.add(requestPermission)
                    }

                } else {

                    if (!isShouldRationale) {
                        permissions.add(requestPermission)
                    }
                    Log.d(TAG, "shouldShowRequestPermissionRationale else")
                }

            }
        }

        return permissions
    }

}
