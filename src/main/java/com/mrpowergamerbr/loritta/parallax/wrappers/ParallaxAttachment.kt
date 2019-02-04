package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.entities.Message

class ParallaxAttachment(private val attachment: Message.Attachment) {

    val id = attachment.id

    val filename = attachment.fileName
    val filesize = attachment.size

    val url = attachment.url
    val proxyURL = attachment.proxyUrl

    val image = LorittaUtils.downloadImage(attachment.url)

    val height = image?.height
    val width = image?.width
}