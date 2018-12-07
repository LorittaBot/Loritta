package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.threads.UpdateStatusThread
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.core.EmbedBuilder
import java.util.*

class AvatarCommand : AbstractCommand("avatar", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["AVATAR_DESCRIPTION"]
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
		}
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("@Loritta")
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var getAvatar = context.getUserAt(0)

		if (getAvatar == null) {
			getAvatar = context.userHandle
		}

		val embed = EmbedBuilder()
		embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
		embed.setDescription("**${context.locale["AVATAR_CLICKHERE", getAvatar.effectiveAvatarUrl + "?size=2048"]}**")

		if (getAvatar.id == "390927821997998081")
			embed.appendDescription("\n*${context.locale["AVATAR_PantufaCute"]}* \uD83D\uDE0A")

		if (getAvatar.id == Loritta.config.clientId) {
			val calendar = Calendar.getInstance()
			val currentDay = calendar.get(Calendar.DAY_OF_WEEK)

			embed.appendDescription("\n*${context.locale["AVATAR_LORITTACUTE"]}* \uD83D\uDE0A")
			if (Loritta.config.fanArtExtravaganza && currentDay == Calendar.SUNDAY) {
				val fanArt = UpdateStatusThread.currentFanArt

				val user = lorittaShards.retrieveUserById(fanArt.artistId)

				val displayName = fanArt.fancyName ?: user?.name

				embed.appendDescription("\n\n**" + locale.format(displayName) { commands.miscellaneous.fanArts.madeBy } + "**")
				val artist = loritta.fanArtConfig.artists[fanArt.artistId]
				if (artist != null) {
					for (socialNetwork in artist.socialNetworks) {
						var root = socialNetwork.display
						if (socialNetwork.link != null) {
							root = "[$root](${socialNetwork.link})"
						}
						embed.appendDescription("\n**${socialNetwork.socialNetwork.fancyName}:** $root")
					}
				}
			}
		}

		embed.setTitle("\uD83D\uDDBC ${getAvatar.name}")
		embed.setImage(getAvatar.effectiveAvatarUrl + if (!getAvatar.effectiveAvatarUrl.endsWith(".gif")) "?size=2048" else "")
		context.sendMessage(context.getAsMention(true), embed.build())
	}
}