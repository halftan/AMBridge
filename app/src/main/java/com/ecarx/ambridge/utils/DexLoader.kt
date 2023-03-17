package com.ecarx.ambridge.utils

import android.content.Context
import dalvik.system.DexClassLoader
import java.io.File
import java.io.InputStream

class DexLoader(mCtx: Context) {

    fun getLoader(inStr: InputStream): DexClassLoader {
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
}