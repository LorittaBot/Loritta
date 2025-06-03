package net.perfectdreams.loritta.embededitor.generator

import kotlinx.html.*
import net.perfectdreams.loritta.embededitor.EmbedRenderer
import net.perfectdreams.loritta.embededitor.data.DiscordMessage
import net.perfectdreams.loritta.embededitor.utils.*

object MessageContentGenerator {
    fun generate(m: EmbedRenderer, content: FlowContent, message: DiscordMessage, modifyTagCallback: MODIFY_TAG_CALLBACK? = null) {
        content.div(classes = "markup-2BOw-j messageContent-2qWWxC") {
            span {
                modifyTagCallback?.invoke(this, this, MessageTagSection.MESSAGE_CONTENT, null)

                m.parseAndAppendDiscordText(this, message.content)
            }
        }
    }
}