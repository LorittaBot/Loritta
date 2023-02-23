package net.perfectdreams.loritta.morenitta.commands.vanilla.economy

import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordMessage

class EmojiFightEmojiCommand(val m: LorittaBot) : DiscordAbstractCommandBase(
	m,
	listOf("emojifight emoji", "rinhadeemoji emoji", "emotefight emoji"),
	net.perfectdreams.loritta.common.commands.CommandCategory.ECONOMY
) {
	override fun command() = create {
		localizedDescription("commands.command.emojifightbet.description")
		localizedExamples("commands.command.emojifightbet.examples")

		usage {
			arguments {
				argument(ArgumentType.TEXT) {}
			}
		}

		this.similarCommands = listOf("EmojiFightCommand")
		this.canUseInPrivateChannel = false

		executesDiscord {
			val canUseCustomEmojis = loritta.newSuspendedTransaction {
				UserPremiumPlans.getPlanFromValue(loritta._getActiveMoneyFromDonations(user.idLong)).customEmojisInEmojiFight
			}

			if (!canUseCustomEmojis) {
				reply("Apenas usuários com plano premium \"Recomendado\" ou superior podem colocar emojis personalizados no emoji fight!")
				return@executesDiscord
			}

			if (this.args.isEmpty()) {
				loritta.newSuspendedTransaction {
					loritta.getOrCreateLorittaProfile(this@executesDiscord.user.idLong)
						.settings
						.emojiFightEmoji = null
				}

				reply("Emoji personalizado removido!")
				return@executesDiscord
			}

			val discordEmoji = (this.message as DiscordMessage).handle.mentions.customEmojis.firstOrNull()

			val newEmoji = if (discordEmoji != null) {
				discordEmoji.asMention
			} else {
				val match = loritta.unicodeEmojiManager.regex.find(this.args[0])

				if (match == null) {
					reply("Não encontrei nenhum emoji na sua mensagem...")
					return@executesDiscord
				}

				match.value
			}

			loritta.newSuspendedTransaction {
				loritta.getOrCreateLorittaProfile(this@executesDiscord.user.idLong)
					.settings
					.emojiFightEmoji = newEmoji
			}

			if (discordEmoji == null)
				reply("Emoji alterado! Nas próximas rinhas de emoji, o $newEmoji irá te acompanhar nas suas incríveis batalhas cativantes.")
			else
				reply(
					LorittaReply(
						"Emoji alterado! Nas próximas rinhas de emoji, o $newEmoji irá te acompanhar nas suas incríveis batalhas cativantes.",
					),
					LorittaReply(
						"Lembre-se que eu preciso estar no servidor onde o emoji está para eu conseguir usar o emoji!",
						mentionUser = false
					),
					LorittaReply(
						"Observação: Você será banido de usar a Loritta caso você coloque emojis sugestivos ou NSFW. Tenha bom senso e não atrapalhe os servidores dos outros com bobagens!",
						mentionUser = false
					)
				)
		}
	}
}