package com.ecarx.ambridge.utils

import android.content.Context
import dalvik.system.DexClassLoader
import net.bytebuddy.ByteBuddy
import net.bytebuddy.android.AndroidClassLoadingStrategy
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.matcher.ElementMatchers.*
import java.io.File
import java.io.InputStream
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class DexLib(dexInStream: InputStream, context: Context) {
    val loader: DexClassLoader = init(dexInStream)

    private fun init(inStr: InputStream): DexClassLoader {
        val dexFile = File.createTempFile("ambridge", ".dex")
        inStr.use { input ->
            dexFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return DexClassLoader(
                dexFile.absolutePath,
                null,
                null,
                javaClass.classLoader
        )
    }

    fun getClass(className: String): DexClass {
        return DexClass(className, loader)
    }

    @Suppress("UNCHECKED_CAST")
    class DexClass() {
        lateinit var clazz: Class<Any>

        constructor(className: String, loader: DexClassLoader) : this() {
            this.clazz = loader.loadClass(className) as Class<Any>
        }

        constructor(clazz: Class<Any>) : this() {
            this.clazz = clazz
        }

        fun getField(name: String): Field {
            return clazz.getField(name)
        }

        fun getDeclaredFields(): Array<Field> {
            return clazz.declaredFields
        }

        fun getMethod(name: String, vararg paramTypes: Class<Any>): Method {
            return clazz.getMethod(name, *paramTypes)
        }

        fun newInstance(): Any {
            return clazz.newInstance()
        }

        fun getInnerClass(name: String): DexClass? {
            for (iClass in clazz.declaredClasses) if (iClass.simpleName == name) {
                return DexClass(iClass as Class<Any>)
            }
            return null
        }

        fun extend(context: Context, build: (DynamicType.Builder<Any>) -> DynamicType.Builder<Any>): Any {
            return build(ByteBuddy().subclass(this.clazz))
                .make().load(
                    clazz.classLoader,
                    AndroidClassLoadingStrategy.Wrapping(context.getDir("dexgen", Context.MODE_PRIVATE))
                ).loaded.newInstance()
        }

        fun implementThis(context: Context, build: (DynamicType.Builder<Any>) -> DynamicType.Builder<Any>): Any {
            if (clazz.isInterface) {
                return build(ByteBuddy().subclass(Any::class.java).implement(clazz))
                    .make().load(
                        clazz.classLoader,
                        AndroidClassLoadingStrategy.Wrapping(context.getDir("dexgen", Context.MODE_PRIVATE))
                    ).loaded.newInstance()
            }
            throw RuntimeException("Cannot implement ${clazz.typeName} ${clazz.name}")
        }
    }
}