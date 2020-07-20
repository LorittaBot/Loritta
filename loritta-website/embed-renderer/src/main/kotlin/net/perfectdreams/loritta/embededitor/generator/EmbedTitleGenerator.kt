package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.*
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.utils.*

object EmbedTitleGenerator : GeneratorBase {
    fun generate(m: EmbedRenderer, content: FlowContent, embed: DiscordEmbed, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        val title = embed.title
        if (title != null) {
            content.div(classes = "embedTitle-3OXDkz embedMargin-UO5XwE") {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_TITLE_NOT_NULL, null)

                m.parseAndAppendDiscordText(this, title)
            }
        } else {
            content.div {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_TITLE_NULL, null)
            }
        }
    }
}