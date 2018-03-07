
package com.threetree.contactbackup.util

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message

import com.threetree.contactbackup.Factory

import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date

/**
 *
 * log
 */

object LogUtils {
    /**log最大值 */
    private val LOG_FILE_SIZE = 5242880// 5 * 1024 * 1024 = 5MB
    /**日期格式 */
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

    internal var looperThread: LooperThread? = null

    init {
        looperThread = LogUtils.LooperThread()
        looperThread!!.name = "LogUtils5242880"
        looperThread!!.start()
    }

    /**
     * log输出
     * @param tag
     * @param msg
     */
    fun e(tag: String, msg: String) {
        //storeLogInfo(tag, msg, "E");
        val message = Message()
        val bundle = Bundle()
        bundle.putString("tag", tag)
        bundle.putString("msg", msg)
        bundle.putString("priority", "E")
        message.what = LooperThread.WRITH_LOG
        message.data = bundle
        if (looperThread != null && looperThread!!.handler != null) {
            looperThread!!.handler!!.sendMessage(message)
        }
    }

    /**
     * log输出
     * @param tag
     * @param msg
     */
    fun e(tag: String, isStore: Boolean, msg: String) {
        if (isStore) {
            storeLogInfo(tag, msg, "E")
            val message = Message()
            val bundle = Bundle()
            bundle.putString("tag", tag)
            bundle.putString("msg", msg)
            bundle.putString("priority", "E")
            message.what = LooperThread.WRITH_LOG
            message.data = bundle
            if (looperThread != null && looperThread!!.handler != null) {
                looperThread!!.handler!!.sendMessage(message)
            }
        }
    }

    /**
     * log输出
     * @param tag
     * @param msg
     */
    fun d(tag: String, msg: String) {
        //storeLogInfo(tag, msg, "D");
        val message = Message()
        val bundle = Bundle()
        bundle.putString("tag", tag)
        bundle.putString("msg", msg)
        bundle.putString("priority", "D")
        message.what = LooperThread.WRITH_LOG
        message.data = bundle
        if (looperThread != null && looperThread!!.handler != null) {
            looperThread!!.handler!!.sendMessage(message)
        }
    }

    /**
     * log输出
     * @param tag
     * @param msg
     */
    fun v(tag: String, msg: String) {
        //storeLogInfo(tag, msg, "V");
        val message = Message()
        val bundle = Bundle()
        bundle.putString("tag", tag)
        bundle.putString("msg", msg)
        bundle.putString("priority", "V")
        message.what = LooperThread.WRITH_LOG
        message.data = bundle
        if (looperThread != null && looperThread!!.handler != null) {
            looperThread!!.handler!!.sendMessage(message)
        }
    }

    /**
     * log输出
     * @param tag
     * @param msg
     */
    fun i(tag: String, msg: String) {
        //storeLogInfo(tag, msg, "I");
        val message = Message()
        val bundle = Bundle()
        bundle.putString("tag", tag)
        bundle.putString("msg", msg)
        bundle.putString("priority", "I")
        message.what = LooperThread.WRITH_LOG
        message.data = bundle
        if (looperThread != null && looperThread!!.handler != null) {
            looperThread!!.handler!!.sendMessage(message)
        }
    }

    /**
     * log输出
     * @param tag
     * @param msg
     */
    fun i(tag: String, isStore: Boolean, msg: String) {
        if (isStore) {
            //storeLogInfo(tag, msg, "I");
            val message = Message()
            val bundle = Bundle()
            bundle.putString("tag", tag)
            bundle.putString("msg", msg)
            bundle.putString("priority", "I")
            message.what = LooperThread.WRITH_LOG
            message.data = bundle
            if (looperThread != null && looperThread!!.handler != null) {
                looperThread!!.handler!!.sendMessage(message)
            }
        }
    }

    /**
     * log输出
     * @param tag
     * @param msg
     */
    fun w(tag: String, msg: String) {
        val message = Message()
        val bundle = Bundle()
        bundle.putString("tag", tag)
        bundle.putString("msg", msg)
        bundle.putString("priority", "W")
        message.what = LooperThread.WRITH_LOG
        message.data = bundle
        if (looperThread != null && looperThread!!.handler != null) {
            looperThread!!.handler!!.sendMessage(message)
        }
        // storeLogInfo(tag, msg, "W");
    }

    fun writePerformanceLog(msg: String) {

        val message = Message()
        val bundle = Bundle()
        bundle.putString("tag", "[performance]")
        bundle.putString("msg", msg)
        bundle.putString("priority", "D")
        message.what = LooperThread.WRITH_PERFORMANCE_LOG
        message.data = bundle
        if (looperThread != null && looperThread!!.handler != null) {
            looperThread!!.handler!!.sendMessage(message)
        }
        // storeLogInfo(tag, msg, "W");
    }

    internal class LooperThread : Thread() {
        var handler: Handler? = null
        var looper: Looper? = null

        override fun run() {
            Looper.prepare()
            looper = Looper.myLooper()
            handler = object : Handler() {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        WRITH_LOG -> {
                            val bundle = msg.data
                            if (bundle != null) {
                                val tag = bundle.getString("tag")
                                val msgContent = bundle.getString("msg")
                                val priority = bundle.getString("priority")
                                storeLogInfo(tag, msgContent, priority)
                            }
                        }
                        WRITH_PERFORMANCE_LOG -> {
                            val bundleP = msg.data
                            if (bundleP != null) {
                                val tag = bundleP.getString("tag")
                                val msgContent = bundleP.getString("msg")
                                val priority = bundleP.getString("priority")
                                // storePerformanceLogInfo(tag, msgContent, priority);
                            }
                        }
                    }

                }
            }
            Looper.loop()
        }

        companion object {
            private val WRITH_LOG = 9999
            private val WRITH_PERFORMANCE_LOG = 9998
        }
    }

    /**
     * log保存到文件
     * @param tag
     * @param msg
     * @param priority
     */
    private fun storeLogInfo(tag: String?, msg: String?, priority: String?) {

        //        if(!BuildApkConfig.isDebugModel()){
        //            return;
        //        }
        println("TEST------------storeLogInfo-------------")
        synchronized(LogUtils::class.java) {
            var path = Environment.getExternalStorageDirectory().absolutePath + File.separator + "wakaicloud" + File.separator
            if (Factory.get().getApplicationContext() != null) {
                val externalFilesDir = Factory.get().getApplicationContext().getExternalFilesDir(null)
                if (externalFilesDir != null) {
                    path = externalFilesDir!!.getAbsolutePath() + File.separator
                }
            }
            mkdirectory(path)

            path += "javalog.txt"


            val logFile = File(path)
            if (logFile.exists()) {
                val size = logFile.length()
                if (size >= LOG_FILE_SIZE) {
                    val isDelete = logFile.delete()
                    println("storeLogInfo isDelete:" + isDelete)
                }
            }
            var outPutStream: FileWriter? = null
            var out: PrintWriter? = null
            try {
                println("TEST------------storeLogInfo---------write--path==" + path)
                outPutStream = FileWriter(logFile, true)
                out = PrintWriter(outPutStream)
                out.println(dateFormat.format(Date(System.currentTimeMillis())) + "sdkversion:" + "	" + priority
                        + "	" + ">>" + tag + "<<    	" + msg + '\r')
                outPutStream.flush()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    if (null != outPutStream) {
                        outPutStream.close()
                    }
                    if (out != null) {
                        out.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun toMib(size: Long): Long {
        return size / 1024 / 1024
    }

    fun mkdirectory(file: String) {
        val f = File(file)
        if (!f.exists()) {
            f.mkdirs()
        }
    }

}
