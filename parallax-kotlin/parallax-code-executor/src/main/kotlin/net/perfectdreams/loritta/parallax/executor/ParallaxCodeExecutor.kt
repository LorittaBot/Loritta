package net.perfectdreams.loritta.parallax.executor

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.content
import kotlinx.serialization.json.long
import kotlinx.serialization.parse
import net.perfectdreams.loritta.api.commands.CommandException
import net.perfectdreams.loritta.parallax.api.*
import net.perfectdreams.loritta.parallax.api.packet.ParallaxAckPacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxConnectionUtils
import net.perfectdreams.loritta.parallax.api.packet.ParallaxSendMessagePacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxThrowablePacket
import java.lang.reflect.InvocationTargetException

object ParallaxCodeExecutor {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            // We are going to load the class sent via the "-Dparallax.executeClazz" system property
            val clazz = Class.forName(System.getProperty("parallax.executeClazz"))

            // Now we are going to read the command information
            val input = readLine()!!
            val payload = Json.parseJson(input).jsonObject
            val messageArgs = payload["args"]!!.jsonArray.map { it.content }
            println("Message arguments: $messageArgs")

            val message = Json.parse(ParallaxMessage.serializer(), payload["message"].toString())
            val guild = Json.parse(ParallaxGuild.serializer(), payload["guild"].toString())
            guild.members.forEach { it.guild = guild }

            val parallaxContext = ParallaxContext(guild, ParallaxMessageChannel(message.textChannelId), message, messageArgs)

            // Now we will set our SecurityManager to block any malicious code
            val securityManager = ParallaxSecurityManager()
            System.setSecurityManager(securityManager)

            // Then we get the main method...
            val method = clazz.getMethod("main", ParallaxContext::class.java)
            // And invoke it!!
            method.invoke(null, parallaxContext)
        } catch (e: Throwable) {
            e.printStackTrace()

            if (e is ParallaxCommandException) {
                ParallaxConnectionUtils.sendPacket<ParallaxAckPacket>(
                        ParallaxSendMessagePacket(
                                e.message ?: "???"
                        )
                )
            } else if (e is InvocationTargetException) {
                e.targetException.printStackTrace()
                ParallaxConnectionUtils.sendPacket<ParallaxAckPacket>(
                        ParallaxThrowablePacket(
                                e.cause?.message ?: e.cause?.toString()
                                        ?: "Error"
                        )
                )
            } else {
                ParallaxConnectionUtils.sendPacket<ParallaxAckPacket>(
                        ParallaxThrowablePacket(
                                e.cause?.message ?: e.message ?: "Error")
                )
            }
        }
    }
}