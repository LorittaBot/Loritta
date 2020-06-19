package net.perfectdreams.loritta.parallax.compiler

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.JvmTarget
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.script.experimental.jvm.util.KotlinJars
import kotlin.script.experimental.jvm.util.classpathFromClassloader

class KotlinDynamicCompiler {
    fun compileModule(moduleName: String,
                      sourcePath: List<String>,
                      saveClassesDir: File,
                      classLoader: ClassLoader? = null,
                      forcedAddKotlinStd: Boolean = true

    ): GenerationState {
        val logger = LoggerFactory.getLogger("KotlinDynamicCompiler")
        val stubDisposable = StubDisposable()
        val configuration = CompilerConfiguration()
        configuration.put(CommonConfigurationKeys.MODULE_NAME, moduleName)
        val baos = ByteArrayOutputStream()
        val ps: PrintStream = PrintStream(baos)
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, PrintingMessageCollector(ps, MessageRenderer.PLAIN_FULL_PATHS, true))
        configuration.put(JVMConfigurationKeys.OUTPUT_DIRECTORY, saveClassesDir)
        //configuration.put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)
        configuration.put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.JVM_1_8)
        val classPath = mutableSetOf<File>()
        if (classLoader != null) {
            classPath.addAll(classpathFromClassloader(classLoader)!!)
        }
        if (forcedAddKotlinStd) {
            classPath.add(KotlinJars.stdlib)
        }
        /* JAR_CLASS_PATH.forEach {
            classPath.add(it)
        } */
        configuration.addJvmClasspathRoots(classPath.toList())
        configuration.addKotlinSourceRoots(sourcePath)
        val env = KotlinCoreEnvironment.createForProduction(stubDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
        val result = KotlinToJVMBytecodeCompiler.analyzeAndGenerate(env)
        ps.flush()
        if (result != null) {
            return result
        } else {
            logger.info("kotlin编译异常")
            throw IllegalStateException("Compilation error. Details:\n${baos.toByteArray().toString(Charsets.UTF_8)}")

        }

    }

    inner class StubDisposable : Disposable {
        @Volatile
        var isDisposed: Boolean = false
            private set

        override fun dispose() {
            isDisposed = true
        }
    }
}