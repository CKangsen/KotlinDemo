package com.threetree.contactbackup.util

import android.content.Context
import android.text.TextUtils
import android.text.format.Time
import android.view.View
import com.threetree.contactbackup.util.DateUtils.dateFormate_year_month_day
import com.threetree.contactbackup.util.DateUtils.formate_month_day


import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


object DateUtils {
    // 一天
    private val ONE_DAYS = (86400 * 1000).toLong()

    private var dateFormate_list: DateFormat? = null

    private var dateFormate_week: DateFormat? = null

    private var dateFormate_month: DateFormat? = null

    private val dateFormate_list_PATTEN = "HH:mm"
    private val dateFormate_week_PATTEN = "EEE HH:mm"
    private val dateFormate_month_PATTEN_EN = "MMM dd HH:mm"
    private val dateFormate_week_format = "EEE"
    private val dateFormate_month_day = "MMM dd"
    private val dateFormate_year_month_day = "yyyy.MM.dd"
    private val formate_month_day = "MM/dd"
    // 5.0
    private val ONE_MINUTE = 60000L
    private val ONE_HOUR = 3600000L
    private val ONE_DAY = 86400000L
    private val ONE_WEEK = 604800000L

    private val HOUR_BY_MIN = 60
    private val DAY_BY_MIN = HOUR_BY_MIN * 24


    private//sdf.setTimeZone(TimeZone.getDefault());
    val dateYYYY_MM_dd: DateFormat
        @Synchronized get() = SimpleDateFormat(dateFormate_year_month_day)


    private val mM_dd: DateFormat
        @Synchronized get() = SimpleDateFormat(formate_month_day)

    /**
     * 获取6个月前当前时间戳
     * @return
     */
    //获取当前时间
    //得到日历
    //把当前时间赋给日历
    //设置为前6月
    //获取2个月前的时间
    val smSstartTs: Long
        get() {
            var da = Date()
            val calendar = Calendar.getInstance()
            calendar.time = da
            calendar.add(calendar.MONTH, -6)
            da = calendar.time
            return da.time
        }


    fun compare_date(datatime: String, Systemdatatime: String): Boolean {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            val dt1 = df.parse(datatime)
            val dt2 = df.parse(Systemdatatime)
            val time = dt2.time - dt1.time
            return if (time >= ONE_MINUTE) {
                true
            } else {
                false
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return false
    }


    /**
     * language change(area change area)
     */
    fun laguageOrTimeZoneChanged() {
        dateFormate_week = null
        dateFormate_month = null
        dateFormate_list = null
    }


    /**
     * 格式化时间
     *
     * @param milliseconds
     * @param pattern
     * @return
     */
    fun getFormatDateTime(milliseconds: Long, pattern: String): String {
        val date = Date(milliseconds)
        val sf = SimpleDateFormat(pattern)
        return sf.format(date)
    }

    fun getHours(time: Long): Long? {
        val diff = System.currentTimeMillis() - time// 这样得到的差值是微秒级别
        val days = diff / (1000 * 60 * 60 * 24)
        return (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
    }

    private fun toSeconds(date: Long): Long {
        return date / 1000L
    }

    private fun toMinutes(date: Long): Long {
        return toSeconds(date) / 60L
    }

    private fun toHours(date: Long): Long {
        return toMinutes(date) / 60L
    }

    private fun toDays(date: Long): Long {
        return toHours(date) / 24L
    }

    private fun toMonths(date: Long): Long {
        return toDays(date) / 30L
    }

    private fun toYears(date: Long): Long {
        return toMonths(date) / 365L
    }

    fun getDateTime(currentTime: String): Date? {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            date = formatter.parse(currentTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return date
    }

    fun getDateTime(currentTime: Long?): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd")
        val date = Date(currentTime!!)
        return formatter.format(date)
    }


    /**
     * 显示时间(xxHxxm)和日期（xxMxxD）
     *
     * @param context
     * @param when
     * @return
     */
    fun formatTimeStampString(context: Context, `when`: Long): String {
        val then = Time()
        then.set(`when`)
        val now = Time()
        now.setToNow()
        var date = ""
        // Basic settings for formatDateTime() we want for all cases.
        var format_flags = android.text.format.DateUtils.FORMAT_NO_NOON_MIDNIGHT or
                android.text.format.DateUtils.FORMAT_ABBREV_ALL or
                android.text.format.DateUtils.FORMAT_CAP_AMPM

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags = format_flags or (android.text.format.DateUtils.FORMAT_SHOW_YEAR or android.text.format.DateUtils.FORMAT_SHOW_DATE)
            date = getDateToStringYear(`when`)
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            format_flags = format_flags or android.text.format.DateUtils.FORMAT_SHOW_DATE
            date = getDateToStringDay(`when`)
        } else {
            // Otherwise, if the message is from today, show the time.
            format_flags = format_flags or android.text.format.DateUtils.FORMAT_SHOW_TIME
            date = android.text.format.DateUtils.formatDateTime(context, `when`, format_flags)
        }
        return date
    }


    fun getDateToStringDay(time: Long): String {
        val d = Date(time)
        val sf = SimpleDateFormat("MM/dd")
        return sf.format(d)
    }

    fun getDateToStringYear(time: Long): String {
        val d = Date(time)
        val sf = SimpleDateFormat("yyyy/MM/dd")
        return sf.format(d)
    }

    fun getFileDateToString(time: Long): String {
        val d = Date(time)
        val sf = SimpleDateFormat("MM/dd/yyyy")
        return sf.format(d)
    }

    /**
     * 比较时间
     *
     * @param yearText
     * @param monthText
     * @param dayText
     * @param view
     * @return
     */
    fun compareDate(yearText: CharSequence, monthText: CharSequence, dayText: CharSequence, view: View): Boolean {

        try {
            val df = SimpleDateFormat("yyyy-MM-dd")
            val selected = df.parse(yearText.toString() + "-" + monthText + "-" + dayText)
            val today = Date()
            if (selected.time - today.time < 0) {
                view.isEnabled = true
                view.isClickable = true
                return true
            }
        } catch (e: ParseException) {

            e.printStackTrace()
        }

        view.isEnabled = false
        view.isClickable = false
        return false
    }

    /**
     * 年月日 转日月年
     *
     * @param dateStr
     * @return
     */
    fun yyyyMMddToddMMyyyy(dateStr: String): String? {
        var resultStr = dateStr
        try {

            if (!TextUtils.isEmpty(dateStr) && dateStr.contains("-")) {
                val str = dateStr.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (str != null && str.size == 3 && str[2].length == 2 && str[0].length == 4) {
                    resultStr = str[2] + "-" + str[1] + "-" + str[0]

                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return resultStr
    }


}
