package net.perfectdreams.loritta.parallax.executor

class ParallaxSecurityManager : SecurityManager() {
    private val whitelistedPackages = setOf(
            "java.lang",
            "java.io",
            "java.util",
            "kotlin.jvm.internal", // Used by intristics (null checks)
            "kotlin.jvm.functions", // ^
            "net.perfectdreams.loritta.parallax.api",
            "net.perfectdreams.loritta.parallax.api.packet",
            "kotlin.text",
            "kotlin",
            "kotlin.collections",
            "kotlin.random"

    )

    override fun checkPackageAccess(pkg: String) {
        // println(pkg)
        if (pkg !in whitelistedPackages) {
            try {
                throw RuntimeException("Blacklisted package: $pkg")
            } catch (e: RuntimeException) {
                var bypassAccess = false

                // e.printStackTrace()
                // Thread.sleep(250)

                for (element in e.stackTrace) {
                    if (element.fileName == "CUSTOM_COMPILED_CODE.kt")
                        break
                    if (element.className.startsWith("net.perfectdreams.loritta.parallax.api.")) {
                        bypassAccess = true
                        break
                    }
                    if (element.className.startsWith("net.perfectdreams.loritta.parallax.executor.") && element.methodName != "checkPackageAccess") {
                        bypassAccess = true
                        break
                    }
                }

                // println("Bypass access? $bypassAccess")
                if (bypassAccess)
                    return

                val cantAccess = e.stackTrace.any { it.fileName == "CUSTOM_COMPILED_CODE.kt" }

                if (cantAccess)
                    throw e
            }
        }
    }
}