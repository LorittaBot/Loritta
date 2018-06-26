package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.MessageBuilder
import org.apache.commons.io.IOUtils
import java.net.HttpURLConnection
import java.net.URL

class AminoConverterModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return serverConfig.aminoConfig.fixAminoImages
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		event.textChannel!!
		event.member!!

		for (attachments in event.message.attachments) {
			if (attachments.fileName.endsWith(".Amino") || attachments.fileName == "Amino") {
				val imageUrl = URL(attachments.url)
				val connection = imageUrl.openConnection() as HttpURLConnection
				connection.setRequestProperty("User-Agent",
						Constants.USER_AGENT)

				val byteArray = IOUtils.toByteArray(connection.inputStream)
				// Nós não conseguimos detectar se a imagem é uma GIF a partir do content type...
				val isGif = byteArray.let {
					val byte0 = it[0].toInt()
					val byte1 = it[1].toInt()
					val byte2 = it[2].toInt()
					val byte3 = it[3].toInt()
					val byte4 = it[4].toInt()
					val byte5 = it[5].toInt()

					byte0 == 0x47 && byte1 == 0x49 && byte2 == 0x46 && byte3 == 0x38 && byte4 == 0x39 && byte5 == 0x61 // GIF89a
				}

				val extension = if (isGif) "gif" else "png"

				event.textChannel.sendFile(byteArray.inputStream(), "amino.$extension", MessageBuilder().append("(Por " + event.member.asMention + ") **Link para o \".Amino\":** " + attachments.url).build()).complete()
				event.message.delete().complete()
			}
		}

		return false
	}
}