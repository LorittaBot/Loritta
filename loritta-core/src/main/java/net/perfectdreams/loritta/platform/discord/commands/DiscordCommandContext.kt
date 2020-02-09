package net.perfectdreams.loritta.platform.discord.commands

import com.mrpowergamerbr.loritta.LorittaLauncher
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.discord.entities.DiscordMessage
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser

class DiscordCommandContext(
		args: List<String>,
		val discordMessage: Message
) : CommandContext(args, DiscordMessage(discordMessage)) {
	val isPrivateChannel = discordMessage.channelType == ChannelType.PRIVATE

	override suspend fun user(argument: Int): User? {
		if (this.args.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.args[argument] // Ok, será que isto é uma URL?

			// Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
			for (user in this.message.mentionedUsers) {
				if (user.asMention == link.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
					// Diferente de null? Então vamos usar o avatar do usuário!
					return user
				}
			}

			// Ok, então só pode ser um ID do Discord!
			try {
				val user = LorittaLauncher.loritta.lorittaShards.retrieveUserById(link)

				if (user != null) // Pelo visto é!
					return JDAUser(user)
			} catch (e: Exception) {
			}
		}
		return null
	}
}