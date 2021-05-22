package net.perfectdreams.loritta.plugin.stafflorittaban.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.perfectdreams.loritta.plugin.stafflorittaban.StaffLorittaBanConfig
import net.perfectdreams.loritta.tables.BannedUsers

class AddReactionForStaffLoriBanModule(val config: StaffLorittaBanConfig) : MessageReceivedModule {
	override suspend fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		return config.enabled && event.channel.idLong in config.channels
	}

	override suspend fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile?, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val message = event.message

		val split = message.contentRaw.split(" ")
		val isUnban = split[0] == "unban"
		val toBeBannedUserId = split[1]
		val args = split.toMutableList()
				.apply { this.removeAt(0) }
				.apply { this.removeAt(0) }

		val reason = args.joinToString(" ")

		val profile = loritta.getLorittaProfile(toBeBannedUserId) ?: return false
		val bannedState = profile.getBannedState()

		if (!isUnban) {
			if (bannedState != null) {
				event.channel.sendMessage("O usuário $toBeBannedUserId já está banido, bobinho! Ele foi banido pelo motivo `${bannedState[BannedUsers.reason]}`")
						.queue()
				return false
			}
		} else {
			if (bannedState == null) {
				event.channel.sendMessage("O usuário $toBeBannedUserId não está banido, bobão!")
						.queue()
				return false
			}
		}

		if (!isUnban) {
			if (reason.isBlank()) {
				event.channel.sendMessage("Você esqueceu de colocar o motivo! Coloque um motivo top top para o ban! ╰(\\*°▽°\\*)╯")
						.queue()
				return false
			}

			if (reason.startsWith("http://") || reason.startsWith("https://")) {
				event.channel.sendMessage("Coloque um motivo mais \"wow\", sabe? Sempre é bom explicar o motivo da pessoa ter sido banida (Como `Provocar a equipe de suporte da Loritta`) em vez de colocar apenas uma imagem, provas são boas mas não devemos apenas depender que a pessoa descubra o motivo do ban apenas baseando em uma imagem ^-^")
						.queue()
				return false
			}
		}

		event.message.addReaction("sad_cat_thumbs_up:686370257308483612").queue()
		return false
	}
}