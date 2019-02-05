package com.mrpowergamerbr.loritta.parallax.wrappers

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageChannel
import org.apache.commons.lang3.exception.ExceptionUtils
import java.awt.Color

abstract class ParallaxPromise<T> {
    companion object {
        val DEFAULT_CHANNEL_FAILURE_CALLBACK: (MessageChannel, Throwable) -> (Unit) = { channel, e ->
            val builder = EmbedBuilder()
            builder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU")
            val description: String

            if (e.cause != null && (e.cause as Throwable).message != null) {
                description = (e.cause as Throwable).message!!.trim { it <= ' ' }
            } else {
                description = ExceptionUtils.getStackTrace(e).substring(0, Math.min(2000, ExceptionUtils.getStackTrace(e).length))
            }

            builder.setDescription("```$description```")
            builder.setFooter(
                    "Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null)
            builder.setColor(Color.RED)
            channel.sendMessage(builder.build()).queue()
        }
    }

    fun queue() {
        queue(null)
    }

    fun queue(success: java.util.function.Function<T, Any?>?) {
        queue(success, null)
    }

    abstract fun queue(success: java.util.function.Function<T, Any?>?, failure: java.util.function.Function<Any?, Any?>?)
}