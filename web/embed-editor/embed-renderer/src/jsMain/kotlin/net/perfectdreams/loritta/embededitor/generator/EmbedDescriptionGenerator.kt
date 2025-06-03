package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.FlowContent
import kotlinx.html.div
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.utils.MessageTagSection

object EmbedDescriptionGenerator : GeneratorBase {
    fun generate(m: EmbedRenderer, content: FlowContent, embed: DiscordEmbed, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        val description = embed.description
        if (description != null) {
            content.div(classes = "embedDescription-1Cuq9a embedMargin-UO5XwE") {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_DESCRIPTION_NOT_NULL, null)

                m.parseAndAppendDiscordText(this, description)
            }
        } else {
            content.div {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_DESCRIPTION_NULL, null)
            }
        }
    }
}