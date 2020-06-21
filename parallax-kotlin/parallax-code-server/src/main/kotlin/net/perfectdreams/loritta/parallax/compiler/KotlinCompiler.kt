package net.perfectdreams.loritta.parallax.compiler

import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files

class KotlinCompiler {
    fun kotlinc(source: String) /* : Class<*>? */ {
        val logger = LoggerFactory.getLogger("KotlinCompiler")
        val packageName = Regex("""package[ ]+([\w._\p{S}]+)""").find(source)?.groupValues?.get(1) ?: ""
        val className = Regex("""class[ ]+(\S*)""").find(source)?.groupValues?.get(1) ?: ""
        var canonicalName = ""
        if (packageName.isNotEmpty()) {
            canonicalName += "$packageName."
        }
        canonicalName += className

        val kotlinDynamicCompiler = KotlinDynamicCompiler()

        try {
            val outputDir = File("/home/parallax/compiled/")
            val key = "CUSTOM_COMPILED_CODE"
            outputDir.absolutePath
            val ktFile = File(outputDir, "$key.kt")
            ktFile.writeText(source)
            kotlinDynamicCompiler.compileModule("", listOf(ktFile.absolutePath), outputDir, Thread.currentThread().contextClassLoader)
            val uri = arrayOf(outputDir.toURI().toURL())
            val classLoader = URLClassLoader.newInstance(uri)
            println("Canonical: ${canonicalName}")
            // return classLoader.loadClass("CustomCommand")
        } catch (e: Exception) {
            logger.error("Kotlin编译失败,source={}", source, e)
            e.printStackTrace()
        }
        // return null
    }

}