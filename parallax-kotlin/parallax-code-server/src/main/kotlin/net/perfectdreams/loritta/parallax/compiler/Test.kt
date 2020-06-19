package net.perfectdreams.loritta.parallax.compiler

import kotlin.reflect.jvm.jvmName

fun main() {
    val compiler = KotlinCompiler()
    val result = compiler.kotlinc(
            """
                object Test {
                    init {
                        println("Init called")
                    }
                    
                    @JvmStatic
                    fun main() {
                        println(1 + 1)
                        println("Hello world!")
                        
                        this::class.java.getDeclaredMethod("cantAccess").invoke(null)
                    }
                }
            """.trimIndent()
    )

    /* result!!

    result?.methods?.forEach {
        println(it)
    }

    println("NAMES")
    println(result::class.simpleName)
    println(result::class.qualifiedName)
    println(result::class.jvmName)

    val method = result?.getMethod("main")
    val securityManager = MySecurityManager()
    System.setSecurityManager(securityManager)
    method?.invoke(null) */
}

class MySecurityManager : SecurityManager() {
    private val whitelistedPackages = setOf(
            "java.io",
            "java.lang"
    )

    override fun checkPackageAccess(pkg: String) {
        if (pkg !in whitelistedPackages) {
            try {
                throw RuntimeException("Blacklisted package: $pkg")
            } catch (e: RuntimeException) {
                val cantAccess = e.stackTrace.any { it.fileName == "CUSTOM_COMPILED_CODE.kt" }

                if (cantAccess)
                    throw e
            }
        }
    }
}