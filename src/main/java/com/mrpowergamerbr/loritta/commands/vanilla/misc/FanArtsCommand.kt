package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder

class FanArtsCommand : AbstractCommand("fanarts", category = CommandCategory.MISC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["FANARTS_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val list = loritta.fanArts.shuffled()

		sendFanArtEmbed(context, locale, list, 0)
	}

	fun sendFanArtEmbed(context: CommandContext, locale: BaseLocale, list: List<LorittaFanArt>, item: Int) {
		val fanArt = list[item]
		val embed = EmbedBuilder().apply {
			setTitle("\uD83D\uDDBC<:loritta:331179879582269451> Fan Art")

			val user = lorittaShards.retrieveUserById(fanArt.artistId)

			val displayName = fanArt.fancyName ?: user?.name

			var info = ""

			if (user != null) {
				info += user.name + "#" + user.discriminator + "\n"
			}

			if (fanArt.additionalInfo != null) {
				info += fanArt.additionalInfo + "\n"
			}

			setDescription(locale["FANARTS_EmbedDescription", displayName, info])
			setImage("https://loritta.website/assets/img/fanarts/${fanArt.fileName}")
			setColor(Constants.LORITTA_AQUA)
		}

		val message = context.sendMessage(context.getAsMention(true), embed.build())

		var allowForward = false
		var allowBack = false
		if (item != 0) {
			message.addReaction("⏪").complete()
			allowBack = true
		}
		if (list.size > item + 1) {
			message.addReaction("⏩").complete()
			allowForward = true
		}

		message.onReactionAddByAuthor(context) {
			message.delete().complete()

			if (allowForward && it.reactionEmote.name == "⏩") {
				sendFanArtEmbed(context, locale, list, item + 1)
			}
			if (allowBack && it.reactionEmote.name == "⏪") {
				sendFanArtEmbed(context, locale, list, item - 1)
			}
		}
	}
}