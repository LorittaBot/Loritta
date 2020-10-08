package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.*
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.utils.*

object EmbedImageGenerator : GeneratorBase {
    fun generate(m: EmbedRenderer, content: FlowContent, embed: DiscordEmbed, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        val imageUrl = embed.image?.url
        if (imageUrl != null) {
            content.div {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_IMAGE_NOT_NULL, null)
                img(src = m.parsePlaceholders(imageUrl)) {
                    style = "width: 100%;"
                }
            }
        } else {
            content.div {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_IMAGE_NULL, null)
            }
        }
    }
}