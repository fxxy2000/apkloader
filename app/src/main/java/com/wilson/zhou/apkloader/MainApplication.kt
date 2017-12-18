package com.wilson.zhou.apkloader

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexClassLoader
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Array

/**
 * Created by wilso on 12/17/2017.
 */

class MainApplication : Application() {
    private val APK = "app-debug.apk"

    override fun attachBaseContext(base: Context) {
        load(base, APK)
        addRes(base, File(base.getDir(workDirectory, Context.MODE_PRIVATE), APK).absolutePath)
//        base.setTheme(0x7f09008d)
//        base.theme
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
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

    private val optimizedDirectory = "optimized"
    private val workDirectory = "working"

    @Throws(Exception::class)
    fun load(context:Context, fileName: String) {
        var optimized = File(optimizedDirectory)
        optimized = context.getDir(optimized.toString(), Context.MODE_PRIVATE)
        optimized = File(optimized, fileName)
        optimized.mkdir()

        var work = context.getDir(workDirectory, Context.MODE_PRIVATE)
        work = File(work, fileName)

        val inputDex = context.assets.open(fileName)
        val outputDex = FileOutputStream(work)
        val buf = ByteArray(0x1000)
        while (true) {
            val r = inputDex.read(buf)
            if (r == -1)
                break
            outputDex.write(buf, 0, r)
        }
        outputDex.close()
        inputDex.close()

        val localClassLoader = MainActivity::class.java.classLoader
        val classLoader = DexClassLoader(work.absolutePath, optimized.absolutePath,null, localClassLoader)
        if (localClassLoader is BaseDexClassLoader) {
            val existing = getDexClassLoaderElements(localClassLoader)
            val incoming = getDexClassLoaderElements(classLoader)
            val joined = joinArrays(incoming, existing)
            setDexClassLoaderElements(localClassLoader, joined)
        } else {
            throw UnsupportedOperationException("Class loader not supported")
        }
    }

    @Throws(Exception::class)
    private fun setDexClassLoaderElements(classLoader: BaseDexClassLoader, elements: Any) {

        val dexClassLoaderClass = BaseDexClassLoader::class.java

        val pathListField = dexClassLoaderClass.getDeclaredField("pathList")

        pathListField.isAccessible = true

        val pathList = pathListField.get(classLoader)

        val dexElementsField = pathList.javaClass.getDeclaredField("dexElements")

        dexElementsField.isAccessible = true

        dexElementsField.set(pathList, elements)

    }


    @Throws(Exception::class)
    private fun getDexClassLoaderElements(classLoader: BaseDexClassLoader): Any {

        val dexClassLoaderClass = BaseDexClassLoader::class.java

        val pathListField = dexClassLoaderClass.getDeclaredField("pathList")

        pathListField.isAccessible = true

        val pathList = pathListField.get(classLoader)

        val dexElementsField = pathList.javaClass.getDeclaredField("dexElements")

        dexElementsField.isAccessible = true

        return dexElementsField.get(pathList)

    }

    private fun joinArrays(o1: Any, o2: Any): Any {
        val o1Type = o1.javaClass.componentType
        val o2Type = o2.javaClass.componentType
        if (o1Type != o2Type) {
            throw IllegalArgumentException()
        }
        val o1Size = Array.getLength(o1)
        val o2Size = Array.getLength(o2)
        val array = Array.newInstance(o1Type, o1Size + o2Size)
        var offset = 0
        var i = 0
        while (i < o1Size) {
            Array.set(array, offset, Array.get(o1, i))
            i++
            offset++
        }

        i = 0
        while (i < o2Size) {
            Array.set(array, offset, Array.get(o2, i))
            i++
            offset++
        }

        return array

    }

}
