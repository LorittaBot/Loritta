package net.perfectdreams.loritta.parallax.executor

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.content
import kotlinx.serialization.json.long
import net.perfectdreams.loritta.parallax.api.ParallaxContext
import net.perfectdreams.loritta.parallax.api.ParallaxGuild
import net.perfectdreams.loritta.parallax.api.ParallaxMessageChannel
import net.perfectdreams.loritta.parallax.api.ParallaxRole

object ParallaxCodeExecutor {
    @JvmStatic
    fun main(args: Array<String>) {
        // We are going to load the class sent via the "-Dparallax.executeClazz" system property
        val clazz = Class.forName(System.getProperty("parallax.executeClazz"))

        // Now we are going to read the command information
        val input = readLine()!!
        val payload = Json.parseJson(input).jsonObject
        val messageArgs = payload["args"]!!.jsonArray.map { it.content }
        println("Message arguments: $messageArgs")

        val channelId = payload["message"]!!.jsonObject["textChannelId"]!!.long
        val guild = Json.parse(ParallaxGuild.serializer(), payload["guild"].toString())
        guild.members.forEach { it.guild = guild }

        val parallaxContext = ParallaxContext(guild, ParallaxMessageChannel(channelId), messageArgs)

        // Now we will set our SecurityManager to block any malicious code
        val securityManager = ParallaxSecurityManager()
        System.setSecurityManager(securityManager)

        // Then we get the main method...
        val method = clazz.getMethod("main", ParallaxContext::class.java)
        // And invoke it!!
        method.invoke(null, parallaxContext)
    }
}