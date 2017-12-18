package com.wilson.zhou.apkloader

import android.app.Activity
import android.app.Application
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
//            val loader = PathClassLoader(Environment.getExternalStorageDirectory().absolutePath + File.separator+"app-debug.apk",ClassLoader.getSystemClassLoader())
            val clazz = Class.forName("com.wilson.assisapi.MainActivity")
            val intent = Intent(this, clazz)
            startActivity(intent)
            Toast.makeText(this, "load is clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
