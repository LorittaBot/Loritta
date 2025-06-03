package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.*
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.utils.*

object EmbedThumbnailGenerator {
    fun generate(m: EmbedRenderer, content: FlowContent, embed: DiscordEmbed, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        val thumbnailUrl = embed.thumbnail?.url
        if (thumbnailUrl != null) {
            content.a(classes = "anchor-3Z-8Bb anchorUnderlineOnHover-2ESHQB imageWrapper-2p5ogY imageZoom-1n-ADA clickable-3Ya1ho embedThumbnail-2Y84-K") {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_THUMBNAIL_NOT_NULL, null)

                style = "width: 80px;"
                img(src = m.parsePlaceholders(thumbnailUrl)) {
                    style = "width: 100%;"
                }
            }
        } else {
            content.div {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_THUMBNAIL_NULL, null)
            }
        }
    }
}