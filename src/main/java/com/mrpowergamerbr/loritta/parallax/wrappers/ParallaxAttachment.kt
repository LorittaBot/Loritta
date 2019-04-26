package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.api.entities.Message

class ParallaxAttachment(private val attachment: Message.Attachment) {

    val id = attachment.id

    val filename = attachment.fileName
    val filesize = attachment.size

    val url = attachment.url
    val proxyURL = attachment.proxyUrl

    val image = if (attachment.isImage)
        ParallaxImage(LorittaUtils.downloadImage(attachment.url)!!)
    else
        null

    val height = image?.image?.height
    val width = image?.image?.width
}