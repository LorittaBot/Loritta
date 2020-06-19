package net.perfectdreams.loritta.parallax.executor

import net.perfectdreams.loritta.parallax.api.ParallaxContext
import kotlin.concurrent.thread

object ParallaxCodeExecutor {
    @JvmStatic
    fun main(args: Array<String>) {
        // We are going to load the class sent via the "-Dparallax.executeClazz" system property
        val clazz = Class.forName(System.getProperty("parallax.executeClazz"))

        /* clazz.methods.forEach {
            println(it)
        } */

        val parallaxContext = ParallaxContext()
        // Now we will set our SecurityManager to block any malicious code
        val securityManager = ParallaxSecurityManager()
        System.setSecurityManager(securityManager)

        // Then we get the main method...
        val method = clazz.getMethod("main", ParallaxContext::class.java)
        // And invoke it!!
        method.invoke(null, parallaxContext)
    }
}