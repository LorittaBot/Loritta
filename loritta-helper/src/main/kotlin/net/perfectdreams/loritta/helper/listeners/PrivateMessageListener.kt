package net.perfectdreams.loritta.helper.listeners

import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.helper.LorittaHelper

class PrivateMessageListener(val m: LorittaHelper) : ListenerAdapter() {
    companion object {
        val VALID_APPEAL_TEXTS = listOf(
            "apelo",
            "appeal"
        )
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromType(ChannelType.PRIVATE))
            return

        if (VALID_APPEAL_TEXTS.any { event.message.contentRaw.equals(it, true) }) {
            event.channel.sendMessage(
                MessageCreate {
                    content = """**Então... você está afim de fazer um apelo de ban na Loritta? Então você veio ao lugar certo! <:lorota_jubinha:500766283965661184>**""".trimMargin()

                    actionRow(
                        Button.of(
                            ButtonStyle.LINK,
                            "https://appeals.loritta.website/br/?utm_source=discord&utm_medium=button&utm_campaign=unban-appeal&utm_content=appeal-old-helper-invoke"
                            "Enviar um Apelo de Ban"
                        ).withEmoji(Emoji.fromCustom("lori_angel", 964701052324675622L, false))
                    )
                }
            ).queue()
        }
    }
}