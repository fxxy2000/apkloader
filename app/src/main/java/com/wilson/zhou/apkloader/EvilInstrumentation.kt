package com.wilson.zhou.apkloader

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log

import java.lang.reflect.Method

/**
 * Created by wilso on 12/17/2017.
 */

class EvilInstrumentation(
        internal var mBase: Instrumentation) : Instrumentation() {

    fun execStartActivity(
            who: Context?, contextThread: IBinder?, token: IBinder?, target: Activity?,
            intent: Intent?, requestCode: Int?, options: Bundle?): Instrumentation.ActivityResult {

        Log.i(TAG, "Evil is executing....")
//        Log.d(TAG, "\nstartActivity, param: \n" + "who = [" + who + "], " +
//                "\ncontextThread = [" + contextThread + "], \ntoken = [" + token + "], " +
//                "\ntarget = [" + target + "], \nintent = [" + intent +
//                "], \nrequestCode = [" + requestCode + "], \noptions = [" + options + "]")

        try {
            val execStartActivity = Instrumentation::class.java.getDeclaredMethod(
                    "execStartActivity",
                    Context::class.java, IBinder::class.java, IBinder::class.java, Activity::class.java,
                    Intent::class.java, Int::class.javaPrimitiveType, Bundle::class.java)
            execStartActivity.isAccessible = true
            return execStartActivity.invoke(mBase, who,
                    contextThread, token, target, intent, requestCode, options) as Instrumentation.ActivityResult
        } catch (e: Exception) {
            throw RuntimeException("do not support!!! pls adapt it")
        }

    }

    companion object {
        private val TAG = "EvilInstrumentation"
    }
}
