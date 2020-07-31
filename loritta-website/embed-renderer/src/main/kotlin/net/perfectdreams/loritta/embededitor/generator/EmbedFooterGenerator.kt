package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.*
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordEmbed
import net.perfectdreams.loritta.embededitor.utils.*

object EmbedFooterGenerator : GeneratorBase {
    fun generate(m: EmbedRenderer, content: FlowContent, embed: DiscordEmbed, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        val footer = embed.footer
        if (footer != null) {
            content.div(classes = "embedFooter-3yVop- embedMargin-UO5XwE") {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_FOOTER_NOT_NULL, null)
                if (footer.iconUrl != null) {
                    img(src = m.parsePlaceholders(footer.iconUrl), classes = "embedFooterIcon-239O1f")
                }

                span(classes = "embedFooterText-28V_Wb") {
                    m.parseAndAppendDiscordText(this, footer.text, false, false)
                }
            }
        } else {
            content.div {
                modifyTagCallback?.invoke(this, this, MessageTagSection.EMBED_FOOTER_NULL, null)
            }
        }
    }
}