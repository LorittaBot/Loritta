package net.perfectdreams.loritta.commands.vanilla.discord

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.Emotes
import java.util.*

class AvatarCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("avatar"), CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.discord.avatar"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		usage {
			argument(ArgumentType.USER) {}
		}

		examples {
			+ "@Loritta"
		}

		executesDiscord {
			val context = this

			var getAvatar = context.user(0)?.handle

			if (getAvatar == null) {
				getAvatar = context.user
			}

			val embed = EmbedBuilder()
			embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
			embed.setDescription("**${context.locale["$LOCALE_PREFIX.clickHere", "${getAvatar.effectiveAvatarUrl}?size=2048"]}**")

			// Easter Egg: Pantufa
			if (getAvatar.idLong == 390927821997998081L)
				embed.appendDescription("\n*${context.locale["$LOCALE_PREFIX.pantufaCute"]}* ${Emotes.LORI_TEMMIE}")

			// Easter Egg: Gabriela
			if (getAvatar.idLong == 481901252007952385L)
				embed.appendDescription("\n*${context.locale["$LOCALE_PREFIX.gabrielaCute"]}* ${Emotes.LORI_PAT}")

			// Easter Egg: Pollux
			if (getAvatar.idLong == 271394014358405121L || getAvatar.idLong == 354285599588483082L || getAvatar.idLong == 578913818961248256L)
				embed.appendDescription("\n*${context.locale["$LOCALE_PREFIX.polluxCute"]}* ${Emotes.LORI_HEART}")

			// Easter Egg: Loritta
			if (getAvatar.id == com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.clientId) {
				val calendar = Calendar.getInstance()
				val currentDay = calendar.get(Calendar.DAY_OF_WEEK)

				embed.appendDescription("\n*${context.locale["$LOCALE_PREFIX.lorittaCute"]}* ${Emotes.LORI_SMILE}")
				if (com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.fanArtExtravaganza.enabled && currentDay == com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.fanArtExtravaganza.dayOfTheWeek) {
					val currentFanArtInMasterCluster = lorittaShards.queryMasterLorittaCluster("/api/v1/loritta/current-fan-art-avatar").await().obj

					val artistId = currentFanArtInMasterCluster["artistId"].nullString
					val fancyName = currentFanArtInMasterCluster["fancyName"].nullString

					if (artistId != null) {
						val user = lorittaShards.retrieveUserInfoById(artistId.toLong())

						val displayName = fancyName ?: user?.name

						embed.appendDescription("\n\n**" + locale["commands.miscellaneous.fanArts.madeBy", displayName] + "**")
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
			embed.setImage("${getAvatar.effectiveAvatarUrl}?size=2048")
			context.sendMessage(context.getUserMention(true), embed.build())
		}
	}
}