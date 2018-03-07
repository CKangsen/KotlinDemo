package com.threetree.contactbackup.constant


object WakaIcloudConstant {

    val BUBBLE_MAX_COUNT = 999

    /********************注册类型开始 */
    /**
     * 手机号注册
     */
    val TYPE_REGISTER_PHONE = 0
    /**
     * 第三方登录，未绑定TUDC
     */
    val TYPE_REGISTER_THIRD_ACCOUNT_NOT_BIND_UID = 1
    /**
     * 第三方登录，已绑定TUDC，未绑定手机号的情况
     */
    val TYPE_REGISTER_THIRD_ACCOUNT_BINDED_UID = 2
    /********************注册类型结束 */

    /********************操作类型开始 */
    /**
     * 绑定手机
     */
    val BIND_PHONE = 0
    /**
     * 解绑手机
     */
    val UNBIND_PHONE = 1

    /********************操作类型结束 */

    /**
     * 密码最短长度
     */
    val INPUT_PASSWORD_MIN_LENGTH = 8

    /**
     * 验证码最短长度
     */
    val INPUT_IDENTIFY_CODE_MIN_LENGTH = 6

    /**
     * 手机号最短长度
     */
    val INPUT_PHONE_CODE_MIN_LENGTH = 8

    /**the max length of password */
    val PASSWORD_MAX_LENGTH = 20
    /**the min length of password */
    val PASSWORD_MIN_LENGTH = 8
    /**the min length of phone number */
    val PHONE_NUM_MIN_LENGTH = 8

    val RESEND_CODE_TIME = 60 * 1000
    val ICLOUD_SELECT_COUNTRY_RESULT_CODE = 1001
    val ICLOUD_LOGIN_RESULT_CODE = 11
    val REQUEST_CODE_CHANGEPASSWORD = 1

    val FLAG_NULL = 0
    val FLAG_CONTACT = 1
    val FLAG_SMS = 2
    val FLAG_CONTACT_SMS = 3
    val FLAG_CONTACT_SMS_CALL = 4
    val FLAG_CALL = 5
    val FLAG_CONTACT_CALL = 6
    val FLAG_SMS_CALL = 7


    val DELETE_COUNT_LIMIT = 20

    val REQ_CODE_UNNETWORK = 4096 //not network
    val REQ_CODE_TOKEN_ERROR = 120002
    val REQ_CODE_SYNC_ERROR = 160003

    val REQ_SETDEFAULT_SMS_RESULT_CODE = 1002

    //action
    val HANDLE_SMS_SERVICE_ACTION = "com.afmobi.wakacloud.HandleSmsService"

    val SMS_CONTENT = "sms_content"
    val SMS_ADDRESS = "sms_address"
    val VERIFYCODE_SMS_FLAG_1 = "TranssionID"
    val VERIFYCODE_SMS_FLAG_2 = "英富必"
    val VERIFYCODE_LOGIN_ACTIVITY_NAME = "com.afmobi.wakacloud.ui.login.LoginActivity"
    val VERIFYCODE_REGISTER_ACTIVITY_NAME = "com.afmobi.wakacloud.ui.register.RegisterPhoneIdentifyingCodeDemo"


}
