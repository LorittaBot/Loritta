package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.*
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.utils.*

object EmbedAuthorGenerator : GeneratorBase {
    fun generate(m: EmbedRenderer, content: FlowContent, embed: DiscordEmbed, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        val author = embed.author
        if (author != null) {
            content.div(classes = "embedAuthor-3l5luH embedMargin-UO5XwE") {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_AUTHOR_NOT_NULL, null)
                if (author.iconUrl != null) {
                    img(src = m.parsePlaceholders(author.iconUrl), classes = "embedAuthorIcon--1zR3L")
                }

                if (author.url != null) {
                    a(href = m.parsePlaceholders(author.url), classes = "anchor-3Z-8Bb anchorUnderlineOnHover-2ESHQB embedAuthorNameLink-1gVryT embedLink-1G1K1D embedAuthorName-3mnTWj") {
                        m.parseAndAppendDiscordText(this, author.name, false, false)
                    }
                } else {
                    span(classes = "embedAuthorName-3mnTWj") {
                        m.parseAndAppendDiscordText(this, author.name, false, false)
                    }
                }
            }
        } else {
            content.div {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_AUTHOR_NULL, null)
            }
        }
    }
}