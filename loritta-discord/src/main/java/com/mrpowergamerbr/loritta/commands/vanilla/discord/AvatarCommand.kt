package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.Emotes
import java.util.*

class AvatarCommand : AbstractCommand("avatar", category = CommandCategory.DISCORD) {
	companion object {
		const val LOCALE_PREFIX = "commands.command.avatar"
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.avatar.description")

	override fun getUsage(): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = true
			}
		}
	}

	override fun getExamplesKey() = LocaleKeyData("commands.command.avatar.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var getAvatar = context.getUserAt(0)

		if (getAvatar == null) {
			getAvatar = context.userHandle
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
		if (getAvatar.id == loritta.discordConfig.discord.clientId) {
			val calendar = Calendar.getInstance()
			val currentDay = calendar.get(Calendar.DAY_OF_WEEK)

			embed.appendDescription("\n*${context.locale["$LOCALE_PREFIX.lorittaCute"]}* ${Emotes.LORI_SMILE}")
			if (loritta.discordConfig.discord.fanArtExtravaganza.enabled && currentDay == loritta.discordConfig.discord.fanArtExtravaganza.dayOfTheWeek) {
				val currentFanArtInMasterCluster = lorittaShards.queryMasterLorittaCluster("/api/v1/loritta/current-fan-art-avatar").await().obj

				val artistId = currentFanArtInMasterCluster["artistId"].nullString
				val fancyName = currentFanArtInMasterCluster["fancyName"].nullString

				if (artistId != null) {
					val user = lorittaShards.retrieveUserInfoById(artistId.toLong())

					val displayName = fancyName ?: user?.name

					embed.appendDescription("\n\n**" + locale["commands.command.fanarts.madeBy", displayName] + "**")
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
		context.sendMessage(context.getAsMention(true), embed.build())
	}
}