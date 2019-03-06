package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.config.fanarts.LorittaFanArt
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory

class FanArtsCommand : AbstractCommand("fanarts", category = CommandCategory.MISC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.miscellaneous.fanArts.description", "<a:lori_blobheartseyes:393914347706908683>", "<a:lori_blobheartseyes:393914347706908683>"]
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		sendFanArtEmbed(context, locale, loritta.fanArts, Loritta.RANDOM.nextInt(loritta.fanArts.size))
	}

	suspend fun sendFanArtEmbed(context: CommandContext, locale: LegacyBaseLocale, list: List<LorittaFanArt>, item: Int) {
		val fanArt = list[item]
		val index = loritta.fanArts.indexOf(fanArt) + 1

		val embed = EmbedBuilder().apply {
			setTitle("\uD83D\uDDBC<:loritta:331179879582269451> Fan Art")

			val user = lorittaShards.retrieveUserById(fanArt.artistId)

			val displayName = fanArt.fancyName ?: user?.name

			setDescription("**" + locale.toNewLocale()["commands.miscellaneous.fanArts.madeBy", displayName] + "**")
			val artist = loritta.fanArtConfig.artists[fanArt.artistId]
			if (artist != null) {
				for (socialNetwork in artist.socialNetworks) {
					var root = socialNetwork.display
					if (socialNetwork.link != null) {
						root = "[$root](${socialNetwork.link})"
					}
					appendDescription("\n**${socialNetwork.socialNetwork.fancyName}:** $root")
				}
			}
			appendDescription("\n\n${locale.toNewLocale()["commands.miscellaneous.fanArts.thankYouAll", displayName]}")

			var footer = "Fan Art ${locale.toNewLocale()["loritta.xOfX", index, loritta.fanArts.size]}"

			if (user != null) {
				footer = "${user.name + "#" + user.discriminator} • $footer"
			}

			setFooter(footer, user?.effectiveAvatarUrl)
			setImage("https://loritta.website/assets/img/fanarts/${fanArt.fileName}")
			setColor(Constants.LORITTA_AQUA)
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build())

		var allowForward = false
		var allowBack = false
		if (item != 0) {
			message.addReaction("⏪").queue()
			allowBack = true
		}
		if (list.size > item + 1) {
			message.addReaction("⏩").queue()
			allowForward = true
		}

		message.onReactionAddByAuthor(context) {
			message.delete().queue()

			if (allowForward && it.reactionEmote.name == "⏩") {
				sendFanArtEmbed(context, locale, list, item + 1)
			}
			if (allowBack && it.reactionEmote.name == "⏪") {
				sendFanArtEmbed(context, locale, list, item - 1)
			}
		}
	}
}