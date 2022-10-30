package net.perfectdreams.loritta.morenitta.parallax

import net.perfectdreams.loritta.morenitta.utils.substringIfNeeded
import mu.KotlinLogging
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.deviousfun.MessageBuilder
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.deviousfun.entities.Message
import org.apache.commons.lang3.exception.ExceptionUtils
import java.awt.Color
import java.util.concurrent.ExecutionException

object ParallaxUtils {
    private val logger = KotlinLogging.logger {}

    /**
     * Sends the [throwable] to a [channel] inside a [net.dv8tion.jda.api.entities.DeviousEmbed]
     *
     * @return the sent throwable
     */
    suspend fun sendThrowableToChannel(throwable: Throwable, channel: Channel, message: String? = null): Message {
        logger.warn(throwable) { "Error while evaluating code" }

        val messageBuilder = MessageBuilder()
        messageBuilder.append(message ?: " ")

        val cause = throwable.cause

        val embedBuilder = EmbedBuilder()
        embedBuilder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU")

        val description = when (throwable) {
            // Thread.stop (deprecated)
            is ExecutionException -> "A thread que executava este comando agora está nos céus... *+angel* (Provavelmente seu script atingiu o limite máximo de memória utilizada!)"
            else -> {
                val stringBuilder = StringBuilder()

                if (cause?.message != null)
                    stringBuilder.append("${cause.message}\n")

                stringBuilder.append(ExceptionUtils.getStackTrace(throwable))

                stringBuilder.toString().substringIfNeeded(0 until 2000)
            }
        }

        embedBuilder.setDescription("```$description```")
        embedBuilder.setFooter(
            "Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢",
            null
        )
        embedBuilder.setColor(Color.RED)

        messageBuilder.setEmbed(embedBuilder.build())

        return channel.sendMessage(messageBuilder.build())
    }
}