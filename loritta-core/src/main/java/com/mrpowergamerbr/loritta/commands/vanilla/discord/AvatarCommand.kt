package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import java.util.*

class AvatarCommand : AbstractCommand("avatar", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["AVATAR_DESCRIPTION"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
		}
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("@Loritta")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var getAvatar = context.getUserAt(0)

		if (getAvatar == null) {
			getAvatar = context.userHandle
		}

		val embed = EmbedBuilder()
		embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
		embed.setDescription("**${context.legacyLocale["AVATAR_CLICKHERE", getAvatar.effectiveAvatarUrl + "?size=2048"]}**")

		if (getAvatar.id == "390927821997998081")
			embed.appendDescription("\n*${context.legacyLocale["AVATAR_PantufaCute"]}* \uD83D\uDE0A")

		if (getAvatar.id == loritta.discordConfig.discord.clientId) {
			val calendar = Calendar.getInstance()
			val currentDay = calendar.get(Calendar.DAY_OF_WEEK)

			embed.appendDescription("\n*${context.legacyLocale["AVATAR_LORITTACUTE"]}* \uD83D\uDE0A")
			if (loritta.discordConfig.discord.fanArtExtravaganza.enabled && currentDay == loritta.discordConfig.discord.fanArtExtravaganza.dayOfTheWeek) {
				val currentFanArtInMasterCluster = lorittaShards.queryMasterLorittaCluster("/api/v1/loritta/current-fan-art-avatar").await().obj

				val artistId = currentFanArtInMasterCluster["artistId"].nullString
				val fancyName = currentFanArtInMasterCluster["fancyName"].nullString

				if (artistId != null) {
					val user = lorittaShards.retrieveUserById(artistId)

					val displayName = fancyName ?: user?.name

					embed.appendDescription("\n\n**" + locale.toNewLocale()["commands.miscellaneous.fanArts.madeBy", displayName] + "**")
					// TODO: Readicionar redes sociais depois
					/* val artist = loritta.fanArtArtists.firstOrNull {
						it.socialNetworks
								?.firstIsInstanceOrNull<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
					}?.id

					if (artist != null) {
						for (socialNetwork in artist.socialNetworks) {
							var root = socialNetwork.display
							if (socialNetwork.link != null) {
								root = "[$root](${socialNetwork.link})"
							}
							embed.appendDescription("\n**${socialNetwork.socialNetwork.fancyName}:** $root")
						}
					} */
				}
			}
		}

		embed.setTitle("\uD83D\uDDBC ${getAvatar.name}")
		embed.setImage(getAvatar.effectiveAvatarUrl + "?size=2048")
		context.sendMessage(context.getAsMention(true), embed.build())
	}
}