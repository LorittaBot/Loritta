package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder

class FanArtsCommand : AbstractCommand("fanarts", category = CommandCategory.MISC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.format { commands.fanarts.description }
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		sendFanArtEmbed(context, locale, loritta.fanArts, Loritta.RANDOM.nextInt(loritta.fanArts.size))
	}

	suspend fun sendFanArtEmbed(context: CommandContext, locale: BaseLocale, list: List<LorittaFanArt>, item: Int) {
		val fanArt = list[item]
		val index = loritta.fanArts.indexOf(fanArt) + 1

		val embed = EmbedBuilder().apply {
			setTitle("\uD83D\uDDBC<:loritta:331179879582269451> Fan Art")

			val user = lorittaShards.retrieveUserById(fanArt.artistId)

			val displayName = fanArt.fancyName ?: user?.name

			setDescription("**" + locale.format(displayName) { commands.fanarts.madeBy } + "**")
			appendDescription("\n\n${locale.format(displayName) { commands.fanarts.thankYouAll }}")

			var footer = "Fan Art ${locale.format(index, loritta.fanArts.size) { loritta.xOfX }}"

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