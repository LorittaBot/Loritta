package net.perfectdreams.loritta.website

import com.fasterxml.jackson.module.kotlin.readValue
import net.perfectdreams.loritta.utils.Constants
import net.perfectdreams.loritta.website.utils.config.WebsiteConfig
import java.io.File
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarFile

object LorittaWebsiteLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val path = this::class.java.protectionDomain.codeSource.location.path
            val jar = JarFile(path)
            val mf = jar.manifest
            val mattr = mf.mainAttributes
            // Yes, you SHOULD USE Attributes.Name.CLASS_PATH! Don't try using "Class-Path", it won't work!
            val manifestClassPath = mattr[Attributes.Name.CLASS_PATH] as String

            // The format within the Class-Path attribute is different than the one expected by the property, so let's fix it!
            // By the way, don't forget to append your original JAR at the end of the string!
            val clazz = LorittaWebsiteLauncher::class.java
            val protectionDomain = clazz.protectionDomain
            val propClassPath = manifestClassPath.replace(
                " ",
                ":"
            ) + ":${Paths.get(protectionDomain.codeSource.location.toURI()).fileName}"

            // Now we set it to our own classpath
            System.setProperty("kotlin.script.classpath", propClassPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val configPath = System.getProperty("config.file") ?: "./website.conf"
        val websiteConfig = Constants.HOCON_MAPPER.readValue<WebsiteConfig>(File(configPath))

        val website = LorittaWebsite(websiteConfig)
        website.start()
    }
}