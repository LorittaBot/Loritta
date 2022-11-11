package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.DiscordAttachment
import dev.kord.common.entity.optional.value
import net.perfectdreams.loritta.deviousfun.DeviousShard

class Attachment(val deviousShard: DeviousShard, val attachment: DiscordAttachment) {
    val url: String
        get() = attachment.url

    // TODO: Check by Content-Type too
    val isImage: Boolean
        get() = (attachment.width.value ?: -1) >= 0
}