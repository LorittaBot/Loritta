package net.perfectdreams.loritta.morenitta.modules

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot

class AddReactionForHeathecliffModule(val loritta: LorittaBot) : MessageReceivedModule {
	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return event.channel.idLong == 643828343325851648L || event.channel.idLong == 646871435465326592L
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		if (event.channel.idLong == 643828343325851648L) {
			if (!event.message.contentRaw.startsWith(">")) {
				runCatching { event.message.addReaction("\uD83D\uDC4D") }
				runCatching { event.message.addReaction("stonks:643608960720699464") }
				runCatching { event.message.addReaction("baka:473905338220019732") }
				runCatching { event.message.addReaction("❤") }
			}
		}

		if (event.channel.idLong == 646871435465326592L) {
			if (25 >= event.message.contentRaw.length) {
				runCatching { event.message.delete() }
				val channel = event.author.openPrivateChannel()

				channel.sendMessage("Olha... eu duvido que você conseguiu responder todas as <#647909086326816799> com tão poucas palavras. Que tal responder elas de uma forma decente? ${Emotes.LORI_SHRUG}")
				return false
			}

			runCatching { event.message.addReaction("gato_joinha:593161404937404416") }

			val chatStaff = event.guild?.getTextChannelById(643604594114691122L)

			runCatching {
				chatStaff?.sendMessage("<@&320608529398497280> <@&300279961686638603> Um formulário foi preenchido por ${event.author.asMention}! ${Emotes.LORI_PAT} ${event.message.jumpUrl}")
			}
		}

		return false
	}
}