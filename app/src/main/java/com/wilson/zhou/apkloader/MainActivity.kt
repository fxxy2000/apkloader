package com.wilson.zhou.apkloader

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.reflect.Array
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexClassLoader
import java.io.FileInputStream
import java.io.FileOutputStream
import android.util.DisplayMetrics
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources


class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_load.setOnClickListener {
            attachContext()
//            val loader = PathClassLoader(Environment.getExternalStorageDirectory().absolutePath + File.separator+"app-debug.apk",ClassLoader.getSystemClassLoader())
//            val clazz = Class.forName("com.wilson.assisapi.MainActivity")
            val intent = Intent(this, TestActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            addRes(this, File(getDir("working", Context.MODE_PRIVATE), "app-debug.apk").absolutePath)
            applicationContext.startActivity(intent)
            Toast.makeText(this, "load is clicked", Toast.LENGTH_SHORT).show()
        }
    }

    fun addRes(context:Context, filePath:String) {
        val assets = AssetManager::class.java.newInstance()
        val addAssetPath = AssetManager::class.java
                .getMethod("addAssetPath", String::class.java)
        if (addAssetPath.invoke(assets, filePath) === Integer.valueOf(0)) {
            throw RuntimeException()
        }

        val resourcesImpl = Class.forName("android.content.res.ResourcesImpl")
        val daj = Class.forName("android.view.DisplayAdjustments")
        val impl = resourcesImpl
                .getConstructor(AssetManager::class.java, DisplayMetrics::class.java,
                        Configuration::class.java, daj)
                .newInstance(assets, context.resources.displayMetrics,
                        context.resources.configuration, daj.newInstance())

        val dynamicResources = Resources::class.java.getConstructor(ClassLoader::class.java)
                .newInstance(MainActivity::class.java.classLoader)
        val setImpl = Resources::class.java.getMethod("setImpl",
                Class.forName("android.content.res.ResourcesImpl"))
        setImpl.invoke(dynamicResources, impl)

        val contextImpl = Class.forName("android.app.ContextImpl")
        val res = contextImpl.getDeclaredField("mResources")
        res.isAccessible = true
        res.set(context, dynamicResources)
    }

    @Throws(Exception::class)
    fun attachContext() {
        // 先获取到当前的ActivityThread对象
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread")
        currentActivityThreadMethod.isAccessible = true
        val currentActivityThread = currentActivityThreadMethod.invoke(null)

        // 拿到原始的 mInstrumentation字段
        val mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation")
        mInstrumentationField.isAccessible = true
        val mInstrumentation = mInstrumentationField.get(currentActivityThread) as Instrumentation

        // 创建代理对象
        val evilInstrumentation = EvilInstrumentation(mInstrumentation)

        // 偷梁换柱
        mInstrumentationField.set(currentActivityThread, evilInstrumentation)
    }
}
