package com.mrpowergamerbr.loritta.utils.modules

import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

object AminoConverterModule {
	fun convertToImage(event: MessageReceivedEvent) {
		for (attachments in event.message.attachments) {
			if (attachments.fileName.endsWith(".Amino") || attachments.fileName == "Amino") {
				val bufferedImage = LorittaUtils.downloadImage(attachments.url)

				val os = ByteArrayOutputStream()
				ImageIO.write(bufferedImage, "png", os)
				val inputStream = ByteArrayInputStream(os.toByteArray())

				event.textChannel.sendFile(inputStream, "amino.png", MessageBuilder().append("(Por " + event.member.asMention + ") **Link para o \".Amino\":** " + attachments.url).build()).complete()
				event.message.delete().complete()
			}
		}
	}
}