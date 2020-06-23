package net.perfectdreams.loritta.parallax.compiler

import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files

class KotlinCompiler {
    fun kotlinc(source: String, outputDir: File) {
        val logger = LoggerFactory.getLogger("KotlinCompiler")
        val packageName = Regex("""package[ ]+([\w._\p{S}]+)""").find(source)?.groupValues?.get(1) ?: ""
        val className = Regex("""class[ ]+(\S*)""").find(source)?.groupValues?.get(1) ?: ""
        var canonicalName = ""
        if (packageName.isNotEmpty()) {
            canonicalName += "$packageName."
        }
        canonicalName += className

        val kotlinDynamicCompiler = KotlinDynamicCompiler()

        val key = "CUSTOM_COMPILED_CODE"

        val ktFile = File(outputDir, "$key.kt")
        ktFile.writeText(source)
        kotlinDynamicCompiler.compileModule("", listOf(ktFile.absolutePath), outputDir, Thread.currentThread().contextClassLoader)
    }

}