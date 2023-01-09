package net.perfectdreams.loritta.morenitta.interactions

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.createStyledContent
import net.perfectdreams.loritta.common.emotes.Emote

class ChunkedMessageBuilder {
    var content = ""

    fun styled(content: String, prefix: String) {
        this.content += (createStyledContent(content, prefix) + "\n")
    }

    fun styled(content: String, prefix: Emote) {
        this.content += (createStyledContent(content, prefix.asMention) + "\n")
    }
}