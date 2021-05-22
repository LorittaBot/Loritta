package net.perfectdreams.loritta.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.utils.Emotes

class AddReactionForHeathecliffModule : MessageReceivedModule {
	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return event.channel.idLong == 643828343325851648L || event.channel.idLong == 646871435465326592L
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		if (event.channel.idLong == 643828343325851648L) {
			if (!event.message.contentRaw.startsWith(">")) {
				event.message.addReaction("\uD83D\uDC4D").queue()
				event.message.addReaction("stonks:643608960720699464").queue()
				event.message.addReaction("baka:473905338220019732").queue()
				event.message.addReaction("❤").queue()
			}
		}

		if (event.channel.idLong == 646871435465326592L) {
			if (25 >= event.message.contentRaw.length) {
				event.message.delete().queue()
				val channel = event.author.openPrivateChannel().await()

				channel.sendMessage("Olha... eu duvido que você conseguiu responder todas as <#647909086326816799> com tão poucas palavras. Que tal responder elas de uma forma decente? ${Emotes.LORI_SHRUG}").await()
				return false
			}

			event.message.addReaction("gato_joinha:593161404937404416").queue()

			val chatStaff = event.guild?.getTextChannelById(643604594114691122L)

			chatStaff?.sendMessage("<@&320608529398497280> <@&300279961686638603> Um formulário foi preenchido por ${event.author.asMention}! ${Emotes.LORI_PAT} ${event.message.jumpUrl}")
					?.queue()
		}

		return false
	}
}