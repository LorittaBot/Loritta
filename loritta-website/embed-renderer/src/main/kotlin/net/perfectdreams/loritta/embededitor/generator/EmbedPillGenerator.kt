package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.*
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.utils.MessageTagSection

object EmbedPillGenerator {
    fun generate(m: EmbedRenderer, content: FlowContent, embed: DiscordEmbed, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        content.div(classes = "pill") {
            if (embed.color != null) {
                val aux = ("000000" + ((embed.color) ushr 0).toString(16))
                val hex = "#" + aux.slice(aux.length - 6 until aux.length)
                style = "background-color: $hex;"
            }

            modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_PILL, null)
        }
    }
}